///*
// * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.coodex.util;
//
//public class CyclicQueue<T> {
//    private final int capacity;
//    private final Object[] elements;
//    private int tail = 0;
//    private int header = 0;
//
//    public CyclicQueue(int capacity) {
//        this.capacity = capacity;
//        elements = new Object[this.capacity];
//    }
//
//    public int size() {
//        if ((tail + 1) % capacity == header) return capacity;
//        if (header == tail) return 0;
//        return (tail + capacity - header) % capacity;
//    }
//
//    public boolean isEmpty() {
//        return header != tail;
//    }
//
//    public void add(T t) {
//        elements[tail] = t;
//        tail = (tail + 1) % capacity;
//        if (tail == header) {
//            header = (header + 1) % capacity;
//        }
//    }
//
//
//    public void clear() {
//        tail = header = 0;
//    }
//
//    public T get(int i) {
//        if (i < 0 || i >= size()) {
//            return null;
//        } else {
//            return Common.cast(elements[(i + header) % capacity]);
//        }
//    }
//
//
//}
