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

package org.coodex.commons.jpa.springdata;

import org.coodex.commons.jpa.criteria.Operators;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * 少量调整，移除SpecificationGroup, 增加不定参方法
 *
 * @author sujiwu@126.com
 */
@SuppressWarnings("unchecked")
public class SpecCommon {

    public static <E> Specification<E> distinct(final Specification<E> specification) {
        return new Specification<E>() {
            @Override
            public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);
                return specification == null ? null : specification.toPredicate(root, query, cb);
            }
        };
    }

    public static <ATTR, E> Path<ATTR> getPath(Root<E> root, String attrName) {
        From<E, ?> f = null;
        String[] nodes = attrName.split("\\.");
        String attr = null;
        if (nodes.length == 1) {
            f = root;
            attr = attrName;
        } else {
            for (int i = 0; i < nodes.length; i++) {
                attr = nodes[i];
                if (i < nodes.length - 1) {
                    f = root.join(attr);
                }
            }
        }
        return f.get(attr);
    }

    public static <T> Specification<T> not(final Specification<T> spec) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.not(spec.toPredicate(root, query, cb));
            }
        };
    }

    public static <T> Specifications<T> and(Specification<T>... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static <T> Specifications<T> or(Specification<T>... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static <T> Specifications<T> and(Collection<Specification<T>> specList) {
        Specifications<T> specs = null;
        for (Specification<T> s : specList) {
            if (specs != null) {
                specs = specs.and(s);
            } else {
                specs = Specifications.where(s);
            }
        }
        return specs;
    }


    public static <T> Specifications<T> or(Collection<Specification<T>> specList) {
        Specifications<T> specs = null;
        for (Specification<T> s : specList) {
            if (specs != null) {
                specs = specs.or(s);
            } else {
                specs = Specifications.where(s);
            }
        }
        return specs;
    }

    @Deprecated
    public static <ENTITY, ATTR> Specification<ENTITY> spec(
            Class<ENTITY> entityClass,
            Operators.Logical logical,
            String attributeName,
            ATTR... attributes) {

        return spec(logical, attributeName, attributes);
    }

    @Deprecated
    public static <ENTITY, ATTR> Specification<ENTITY> spec(
            Operators.Logical logical,
            String attributeName,
            ATTR... attributes) {

        return $spec(logical, attributeName, attributes);
    }

    protected static <E, A> Specification<E> $spec(Operators.Logical logical,
                                                   String attributeName,
                                                   A... attributes) {

        return new Spec<E, A>(logical, attributeName, attributes);
    }

    @Deprecated
    public static <ENTITY, ATTR> Specification<ENTITY> memberOf(
            Class<ENTITY> entityClass, String attributeName, ATTR attr) {

        return memberOf(attributeName, attr);
    }

    /**
     * where attr in E.attribute
     *
     * @param attributeName
     * @param attr
     * @param <ENTITY>
     * @param <ATTR>
     * @return
     */
    public static <ENTITY, ATTR> Specification<ENTITY> memberOf(String attributeName, ATTR attr) {

        return new MemberOfSpec<ATTR, ENTITY>(attributeName, attr);
    }

    /**
     * where E.attribute < value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> lessThen(String attributeName, A value) {
        return $spec(Operators.Logical.LESS, attributeName, value);
    }

    /**
     * where E.attribute <= value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> lessThenEquals(String attributeName, A value) {
        return $spec(Operators.Logical.LESS_EQUAL, attributeName, value);
    }

    /**
     * where E.attribute = value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> equals(String attributeName, A value) {
        return $spec(Operators.Logical.EQUAL, attributeName, value);
    }

    /**
     * where E.attribute &lt;&gt; value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> notEquals(String attributeName, A value) {
        return $spec(Operators.Logical.NOT_EQUAL, attributeName, value);
    }

    /**
     * where E.attribute > value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> greaterThen(String attributeName, A value) {
        return $spec(Operators.Logical.GREATER, attributeName, value);
    }

    /**
     * where E.attribute >= value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> greaterThenEquals(String attributeName, A value) {
        return $spec(Operators.Logical.GREATER_EQUAL, attributeName, value);
    }

    /**
     * where E.attribute like %value%
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @return
     */
    public static <E> Specification<E> like(String attributeName, String value) {
        return $spec(Operators.Logical.LIKE, attributeName, value);
    }

    /**
     * where E.attribute like value%
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @return
     */
    public static <E> Specification<E> startWith(String attributeName, String value) {
        return $spec(Operators.Logical.START_WITH, attributeName, value);
    }

    /**
     * where E.attribute like %value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @return
     */
    public static <E> Specification<E> endWith(String attributeName, String value) {
        return $spec(Operators.Logical.END_WITH, attributeName, value);
    }

    /**
     * where E.attribute like value
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @return
     */
    public static <E> Specification<E> customLike(String attributeName, String value) {
        return $spec(Operators.Logical.CUSTOM_LIKE, attributeName, value);
    }

    /**
     * where E.attribute is NULL
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    @Deprecated
    public static <E, A> Specification<E> isNull(String attributeName, A value) {
//        return $spec(Operators.Logical.IS_NULL, attributeName, value);
        return isNull(attributeName);
    }

    public static <E> Specification<E> isNull(String attributeName) {
        return $spec(Operators.Logical.IS_NULL, attributeName);
    }

    /**
     * where E.attribute is not NULL
     *
     * @param attributeName
     * @param value
     * @param <E>
     * @param <A>
     * @return
     */
    @Deprecated
    public static <E, A> Specification<E> notNull(String attributeName, A value) {
//        return $spec(Operators.Logical.NOT_NULL, attributeName, value);
        return notNull(attributeName);
    }

    public static <E> Specification<E> notNull(String attributeName) {
        return $spec(Operators.Logical.NOT_NULL, attributeName);
    }


    /**
     * where E.attribute in (values)
     *
     * @param attributeName
     * @param values
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> in(String attributeName, A... values) {
        return $spec(Operators.Logical.IN, attributeName, values);
    }

    /**
     * where E.attribute not in (values)
     *
     * @param attributeName
     * @param values
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> notIn(String attributeName, A... values) {
        return $spec(Operators.Logical.NOT_IN, attributeName, values);
    }

    /**
     * where E.attribute between min and max
     *
     * @param attributeName
     * @param min
     * @param max
     * @param <E>
     * @param <A>
     * @return
     */
    public static <E, A> Specification<E> between(String attributeName, A min, A max) {
        return $spec(Operators.Logical.BETWEEN, attributeName, min, max);
    }


    public static <ENTITY> Specifications<ENTITY> wrapper(Specifications<ENTITY> specifications) {
        return specifications == null ? Specifications.where(specifications) : specifications;
    }

    static class MemberOfSpec<ATTR, ENTITY> implements Specification<ENTITY> {
        private final ATTR attr;
        private final String attributeName;

        MemberOfSpec(String attributeName, ATTR attr) {
            this.attr = attr;
            this.attributeName = attributeName;
        }

        @Override
        public Predicate toPredicate(Root<ENTITY> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            Path<Collection<ATTR>> path = SpecCommon.getPath(root, attributeName);
            return cb.isMember(attr, path);
        }
    }


    /**
     * 根据 sujiwu@126.com 思路重设计编码
     * <p>
     * Created by davidoff shen on 2017-03-17.
     */
    static class Spec<ENTITY, ATTR> implements Specification<ENTITY> {

        private final Operators.Logical logical;
        private final ATTR[] attributes;
        private final String attributeName;

        Spec(Operators.Logical logical, String attributeName, ATTR... attributes) {
            this.logical = logical;
            this.attributeName = attributeName;
            this.attributes = attributes;
        }

        @Override
        public Predicate toPredicate(Root<ENTITY> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            Path<ATTR> path = getPath(root, attributeName);
            return logical.getOperator().toPredicate(path, cb, attributes);
        }

        protected ATTR[] getAttributes() {
            return attributes;
        }

        protected String getAttributeName() {
            return attributeName;
        }
    }
}
