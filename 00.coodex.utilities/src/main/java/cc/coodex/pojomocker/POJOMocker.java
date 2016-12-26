/**
 *
 */
package cc.coodex.pojomocker;

//import java.lang.reflect.Array;
//import java.lang.reflect.GenericArrayType;
//import java.lang.reflect.GenericDeclaration;
//import java.lang.reflect.ParameterizedType;

import java.lang.reflect.Type;
//import java.lang.reflect.TypeVariable;
//import java.util.Stack;

//import org.coodex.pojomock.refactoring.exceptions.NoActualClassFoundException;
//import org.coodex.pojomock.refactoring.exceptions.TypeVariableNotInClassException;

/**
 * @author davidoff
 */
public class POJOMocker {

    private static final POJOMockerImpl mocker = new POJOMockerImpl();

    static void assertNull(Object obj, String msg) {
        if (obj == null)
            throw new RuntimeException(msg);
    }

    // /**
    // * 根据已实例化的Class定义，获得type的实际类型<br>
    // *
    // * @param type
    // * @param instancedClass
    // * @return
    // * @throws NoActualClassFoundException
    // */
    // public static Class<?> getActualClass(Type type, Class<?> instancedClass)
    // throws NoActualClassFoundException {
    // if (type instanceof Class) {
    // return (Class<?>) type;
    //
    // } else if (type instanceof ParameterizedType) {
    // return getActualClass(((ParameterizedType) type).getRawType(),
    // instancedClass);
    //
    // } else if (type instanceof TypeVariable) {
    // //
    // TypeVariable<?> tv = (TypeVariable<?>) type;
    // Class<?> declaringClass = getDeclaringClass(tv.getGenericDeclaration());
    // if (declaringClass == null)
    // throw new TypeVariableNotInClassException(tv);
    //
    // // 检索type的索引
    // int variableIndex = findTypeParameterIndex(tv, declaringClass);
    //
    // // 从已实例化的class instance中找到declaringClass的泛型类型
    // Type sub = instancedClass.getGenericSuperclass();
    //
    // while (declaringClass != sub && sub != null) {
    // if (sub instanceof Class) {
    // Class<?> rawType = (Class<?>) sub;
    // sub = rawType.getGenericSuperclass();
    // } else if (sub instanceof ParameterizedType) {
    // ParameterizedType pt = (ParameterizedType) sub;
    // if (pt.getRawType() == declaringClass) {
    //
    // } else
    // sub = pt.getRawType();
    // } else
    // throw new NoActualClassFoundException(type, instancedClass);
    // }
    //
    // return null;
    //
    // } else if (type instanceof GenericArrayType) {
    //
    // Class<?> componentClass = getActualClass(
    // ((GenericArrayType) type).getGenericComponentType(),
    // instancedClass);
    // return Array.newInstance(componentClass, 0).getClass();
    //
    // } else
    // throw new NoActualClassFoundException(type, instancedClass);
    // }
    //
    // private static int findTypeParameterIndex(TypeVariable<?> type,
    // Class<?> declaringClass) throws TypeVariableNotInClassException {
    // TypeVariable<?>[] parameters = declaringClass.getTypeParameters();
    // for (int i = 0; i < parameters.length; i++) {
    // if (type == parameters[i])
    // return i;
    // }
    // throw new TypeVariableNotInClassException(type);
    // }

    // private static Class<?> getDeclaringClass(GenericDeclaration gd) {
    // if (gd instanceof Class)
    // return (Class<?>) gd;
    // else
    // // 因为Constructor和Method上定义的泛型变量并非在类实例化是确定，因此不能处理
    // // if (gd instanceof Constructor)
    // // return ((Constructor) gd).getDeclaringClass();
    // // else if (gd instanceof Method)
    // // return ((Method) gd).getDeclaringClass();
    // // else
    // return null;
    //
    // }

    @SuppressWarnings("unchecked")
    public static <C> C mock(Class<C> clz) throws UnableMockException, UnsupportedTypeException {
        return (C)mock(clz, null);
    }
    /**
     * 根据指定的类型模拟数据
     *
     * @param type
     * @return
     * @throws IllegalAccessException
     * @throws MockException
     */
    public static Object mock(Type type) throws UnableMockException,
            UnsupportedTypeException {
        return mock(type, null);
    }

    public static Object mock(Type type, Class<?> contextClass)
            throws UnableMockException, UnsupportedTypeException {
        return mock(type, null, contextClass);
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
    public static Object mock(Type type, POJOMockInfo pmi, Class<?> contextClass)
            throws UnableMockException, UnsupportedTypeException {
        if (pmi == null)
            pmi = new POJOMockInfo();

        try {
            return mocker.mock(type, pmi, contextClass);
        } catch (UnsupportedTypeException e) {
            throw e;
        } catch (UnableMockException e) {
            throw e;
        } catch (Throwable th) {
            throw new UnableMockException(th);
        }
    }

    /**
     * 向系统中注册一个Class的mocker
     *
     * @param classInstanceMocker
     */
    public static void registMocker(
            AbstractClassInstanceMocker classInstanceMocker) {
        POJOMockerFactory.registGlobalMocker(classInstanceMocker);
    }

    static POJOMockerFactory getFactory(Class<? extends POJOMockerFactory> clz) {
        return mocker.getFactory(clz);
    }

    static Object $mock(Type type) throws UnsupportedTypeException,
            IllegalAccessException, IllegalArgumentException, UnableMockException {
        return mocker.$mock(type, MockContextHelper.currentContext());
    }

}
