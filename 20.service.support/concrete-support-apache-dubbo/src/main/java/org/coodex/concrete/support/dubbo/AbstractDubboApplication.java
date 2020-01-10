/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.support.dubbo;

import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.coodex.concrete.api.Application;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;
import org.coodex.concrete.common.*;
import org.coodex.concrete.dubbo.ApacheDubboSubjoin;
import org.coodex.util.Common;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;

import static org.coodex.concrete.common.ConcreteHelper.*;
import static org.coodex.concrete.dubbo.DubboConfigCaching.getApplicationConfig;
import static org.coodex.concrete.dubbo.DubboConfigCaching.getServiceVersion;

public abstract class AbstractDubboApplication implements Application {

    private final static Logger log = LoggerFactory.getLogger(AbstractDubboApplication.class);

    private final String applicationName;
    private Singleton<String> version = new Singleton<>(this::getVersion);

    public AbstractDubboApplication(String applicationName) {
        this.applicationName = applicationName;
    }

    private static Locale getLocale(String localeStr) {
        try {
            return Common.isBlank(localeStr) ? null : Locale.forLanguageTag(localeStr);
        } catch (Throwable th) {
            log.warn("invalid locale string: {}", localeStr);
            return null;
        }
    }

    @Override
    public void registerPackage(String... packages) {
        if (packages == null || packages.length == 0) {
            foreachClassInPackages(this::registerClass, getApiPackages(getNamespace()));
        } else {
            foreachClassInPackages(this::registerClass, packages);
        }
    }

    @Override
    public void register(Class<?>... classes) {
        if (classes == null || classes.length == 0) return;
        for (Class<?> clz : classes) {
            registerClass(clz);
        }
    }

    private void registerClass(Class<?> clz) {
        if (isConcreteService(clz)) {
            registerConcreteService(clz);
        }
    }

    protected abstract String getVersion();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerConcreteService(Class<?> clz) {
        // 惰性加载，确保在运行期拿到实现实例
        final Singleton<Object> serviceImpl = new Singleton<>(() -> BeanServiceLoaderProvider.getBeanProvider().getBean(clz));

        // serviceConfig
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setApplication(getApplicationConfig(applicationName));
        serviceConfig.setRegistries(getRegistries());
        serviceConfig.setProtocols(getProtocols());
        serviceConfig.setInterface(clz);
        serviceConfig.setVersion(getServiceVersion(version.get()));

        // 通过java动态代理设置实现
        serviceConfig.setRef(Proxy.newProxyInstance(clz.getClassLoader(), new Class<?>[]{clz}, (proxy, method, args) -> {
            RpcContext context = RpcContext.getContext();
            // 使用rpc上下文的attachments构建subjoin
            Subjoin subjoin = new ApacheDubboSubjoin(context.getAttachments()).wrap();
            // caller,使用上下文中提供的远端地址及attachments中的locale
            ApacheDubboCaller caller = new ApacheDubboCaller(context);
            String tokenId = subjoin.get(TOKEN_KEY);
            // apm
            Trace trace = APM.build(subjoin)
                    .tag("remote", caller.getAddress())
                    .tag("agent", caller.getClientProvider())
                    .start(String.format("apache-dubbo: %s.%s", method.getDeclaringClass().getName(), method.getName()));

            try {
                ServerSideContext serverSideContext = new ApacheDubboServerSideServiceContext(
                        new ApacheDubboCaller(context),
                        subjoin,
                        getLocale(subjoin.get(LOCALE_KEY)),
                        tokenId);
                Object result = ConcreteContext.runServiceWithContext(
                        serverSideContext,
                        () -> (args == null || args.length == 0) ?
                                method.invoke(serviceImpl.get()) :
                                method.invoke(serviceImpl.get(), args),
                        clz, method, args);

                try {
                    String newTokenId = serverSideContext.getTokenId();
                    if (!Common.isBlank(newTokenId) && !Common.isSameStr(newTokenId, tokenId)) {
                        subjoin.add(Token.CONCRETE_TOKEN_ID_KEY, newTokenId);
                    }
                } catch (Throwable th) {
                    log.warn("token error?? {}", th.getLocalizedMessage(), th);
                }
                context.setAttachments(ConcreteHelper.updatedMap(subjoin));
                return result;
            } catch (Throwable th) {
                context.setAttachments(ConcreteHelper.updatedMap(subjoin));
                trace.error(th);
                throw th;
            } finally {
                trace.finish();
            }
        }));

        serviceConfig.export();
    }

    @Override
    public String getNamespace() {
        return "dubbo";
    }

    public String getApplicationName() {
        return applicationName;
    }

    protected abstract List<RegistryConfig> getRegistries();

    protected abstract List<ProtocolConfig> getProtocols();
}
