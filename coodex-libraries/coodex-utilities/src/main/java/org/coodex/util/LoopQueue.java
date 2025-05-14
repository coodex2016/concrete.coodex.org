/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LoopQueue<E> {

    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final int capacity;
    private final E[] data;

    private int head = 0, tail = 0, size = 0;

    public LoopQueue(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity of loop queue must be large then 0.");
        this.capacity = capacity;
        this.data = Common.cast(new Object[capacity]);
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }


    public boolean isEmpty() {
        return size == 0;
    }

    private E[] getAllData() {
        if (isEmpty()) return null;
        int headRef = head;
        E[] r = Common.cast(new Object[size]);
        for (int i = 0; i < r.length; i++) {
            r[i] = data[headRef];
            headRef = (headRef + 1) % capacity;
        }
        return r;
    }

    public int size() {
        return size;
    }

    public void put(E e) {
        lock.lock();
        try {
            data[tail] = e;
            moveTail();
            if (size > capacity) {
                moveHead();
            }
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public E poll() {
        if (isEmpty())
            return null;
        else {
            lock.lock();
            try {
                E element = data[head];
                moveHead();
                return element;
            } finally {
                lock.unlock();
            }
        }
    }

    public E[] takeAll(long millis) {
        lock.lock();
        try {
            if (isEmpty()) {
                notEmpty.await(millis, TimeUnit.MILLISECONDS);
            }
            E[] r = getAllData();
            clear();
            return r;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            size = tail = head = 0;
        } finally {
            lock.unlock();
        }
    }

    private void moveHead() {
        size--;
        head = (head + 1) % capacity;
    }

    private void moveTail() {
        size++;
        tail = (tail + 1) % capacity;
    }

    public E peek() {
        if (isEmpty())
            return null;
        else
            return data[head];
    }

    @Override
    public String toString() {
        return "LoopQueue{" +
                "capacity=" + capacity +
                ", head=" + head +
                ", tail=" + tail +
                ", size=" + size +
                ", data=" + Arrays.toString(getAllData()) +
                '}';
    }
}
