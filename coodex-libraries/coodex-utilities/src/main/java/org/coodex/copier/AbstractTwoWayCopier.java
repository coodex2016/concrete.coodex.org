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

package org.coodex.copier;

import org.coodex.util.Common;

import java.util.Collection;

/**
 * Created by davidoff shen on 2017-05-11.
 */

public abstract class AbstractTwoWayCopier<A, B>
        extends AbstractCopierCommon
        implements TwoWayCopier<A, B> {

    private final Copier<A, B> a2bCopier = new Copier<A, B>() {
        @Override
        public B newTargetObject() {
            return newB();
        }

        @Override
        public B initTargetObject(B b) {
            return initB(b);
        }

        @Override
        public B initTargetObject() {
            return initB();
        }

        @Override
        public B copy(A a, B b) {
            return copyA2B(a, b);
        }

        @Override
        public B copy(A a) {
            return copyA2B(a);
        }

        @Override
        public <T extends Collection<B>> T copy(Collection<A> as, Class<T> clazz) {
            return copyA2B(as, clazz);
        }

        @Override
        public Collection<B> copy(Collection<A> as) {
            return copyA2B(as);
        }
    };

    private final Copier<B, A> b2aCopier = new Copier<B, A>() {
        @Override
        public A newTargetObject() {
            return newA();
        }

        @Override
        public A initTargetObject(A a) {
            return initA(a);
        }

        @Override
        public A initTargetObject() {
            return initA();
        }

        @Override
        public A copy(B b, A a) {
            return copyB2A(b, a);
        }

        @Override
        public A copy(B b) {
            return copyB2A(b);
        }

        @Override
        public <T extends Collection<A>> T copy(Collection<B> bs, Class<T> clazz) {
            return copyB2A(bs, clazz);
        }

        @Override
        public Collection<A> copy(Collection<B> bs) {
            return copyB2A(bs);
        }
    };

    @Override
    protected Object copy(Object o, Index srcIndex) {
        if (Index.A.getIndex() == srcIndex.getIndex()) {
            A a = Common.cast(o);
            return copyA2B(a);
        } else {
            B b = Common.cast(o);
            return copyB2A(b);
        }
    }

    @Override
    public A newA() {
        return Common.cast(newObject(Index.A));
    }

    @Override
    public A initA() {
        return initA(newA());
    }

    @Override
    public A initA(A o) {
        return Common.cast(init(o, Index.A));
    }

    @Override
    public A copyB2A(B b) {
        return copyB2A(b, initA());
    }


    @Override
    public <T extends Collection<A>> T copyB2A(Collection<B> srcCollection) {
        return copy(srcCollection, Index.B);
    }

    @Override
    public <T extends Collection<A>> T copyB2A(Collection<B> srcCollection, Class<T> clazz) {
        return copy(srcCollection, clazz, Index.B);
    }

    @Override
    public B newB() {
        return Common.cast(newObject(Index.B));
    }

    @Override
    public B initB() {
        return initB(newB());
    }

    @Override
    public B initB(B o) {
        return Common.cast(init(o, Index.B));
    }

    @Override
    public B copyA2B(A a) {
        return copyA2B(a, initB());
    }

    @Override
    public <T extends Collection<B>> T copyA2B(Collection<A> srcCollection) {
        return copy(srcCollection, Index.A);
    }

    @Override
    public <T extends Collection<B>> T copyA2B(Collection<A> srcCollection, Class<T> clazz) {
        return copy(srcCollection, clazz, Index.A);
    }

    @Override
    public Copier<A, B> a2bCopier() {
        return a2bCopier;
    }

    @Override
    public Copier<B, A> b2aCopier() {
        return b2aCopier;
    }
}
