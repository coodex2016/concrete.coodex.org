/**
 * 
 */
package cc.coodex.pojomocker;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author davidoff
 *
 */
class POJOMockerImpl {

   private static final AbstractTypeBasedMocker<? extends Type> typeVariableMocker = new TypeVariableMocker();
   private static final AbstractTypeBasedMocker<? extends Type> parameterizedTypeMocker = new ParameterizedTypeMocker();
   private static final AbstractTypeBasedMocker<? extends Type> genericArrayMocker = new GenericArrayTypeMocker();
   private static final AbstractTypeBasedMocker<? extends Type> classMocker = new ClassTypeMocker();

   private final Map<Class<? extends POJOMockerFactory>, POJOMockerFactory> factorys = new HashMap<Class<? extends POJOMockerFactory>, POJOMockerFactory>();

   private AbstractTypeBasedMocker<? extends Type> getTypeMocker(Type type)
         throws UnsupportedTypeException {
      if (type instanceof Class)
         return classMocker;
      else if (type instanceof TypeVariable)
         return typeVariableMocker;
      else if (type instanceof ParameterizedType)
         return parameterizedTypeMocker;
      else if (type instanceof GenericArrayType)
         return genericArrayMocker;
      else
         throw new UnsupportedTypeException(type);
   }

   /**
    * 获取缓存Mocker工厂
    * 
    * @param clz
    * @return
    */
   synchronized POJOMockerFactory getFactory(
         Class<? extends POJOMockerFactory> clz) {
      POJOMockerFactory factory = factorys.get(clz);
      if (factory == null) {
         try {
            factory = clz.newInstance();
            factorys.put(clz, factory);
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }
      return factory;
   }

   /**
    * 根据指定的类型和模拟条件模拟数据
    * 
    * @param type
    * @param pmi
    * @return
    * @throws UnableMockException
    * @throws UnsupportedTypeException
    * @throws IllegalAccessException
    */
   public Object mock(Type type, POJOMockInfo pmi, Type contextClass)
         throws UnableMockException, UnsupportedTypeException,
         IllegalAccessException {

      if (pmi == null)
         pmi = new POJOMockInfo();

      MockContextHelper.enter();
      try {

         MockContext context = MockContextHelper.currentContext();
         if (contextClass != null)
            context.addContextType(contextClass);
         context.setMockInfo(pmi);
         return $mock(type, context);

      } finally {
         MockContextHelper.leave();
      }
   }

   /**
    * 根据上下文mock指定的type实例
    * 
    * @param type
    * @param context
    * @return
    * @throws UnsupportedTypeException
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   Object $mock(Type type, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {
      POJOMocker.assertNull(type, "type is Null.");
      POJOMocker.assertNull(context, "mockContext is Null.");

      return getTypeMocker(type).mock(type, context);
   }

}
