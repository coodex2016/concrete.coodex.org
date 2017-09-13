package rx.org.coodex.practice.jaxrs.api;

import org.coodex.concrete.rx.ReactiveExtensionFor;

import io.reactivex.Observable;

import org.coodex.practice.jaxrs.api.SaaSExample;

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(SaaSExample.class)
public interface SaaSExample_RX {

    Observable<String> exampleForSaaS(String tenantId, String ok);


}