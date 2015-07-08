package kz.bsbnb.usci.eav.event;

import java.io.Serializable;
import java.lang.reflect.Method;

public interface IMethodEventSource extends Serializable {
    void addListener(Class<?> eventType, Object object, Method method);

    void addListener(Class<?> eventType, Object object, String methodName);

    void removeListener(Class<?> eventType, Object target);

    void removeListener(Class<?> eventType, Object target, Method method);

    void removeListener(Class<?> eventType, Object target, String methodName);
}
