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

package org.coodex.concrete.own;

import org.coodex.closure.CallableClosure;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.message.ServerSideMessage;
import org.coodex.concrete.message.TBMContainer;
import org.coodex.concurrent.components.PriorityRunnable;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.runWithContext;
import static org.coodex.concrete.common.ConcreteHelper.*;
import static org.coodex.concrete.common.ErrorCodes.SERVICE_ID_NOT_EXISTS;
import static org.coodex.concrete.own.PackageHelper.analysisParameters;

public abstract class OwnServiceProvider {

    private final static Logger log = LoggerFactory.getLogger(OwnServiceProvider.class);
    private final Map<String, AbstractUnit> unitMap = new HashMap<String, AbstractUnit>();

    protected Subjoin getSubjoin(RequestPackage requestPackage) {
        return getSubjoin(requestPackage.getSubjoin());
    }

    protected Locale getLocale(Subjoin subjoin) {
        if (subjoin != null && !Common.isBlank(subjoin.get("locale"))) {
            try {
                return LanguageTag.valueOf(subjoin.get("locale")).getAsLocale();
            } catch (IllegalArgumentException e) {

            }
        }
        return Locale.getDefault();
    }

    protected void appendUnits(OwnServiceModule module) {
        for (AbstractUnit unit : module.getUnits()) {
            unitMap.put(((OwnServiceUnit) unit).getKey(), unit);
        }
    }

    public final void registerPackage(String... packages) {
        foreachClassInPackages(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                registerClasses(serviceClass);
            }
        }, packages);
