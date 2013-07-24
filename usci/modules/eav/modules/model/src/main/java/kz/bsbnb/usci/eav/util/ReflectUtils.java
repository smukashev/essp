package kz.bsbnb.usci.eav.util;

import java.lang.reflect.Method;

/**
 *
 */
public class ReflectUtils {

    public static Method findMethod(Class<?> cls, String methodName,
                                    Class<?>... parameterTypes) throws ExceptionInInitializerError {
        try {
            return cls.getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
