package org.coodex.mock;

import org.coodex.util.Common;
import org.coodex.util.Singleton;

import java.lang.reflect.Type;

public class BooleanTypeMocker extends AbstractTypeMocker<Mock.Boolean> {
    static Class<?>[] SUPPORTED = new Class<?>[]{
            boolean.class, Boolean.class,//0,1
            byte.class, Byte.class,//2,3
            short.class, Short.class,//4,5
            int.class, Integer.class,//6,7
            long.class, Long.class,//8,9
            char.class, Character.class,//10,11
            String.class//12
    };

    private static Singleton<BooleanTypeMocker> instance = Singleton.with(BooleanTypeMocker::new);

//    public BooleanTypeMocker() {
//        instance = this;
//    }

    static Object mock(Class<?> c) {
//        if(instance == null){
//            instance = new BooleanTypeMocker();
//        }
        return instance.get().mock(null, null, c);
    }

    @Override
    protected boolean accept(Mock.Boolean annotation) {
        return true;
    }

    @Override
    protected Class<?>[] getSupportedClasses() {
        return SUPPORTED;
    }

    @Override
    public Object mock(Mock.Boolean mockAnnotation, Type targetType) {
        double probabilityOfTrue = mockAnnotation == null ? 0.5d : mockAnnotation.probabilityOfTrue();
        return toType(
                Math.random() < probabilityOfTrue,
                new BooleanMockConfig(mockAnnotation),
                targetType);
    }

    private Object toType(boolean b, BooleanMockConfig config, Type targetType) {
        Class<?> c = getClassFromType(targetType);
        int index = Common.indexOf(SUPPORTED,c);
        switch (index) {
            case 0:
            case 1:
                return b;
            case 2:
            case 3:
                return (byte) ((b ? config.intTrue : config.intFalse));
            case 4:
            case 5:
                return (short) ((b ? config.intTrue : config.intFalse));
            case 6:
            case 7:
                return (b ? config.intTrue : config.intFalse);
            case 8:
            case 9:
                return ((b ? config.intTrue : config.intFalse)) & 0xFFFFFFFFL;
            case 10:
            case 11:
                return b ? config.charTrue : config.charFalse;
            case 12:
                return b ? config.strTrue : config.strFalse;
            default:
                throw new MockException("Illegal type: " + targetType);
        }
    }

    private static class BooleanMockConfig {
        double probabilityOfTrue = 0.5d;

        int intTrue = 1;

        int intFalse = 0;

        char charTrue = 'T';

        char charFalse = 'F';

        String strTrue = "true";

        String strFalse = "false";

        BooleanMockConfig(Mock.Boolean annotation) {
            if (annotation != null) {
                probabilityOfTrue = annotation.probabilityOfTrue();
                intTrue = annotation.intTrue();
                intFalse = annotation.intFalse();
                charTrue = annotation.charTrue();
                charFalse = annotation.charFalse();
                strTrue = annotation.strTrue();
                strFalse = annotation.strFalse();
            }
        }
    }
}
