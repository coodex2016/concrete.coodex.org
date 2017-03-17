/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * 
 */
package org.coodex.pojomocker;

/**
 * @author davidoff
 *
 */
public class POJOMockInfo {

   private int min = 1;
   private int max = 10;
   private int maxRecycledCount = 3;
   private POJOMock.MockType type = POJOMock.MockType.STRING_ZHCN;
   private String sizeOf = "";
   private boolean forceMock = true;
   private int[] arraySize = { -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
         5, 5 };

   private Class<? extends POJOMockerFactory> factory = DefaultPOJOMockerFactory.class;

   public POJOMockInfo() {
   }

   public POJOMockInfo(POJOMock pm) {
      if (pm != null) {
         this.min = pm.min();
         this.max = pm.max();
         this.setMaxRecycledCount(pm.maxRecycledCount());
         this.sizeOf = pm.sizeOf();
         this.forceMock = pm.forceMock();
         this.type = pm.type();
         this.factory = pm.factory();
      }
   }

   public Class<? extends POJOMockerFactory> getFactoryClass() {
      return factory;
   }

   public void setFactoryClass(Class<? extends POJOMockerFactory> factory) {
      this.factory = factory;
   }

   public int getMin() {
      return min;
   }

   public void setMin(int min) {
      this.min = min;
   }

   public int getMax() {
      return max;
   }

   public void setMax(int max) {
      this.max = max;
   }

   public POJOMock.MockType getType() {
      return type;
   }

   public void setType(POJOMock.MockType type) {
      this.type = type;
   }

   public String getSizeOf() {
      return sizeOf;
   }

   public void setSizeOf(String sizeOf) {
      this.sizeOf = sizeOf;
   }

   public boolean isForceMock() {
      return forceMock;
   }

   public void setForceMock(boolean forceMock) {
      this.forceMock = forceMock;
   }

   public int[] getArraySize() {
      return arraySize;
   }

   public void setArraySize(int[] arraySize) {
      this.arraySize = arraySize;
   }

   public int getMaxRecycledCount() {
      return maxRecycledCount;
   }

   public void setMaxRecycledCount(int maxRecycledCount) {
      this.maxRecycledCount = maxRecycledCount;
   }

}
