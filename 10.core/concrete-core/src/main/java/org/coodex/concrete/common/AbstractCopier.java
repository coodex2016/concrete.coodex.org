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

package org.coodex.concrete.common;


/**
 * 根据 sujiwu@126.com 的设计思路、代码修改
 * <p>
 * Created by davidoff shen on 2017-03-17.
 *
 * @see org.coodex.copier.AbstractCopier
 * @deprecated 移动到coodex-utilities
 */
@Deprecated
public abstract class AbstractCopier<SRC, TARGET>
        extends org.coodex.copier.AbstractCopier<SRC, TARGET>
        implements Copier<SRC, TARGET>{

//    //    private Class targetClass;
//    @SuppressWarnings("unchecked")
//    public TARGET newTargetObject() {
//        return (TARGET) newObject(Index.B);
////        synchronized (this) {
////            if (targetClass == null) {
////                targetClass = getClass(1);
////            }
////        }
////        // 根据第二个泛型参数创建实例
//////        Class<TARGET> clz = (Class<TARGET>) TypeHelper.findActualClassFrom(AbstractCopier.class.getTypeParameters()[1], getClass());
////        try {
////            return (TARGET) targetClass.newInstance();
////        } catch (Throwable th) {
////            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
////        }
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public TARGET initTargetObject(TARGET target) {
//        return (TARGET) init(target, Index.B);
//    }
//
//    @Override
//    public TARGET initTargetObject() {
//        return initTargetObject(null);
//    }
//
////    protected TARGET init(TARGET target) {
////        return target;
////    }
//
//    public TARGET copy(SRC src) {
//        return copy(src, initTargetObject());
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    protected Object copy(Object o, Index srcIndex) {
//        return copy((SRC) o);
//    }
//
//    @Override
//    public <T extends Collection<TARGET>> T copy(Collection<SRC> srcCollection, Class<T> clazz) {
//        return copy(srcCollection, clazz, Index.A);
////        if (srcCollection == null) throw new NullPointerException("srcCollection is NULL.");
////        Collection<TARGET> collection = null;
////        if (List.class.equals(clazz)) {
////            collection = new ArrayList<TARGET>();
////        } else if (Set.class.equals(clazz)) {
////            collection = new HashSet<TARGET>();
////        } else {
////            try {
////                collection = clazz.getConstructor().newInstance();
////            } catch (InstantiationException e) {
////            } catch (IllegalAccessException e) {
////            } catch (InvocationTargetException e) {
////            } catch (NoSuchMethodException e) {
////            }
////        }
////        if (collection == null)
////            throw new IllegalArgumentException("class :" + clazz.getCanonicalName() + " not support.");
////        for (SRC src : srcCollection) {
////            collection.add(copy(src));
////        }
////        return (T) collection;
//    }
//
//    @Override
//    public Collection<TARGET> copy(Collection<SRC> srcCollection) {
//        return copy(srcCollection, Index.A);
////        Class<? extends Collection> clazz = srcCollection.getClass();
////        if (List.class.isAssignableFrom(clazz)) {
////            return copy(srcCollection, List.class);
////        } else if (Set.class.isAssignableFrom(clazz)) {
////            return copy(srcCollection, Set.class);
////        } else
////            return copy(srcCollection, clazz);
//    }
}
