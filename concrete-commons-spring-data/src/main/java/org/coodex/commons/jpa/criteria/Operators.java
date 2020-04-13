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

package org.coodex.commons.jpa.criteria;

import org.coodex.util.Common;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;

/**
 * 根据 sujiwu@126.com 思路重设计、编码
 * <p>
 * Created by davidoff shen on 2017-03-17.
 */
public class Operators {


    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public enum Logical {
        /**
         * 等于
         *
         * @see EqualOperator
         */
        EQUAL(new EqualOperator()),
        /**
         * 小于
         *
         * @see LessOperator
         */
        LESS(new LessOperator()),
        /**
         * 大于
         *
         * @see GreaterOperator
         */
        GREATER(new GreaterOperator()),
        /**
         * 小于等于
         *
         * @see LessEqualOperator
         */
        LESS_EQUAL(new LessEqualOperator()),
        /**
         * 大于等于
         *
         * @see GreaterEqualOperator
         */
        GREATER_EQUAL(new GreaterEqualOperator()),
        /**
         * 不等于
         *
         * @see NotEqualOperator
         */
        NOT_EQUAL(new NotEqualOperator()),

        /**
         * between(a,b)
         *
         * @see BetweenOperator
         */
        BETWEEN(new BetweenOperator()),
        /**
         * in [...]
         *
         * @see InOperator
         */
        IN(new InOperator()),
        /**
         * not in [...]
         *
         * @see NotInOperator
         */
        NOT_IN(new NotInOperator()),
        /**
         * like %...%
         *
         * @see LikeOperator
         */
        LIKE(new LikeOperator()),
        /**
         * like ...%
         *
         * @see StartWithOperator
         */
        START_WITH(new StartWithOperator()),
        /**
         * like %...
         *
         * @see EndWithOperator
         */
        END_WITH(new EndWithOperator()),

        /**
         * like custom pattern, ex: like ..%.._..
         *
         * @see CustomPatternLikeOperator
         */
        CUSTOM_LIKE(new CustomPatternLikeOperator()),

        /**
         * is null
         *
         * @see IsNullOperator
         */
        IS_NULL(new IsNullOperator()),
        /**
         * not null
         *
         * @see NotNullOperator
         */
        NOT_NULL(new NotNullOperator());

        private final Operator operator;

        Logical(Operator operator) {
            this.operator = operator;
        }

        public Operator getOperator() {
            return operator;
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public interface Operator {

        Logical getLogical();

        <ATTR> Predicate toPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes);
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    abstract static class AbstractOperator implements Operator {

        protected abstract <ATTR> void check(ATTR[] attributes);

        protected abstract <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes);

        <ATTR> Class<ATTR> getAttributeType(Path<ATTR> attrPath) {
            return Common.cast(solveFromInstance(Path.class.getTypeParameters()[0], attrPath));
        }

        <ATTR> Predicate unsupported(Path<ATTR> attrPath) {
            throw new IllegalArgumentException("logical " + getLogical()
                    + " unsupported attribute type: " + getAttributeType(attrPath));
        }

        @Override
        public <ATTR> Predicate toPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            check(attributes);
            return buildPredicate(attrPath, cb, attributes);
        }
    }

    /**
     * 无参运算
     * Created by davidoff shen on 2017-03-17.
     */
    abstract static class NoneParamOperator extends AbstractOperator {

        @Override
        protected <ATTR> void check(ATTR[] attributes) {
        }
    }

    /**
     * 多参运算，参数必须至少一个个
     * Created by davidoff shen on 2017-03-17.
     */
    abstract static class MultiParamOperator extends AbstractOperator {
        @Override
        protected <ATTR> void check(ATTR[] attributes) {
            if (attributes == null || attributes.length == 0)
                throw new IllegalArgumentException("must have ONE attribute AT LEAST.");
        }
    }

    /**
     * 单一参数运算
     * Created by davidoff shen on 2017-03-17.
     */
    abstract static class SingleParamOperator extends AbstractOperator {
        @Override
        protected <ATTR> void check(ATTR[] attributes) {
            if (attributes == null || attributes.length != 1)
                throw new IllegalArgumentException("must be ONE attribute.");
        }
    }

    /**
     * 双参数运算，例如between
     * Created by davidoff shen on 2017-03-17.
     */
    abstract static class TwoParamOperator extends AbstractOperator {
        @Override
        protected <ATTR> void check(ATTR[] attributes) {
            if (attributes == null || attributes.length != 2)
                throw new IllegalArgumentException("must be TWO attribute.");
        }
    }

