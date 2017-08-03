import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public class C  extends A{
//
//    @Rule
//    public final B b = new B();

    public static void main(String [] args) throws InvocationTargetException, IllegalAccessException {
        D d = new D();
        for(Method method : I2.class.getMethods()){
            System.out.println(String.format("%s, %d, %s", method.getName(), method.getModifiers(), method.getDeclaringClass().getName()));
            method.invoke(d);
        }
    }
}
