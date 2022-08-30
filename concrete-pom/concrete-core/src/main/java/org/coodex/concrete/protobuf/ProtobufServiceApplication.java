/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.protobuf;

import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.ServerSideContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.own.OwnServiceProvider;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.concrete.own.ResponsePackage;
import org.coodex.util.JSONSerializer;
import org.coodex.util.SingletonMap;

import java.util.Optional;
import java.util.function.Consumer;

public class ProtobufServiceApplication extends OwnServiceProvider {

    public static final String SUBJOIN_KEY_INVOKER = "x-invoker-provider";

    private static final SingletonMap<String, ProtobufServiceApplication> APPLICATIONS = SingletonMap
            .<String, ProtobufServiceApplication>builder()
            .function(ProtobufServiceApplication::new)
            .build();
    private final String overProtocol;
    private OwnModuleBuilder ownModuleBuilder;


    private ProtobufServiceApplication(String overProtocol) {
        this.overProtocol = overProtocol;
    }

    public static ProtobufServiceApplication getInstance(String protocol) {
        return APPLICATIONS.get(protocol);
    }

    private static Concrete.ResponsePackage toResp(ResponsePackage<?> resp) {
        return Concrete.ResponsePackage.newBuilder()
                .putAllSubjoin(resp.getSubjoin())
                .setContent(JSONSerializer.getInstance().toJson(resp.getContent()))
                .setConcreteTokenId(Optional.ofNullable(resp.getConcreteTokenId()).orElse(""))
                .setOk(resp.isOk())
                .build();
    }

    private static RequestPackage<Object> toReq(Concrete.RequestPackage req) {
        RequestPackage<Object> requestPackage = new RequestPackage<>();
        requestPackage.setServiceId(req.getServiceId());
        requestPackage.setContent(JSONSerializer.getInstance().parse(req.getContent(), Object.class));
        requestPackage.setConcreteTokenId(req.getConcreteTokenId());
        requestPackage.setSubjoin(req.getSubjoinMap());
        return requestPackage;
    }

    @Override
    public String getNamespace() {
        return "protobuf.over." + overProtocol;
    }

    @Override
    protected OwnModuleBuilder getModuleBuilder() {
        if (ownModuleBuilder == null) {
            ownModuleBuilder = c -> new ProtobufModule(c, overProtocol);
        }
        return ownModuleBuilder;
    }

    @Override
    protected ServerSideContext getServerSideContext(RequestPackage<Object> requestPackage, String tokenId,
                                                     Caller caller) {
        Subjoin subjoin = getSubjoin(requestPackage.getSubjoin());
        return new ProtobufContext(
                caller,
                subjoin,
                getLocale(subjoin),
                tokenId
        );
    }

    @Override
    protected String getModuleName() {
        return "protobuf.over." + overProtocol;
    }

    public void invokeService(Concrete.RequestPackage requestPackage,
                              String remoteAddress,
                              Consumer<Concrete.ResponsePackage> responsePackageConsumer) {
        invokeService(toReq(requestPackage), new Caller() {
                    @Override
                    public String getAddress() {
                        return remoteAddress;
                    }

                    @Override
                    public String getClientProvider() {
                        return requestPackage.getSubjoinOrDefault(SUBJOIN_KEY_INVOKER, "unknown-client-invoker");
                    }
                }, responsePackage -> responsePackageConsumer.accept(toResp(responsePackage)),
                null, null);
    }
}
