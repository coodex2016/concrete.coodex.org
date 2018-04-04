package rx.org.coodex.concrete.test.api;

import org.coodex.concrete.rx.ReactiveExtensionFor;

import io.reactivex.Observable;

import org.coodex.concrete.test.api.Test;

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(Test.class)
public interface Test_RX {

    Observable<String> sayHello(String name);

    Observable<Integer> add(int x1, int x2);


}