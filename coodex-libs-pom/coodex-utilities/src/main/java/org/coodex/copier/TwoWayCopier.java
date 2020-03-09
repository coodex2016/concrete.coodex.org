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

import java.util.Collection;

/**
 * 双向copier
 * Created by davidoff shen on 2017-05-11.
 */
public interface TwoWayCopier<A, B> {


    A newA();

    A initA();

    A initA(A o);

    B copyA2B(A a);

    B copyA2B(A a, B b);

    <T extends Collection<B>> T copyA2B(Collection<A> srcCollection);

    <T extends Collection<B>> T copyA2B(Collection<A> srcCollection, Class<T> clazz);

    B newB();

    B initB();

    B initB(B o);

    A copyB2A(B b);

    A copyB2A(B b, A a);

    <T extends Collection<A>> T copyB2A(Collection<B> srcCollection);

    <T extends Collection<A>> T copyB2A(Collection<B> srcCollection, Class<T> clazz);


    Copier<A, B> a2bCopier();

    Copier<B, A> b2aCopier();


}