    /**
     * TODO 需要考虑转义符怎么处理
     * Created by davidoff shen on 2017-03-17.
     */
    @SuppressWarnings("unchecked")
    abstract static class AbstractLikeOperator extends SingleParamOperator {

        protected abstract String toPattern(String value);

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            if (attributes[0] instanceof String) {
                return cb.like((Path<String>) attrPath, toPattern((String) attributes[0]));
            } else {
                return unsupported(attrPath);
            }
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class StartWithOperator extends AbstractLikeOperator {
        @Override
        public Logical getLogical() {
            return Logical.START_WITH;
        }

        @Override
        protected String toPattern(String value) {
            return value + "%";
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class NotNullOperator extends NoneParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.NOT_NULL;
        }

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            return cb.isNotNull(attrPath);
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class NotInOperator extends InOperator {

        @Override
        public Logical getLogical() {
            return Logical.NOT_IN;
        }

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            return cb.not(super.buildPredicate(attrPath, cb, attributes));
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class NotEqualOperator extends SingleParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.NOT_EQUAL;
        }

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            return cb.notEqual(attrPath, attributes[0]);
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class LikeOperator extends AbstractLikeOperator {
        @Override
        public Logical getLogical() {
            return Logical.LIKE;
        }

        @Override
        protected String toPattern(String value) {
            return "%" + value + "%";
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */

    public static class LessOperator extends SingleParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.LESS;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
//            if (attributes[0] instanceof Number) {
//                return cb.lt((Path<Number>) attrPath, (Number) attributes[0]);
//            } else
            if (attributes[0] instanceof Comparable) {
                return cb.lessThan(Common.cast(attrPath), (Comparable) attributes[0]);
            } else {
                return unsupported(attrPath);
            }
        }

    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class LessEqualOperator extends SingleParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.LESS_EQUAL;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
//            if (attributes[0] instanceof Number) {
//                return cb.le((Path<Number>) attrPath, (Number) attributes[0]);
//            } else
            if (attributes[0] instanceof Comparable) {
                return cb.lessThanOrEqualTo((Path<? extends Comparable>) attrPath, (Comparable) attributes[0]);
            } else {
                return unsupported(attrPath);
            }
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class IsNullOperator extends NoneParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.IS_NULL;
        }

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            return cb.isNull(attrPath);
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class InOperator extends MultiParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.IN;
        }

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            CriteriaBuilder.In<ATTR> predicate = cb.in(attrPath);

            for (ATTR item : attributes) {
                predicate.value(item);
            }
            return predicate;
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class BetweenOperator extends TwoParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.BETWEEN;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            if (attributes[0] instanceof Comparable) {
                return cb.between((Path<? extends Comparable>) attrPath, (Comparable) attributes[0], (Comparable) attributes[1]);
            } else {
                return unsupported(attrPath);
            }
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class GreaterOperator extends SingleParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.GREATER;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
//            if (attributes[0] instanceof Number) {
//                return cb.gt((Path<Number>) attrPath, (Number) attributes[0]);
//            } else
            if (attributes[0] instanceof Comparable) {
                return cb.greaterThan((Path<? extends Comparable>) attrPath, (Comparable) attributes[0]);
            } else {
                return unsupported(attrPath);
            }
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class GreaterEqualOperator extends SingleParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.GREATER_EQUAL;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
//            if (attributes[0] instanceof Number) {
//                return cb.ge((Path<Number>) attrPath, (Number) attributes[0]);
//            } else
            if (attributes[0] instanceof Comparable) {
                return cb.greaterThanOrEqualTo((Path<? extends Comparable>) attrPath, (Comparable) attributes[0]);
            } else {
                return unsupported(attrPath);
            }
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class EqualOperator extends SingleParamOperator {
        @Override
        public Logical getLogical() {
            return Logical.EQUAL;
        }

        @Override
        protected <ATTR> Predicate buildPredicate(Path<ATTR> attrPath, CriteriaBuilder cb, ATTR[] attributes) {
            return cb.equal(attrPath, attributes[0]);
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class EndWithOperator extends AbstractLikeOperator {
        @Override
        public Logical getLogical() {
            return Logical.END_WITH;
        }

        @Override
        protected String toPattern(String value) {
            return "%" + value;
        }
    }

    /**
     * Created by davidoff shen on 2017-03-17.
     */
    public static class CustomPatternLikeOperator extends AbstractLikeOperator {
        @Override
        public Logical getLogical() {
            return Logical.CUSTOM_LIKE;
        }

        @Override
        protected String toPattern(String value) {
            return value;
        }
    }


}
