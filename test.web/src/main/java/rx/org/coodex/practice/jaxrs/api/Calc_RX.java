package rx.org.coodex.practice.jaxrs.api;

import io.reactivex.Observable;
import org.coodex.concrete.rx.ReactiveExtensionFor;
import org.coodex.practice.jaxrs.api.Calc;

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(Calc.class)
public interface Calc_RX {

    Observable<Integer> add(int x, int y);


}