/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.*;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.util.Collection;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class BeanValidationInterceptor extends AbstractInterceptor {

    private final static Logger log = LoggerFactory.getLogger(BeanValidationInterceptor.class);

    private final static ViolationsFormatter DEFAULT_FORMMATER = new ViolationsFormatter() {
        @Override
        public <T> String format(Collection<ConstraintViolation<T>> violations) {
            StringBuilder buf = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                buf.append(violation.getMessage()).append("\n");
            }
            return buf.toString();
        }
    };

    private final static ServiceLoader<ViolationsFormatter> VIOLATIONS_FORMATTER_SPI = new ConcreteServiceLoader<ViolationsFormatter>() {
        @Override
        protected ViolationsFormatter getConcreteDefaultProvider() {
            return DEFAULT_FORMMATER;
        }
    };


    private ExecutableValidator executableValidator = null; // jsr339
    private boolean hasProvider = true;


    private synchronized ExecutableValidator getValidator() {
        if (executableValidator == null) {
            if (hasProvider) {
                try {
                    executableValidator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
                } catch (Throwable t) {
                    hasProvider = false;
                    log.warn("Failed to load validation provider: {}", t.getLocalizedMessage());
                }
            }
        }

        return executableValidator;
    }


    public void checkViolations(Set<ConstraintViolation<Object>> constraintViolations) {
        if (constraintViolations.size() > 0) {
            throw new ConcreteException(ErrorCodes.DATA_VIOLATION,
                    VIOLATIONS_FORMATTER_SPI.getInstance().format(constraintViolations));
        }
    }

    @Override
    public int getOrder() {
        return InterceptOrders.BEAN_VALIDATION;
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return ConcreteHelper.getProfile().getBool("aspect.bean.validation", true)
                && getValidator() != null;
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        checkViolations(getValidator().validateParameters(
                joinPoint.getThis(), context.getDeclaringMethod(), joinPoint.getArguments()));
    }
}
