package kz.bsbnb.usci.eav.model.persistable.impl;

import kz.bsbnb.usci.eav.event.IMethodEventSource;
import kz.bsbnb.usci.eav.event.impl.EventRouter;
import kz.bsbnb.usci.eav.model.persistable.IBaseObject;
import kz.bsbnb.usci.eav.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class BaseObject extends Persistable
        implements IBaseObject, IMethodEventSource {

    private static final Method BASE_OBJECT_EVENT_METHOD = ReflectUtils
            .findMethod(IBaseObject.Listener.class, "baseObjectEvent", IBaseObject.Event.class);

    private EventRouter eventRouter = null;

    public BaseObject() {
    }

    public BaseObject(long id) {
        super(id);
    }

    public void addListener(Class<?> eventType, Object target, Method method) {
        if (eventRouter == null) {
            eventRouter = new EventRouter();
        }
        eventRouter.addListener(eventType, target, method);
    }

    public void addListener(Class<?> eventType, Object target, String methodName) {
        if (eventRouter == null) {
            eventRouter = new EventRouter();
        }
        eventRouter.addListener(eventType, target, methodName);
    }

    public void removeListener(Class<?> eventType, Object target) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target);
        }
    }

    public void removeListener(Class<?> eventType, Object target, Method method) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target, method);
        }
    }

    public void removeListener(Class<?> eventType, Object target, String methodName) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target, methodName);
        }
    }

    public void addListener(IBaseObject.Listener listener) {
        addListener(IBaseObject.Event.class, listener, BASE_OBJECT_EVENT_METHOD);
    }

    public void removeListener(IBaseObject.Listener listener) {
        removeListener(IBaseObject.Event.class, listener, BASE_OBJECT_EVENT_METHOD);
    }

    protected void fireBaseObjectEvent() {
        fireEvent(new IBaseObject.Event(this));
    }

    protected void fireEvent(IBaseObject.Event event) {
        if (eventRouter != null) {
            eventRouter.fireEvent(event);
        }
    }

    public Collection<?> getListeners(Class<?> eventType)
    {
        if (eventRouter != null)
        {
            return eventRouter.getListeners(eventType);
        }
        return new ArrayList<Object>();
    }

}
