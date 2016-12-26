package cc.coodex.concrete.common;

import javax.validation.ConstraintViolation;
import java.util.Collection;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public interface ViolationsFormatter {

    <T> String format(Collection<ConstraintViolation<T>> violations);
}
