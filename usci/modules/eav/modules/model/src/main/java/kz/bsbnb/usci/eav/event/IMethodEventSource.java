package kz.bsbnb.usci.eav.event;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author a.motov
 */
public interface IMethodEventSource extends Serializable {

    public void addListener(Class<?> eventType, Object object, Method method);

    public void addListener(Class<?> eventType, Object object, String methodName);

    public void removeListener(Class<?> eventType, Object target);

    public void removeListener(Class<?> eventType, Object target, Method method);

    public void removeListener(Class<?> eventType, Object target, String methodName);
}