//        if (packages == null || packages.length == 0) {
//            packages = ConcreteHelper.getApiPackages();
//        }
//        List<WebSocketModule> modules = ConcreteHelper.loadModules(WebSocketModuleMaker.WEB_SOCKET_SUPPORT, packages);
//        for (WebSocketModule module : modules) {
//            appendUnits(module);
//        }
    }

    @SuppressWarnings("unchecked")
    public final void registerClasses(Class<?>... classes) {
        for (final Class<?> clz : classes) {
            if (AbstractErrorCodes.class.isAssignableFrom(clz)) {
                ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clz);
            } else if (ConcreteHelper.isConcreteService(clz)) {
                appendUnits(getModuleBuilder().build(clz));
            } else {
                throw new RuntimeException("cannot register class:" + clz.getName());
            }
        }
    }

    protected abstract OwnModuleBuilder getModuleBuilder();

    protected abstract Subjoin getSubjoin(Map<String, String> map);

    protected abstract ServerSideContext getServerSideContext(RequestPackage<Object> requestPackage,
                                                              String tokenId, Caller caller);

    protected void invokeService(final RequestPackage<Object> requestPackage,
                                 final Caller caller,
                                 final OwnServiceProvider.ResponseVisitor responseVisitor,
                                 final OwnServiceProvider.ErrorVisitor errorVisitor,
                                 final OwnServiceProvider.ServerSideMessageVisitor serverSideMessageVisitor,
                                 final OwnServiceProvider.TBMNewTokenVisitor newTokenVisitor) {
        IF.isNull(responseVisitor, ErrorCodes.OWN_PROVIDER_NO_RESPONSE_VISITOR, getModuleName());
        //1 找到方法
        final AbstractUnit unit = IF.isNull(unitMap.get(requestPackage.getServiceId()),
                SERVICE_ID_NOT_EXISTS, requestPackage.getServiceId());

        //2 解析数据
        final Object[] objects = analysisParameters(
                JSONSerializerFactory.getInstance().toJson(requestPackage.getContent()), unit);

        //3 调用并返回结果
        final String tokenId = requestPackage.getConcreteTokenId();

        ConcreteHelper.getExecutor().execute(new PriorityRunnable(ConcreteHelper.getPriority(unit), new Runnable() {
            private Method method = unit.getMethod();

            @Override
            public void run() {

                ServerSideContext context = getServerSideContext(requestPackage, tokenId, caller);

                Trace trace = APM.build(context.getSubjoin())
                        .tag("remote", context.getCaller().getAddress())
                        .tag("agent", context.getCaller().getClientProvider())
                        .start(String.format("%s: %s.%s", getModuleName(), method.getDeclaringClass().getName(), method.getName()));
                try {

                    Object result = runWithContext(
                            context,
                            new CallableClosure() {

                                public Object call() throws Throwable {
                                    if (isDevModel(getModuleName())) {
                                        return void.class.equals(unit.getGenericReturnType()) ?
                                                null :
                                                MockerFacade.mock(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass());
                                    } else {
                                        Object instance = BeanProviderFacade.getBeanProvider().getBean(unit.getDeclaringModule().getInterfaceClass());
                                        if (objects == null)
                                            return method.invoke(instance);
                                        else
                                            return method.invoke(instance, objects);
                                    }

                                }
                            });

                    ResponsePackage responsePackage = new ResponsePackage();
                    final String tokenIdAfterInvoke = context.getTokenId();
                    if (!Common.isSameStr(tokenId, tokenIdAfterInvoke)
                            && !Common.isBlank(tokenIdAfterInvoke)) {

                        responsePackage.setConcreteTokenId(tokenIdAfterInvoke);
                        // 订阅消息推送
                        if (!Common.isBlank(tokenId)) {
                            if (newTokenVisitor != null) {
                                newTokenVisitor.visit(tokenIdAfterInvoke);
                            }
                            TBMContainer.getInstance().clear(tokenId);
                        }
                        TBMContainer.getInstance().listen(tokenIdAfterInvoke, new TBMContainer.TBMListener() {
                            @Override
                            public String getTokenId() {
                                return tokenIdAfterInvoke;
                            }

                            @Override
                            public void onMessage(ServerSideMessage serverSideMessage) {
                                serverSideMessageVisitor.visit(serverSideMessage, tokenIdAfterInvoke);
//                                sendMessage(serverSideMessage, tokenIdAfterInvoke);
                            }
                        });

                    }
                    responsePackage.setSubjoin(updatedMap(context.getSubjoin()));
                    responsePackage.setMsgId(requestPackage.getMsgId());
                    responsePackage.setOk(true);
                    responsePackage.setContent(result);
                    responseVisitor.visit(responsePackage);
//                    sendText(JSONSerializerFactory.getInstance().toJson(responsePackage), session);
                } catch (final Throwable th) {
                    trace.error(th);
                    runWithContext(context, new CallableClosure() {
                        @Override
                        public Object call() throws Throwable {
                            if (errorVisitor != null) {
                                errorVisitor.visit(requestPackage.getMsgId(), th);
                            } else {
                                log.warn("no error visitor: {}", getModuleName());
                            }
                            return null;
                        }
                    });

                } finally {
                    trace.finish();
                }

            }
        }));
    }

    protected abstract String getModuleName();

    public interface ResponseVisitor {
        void visit(ResponsePackage responsePackage);

        void visit(String json);
    }

    public interface ErrorVisitor {
        void visit(String msgId, Throwable th);
    }

    public interface ServerSideMessageVisitor {
        void visit(ServerSideMessage serverSideMessage, String tokenId);
    }

    public interface TBMNewTokenVisitor {
        void visit(String tokenId);
    }

    public interface OwnModuleBuilder {
        OwnServiceModule build(Class clz);
    }

    /**
     * 摘自 org.glassfish.jersey.message.internal.LanguageTag
     * <p>
     * A language tag.
     *
     * @author Paul Sandoz
     * @author Marek Potociar (marek.potociar at oracle.com)
     */
    public static class LanguageTag {

        String tag;
        String primaryTag;
        String subTags;

        protected LanguageTag() {
        }

        public LanguageTag(final String primaryTag, final String subTags) {
            if (subTags != null && subTags.length() > 0) {
                this.tag = primaryTag + "-" + subTags;
            } else {
                this.tag = primaryTag;
            }

            this.primaryTag = primaryTag;

            this.subTags = subTags;
        }

        public static LanguageTag valueOf(final String s) throws IllegalArgumentException {
            final LanguageTag lt = new LanguageTag();

            try {
                lt.parse(s);
            } catch (final ParseException pe) {
                throw new IllegalArgumentException(pe);
            }

            return lt;
        }

        public final boolean isCompatible(final Locale tag) {
            if (this.tag.equals("*")) {
                return true;
            }

            if (subTags == null) {
                return primaryTag.equalsIgnoreCase(tag.getLanguage());
            } else {
                return primaryTag.equalsIgnoreCase(tag.getLanguage())
                        && subTags.equalsIgnoreCase(tag.getCountry());
            }
        }

        public final Locale getAsLocale() {
            return (subTags == null)
                    ? new Locale(primaryTag)
                    : new Locale(primaryTag, subTags);
        }

        protected final void parse(final String languageTag) throws ParseException {
            if (!isValid(languageTag)) {
                throw new ParseException("String, " + languageTag + ", is not a valid language tag", 0);
            }

            final int index = languageTag.indexOf('-');
            if (index == -1) {
                primaryTag = languageTag;
                subTags = null;
            } else {
                primaryTag = languageTag.substring(0, index);
                subTags = languageTag.substring(index + 1, languageTag.length());
            }
        }

        /**
         * Validate input tag (header value) according to HTTP 1.1 spec + allow region code (numeric) instead of country code.
         *
         * @param tag accept-language header value.
         * @return {@code true} if the given value is valid language tag, {@code false} instead.
         */
        private boolean isValid(final String tag) {
            int alphanumCount = 0;
            int dash = 0;
            for (int i = 0; i < tag.length(); i++) {
                final char c = tag.charAt(i);
                if (c == '-') {
                    if (alphanumCount == 0) {
                        return false;
                    }
                    alphanumCount = 0;
                    dash++;
                } else if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || (dash > 0 && '0' <= c && c <= '9')) {
                    alphanumCount++;
                    if (alphanumCount > 8) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return (alphanumCount != 0);
        }

        public final String getTag() {
            return tag;
        }

        public final String getPrimaryTag() {
            return primaryTag;
        }

        public final String getSubTags() {
            return subTags;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LanguageTag) || o.getClass() != this.getClass()) {
                return false;
            }

            final LanguageTag that = (LanguageTag) o;

            if (primaryTag != null ? !primaryTag.equals(that.primaryTag) : that.primaryTag != null) {
                return false;
            }
            if (subTags != null ? !subTags.equals(that.subTags) : that.subTags != null) {
                return false;
            }
            return !(tag != null ? !tag.equals(that.tag) : that.tag != null);

        }

        @Override
        public int hashCode() {
            int result = tag != null ? tag.hashCode() : 0;
            result = 31 * result + (primaryTag != null ? primaryTag.hashCode() : 0);
            result = 31 * result + (subTags != null ? subTags.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return primaryTag + (subTags == null ? "" : subTags);
        }
    }

    public abstract static class DefaultResponseVisitor implements ResponseVisitor {
        @Override
        public void visit(ResponsePackage responsePackage) {
            visit(JSONSerializerFactory.getInstance().toJson(responsePackage));
        }
    }
}
