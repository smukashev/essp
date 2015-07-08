package kz.bsbnb.usci.eav.event.impl;

import kz.bsbnb.usci.eav.event.IMethodEventSource;

import java.lang.reflect.Method;
import java.util.*;

public class EventRouter implements IMethodEventSource {

    private LinkedHashSet<ListenerMethod> listenerList = null;

    public void addListener(Class<?> eventType, Object object, Method method) {
        if (listenerList == null) {
            listenerList = new LinkedHashSet<>();
        }
        listenerList.add(new ListenerMethod(eventType, object, method));
    }

    public void addListener(Class<?> eventType, Object object, String methodName) {
        if (listenerList == null) {
            listenerList = new LinkedHashSet<>();
        }
        listenerList.add(new ListenerMethod(eventType, object, methodName));
    }

    public void removeListener(Class<?> eventType, Object target) {
        if (listenerList != null) {
            final Iterator<ListenerMethod> i = listenerList.iterator();
            while (i.hasNext()) {
                final ListenerMethod lm = i.next();
                if (lm.matches(eventType, target)) {
                    i.remove();
                    return;
                }
            }
        }
    }

    public void removeListener(Class<?> eventType, Object target, Method method) {
        if (listenerList != null) {
            final Iterator<ListenerMethod> i = listenerList.iterator();
            while (i.hasNext()) {
                final ListenerMethod lm = i.next();
                if (lm.matches(eventType, target, method)) {
                    i.remove();
                    return;
                }
            }
        }
    }

    public void removeListener(Class<?> eventType, Object target, String methodName) {
        final Method[] methods = target.getClass().getMethods();
        Method method = null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                method = methods[i];
            }
        }
        if (method == null) {
            throw new IllegalArgumentException();
        }

        if (listenerList != null) {
            final Iterator<ListenerMethod> i = listenerList.iterator();
            while (i.hasNext()) {
                final ListenerMethod lm = i.next();
                if (lm.matches(eventType, target, method)) {
                    i.remove();
                    return;
                }
            }
        }

    }

    public void removeAllListeners() {
        listenerList = null;
    }

    public void fireEvent(EventObject event) {
        if (listenerList != null) {
            final Object[] listeners = listenerList.toArray();
            for (int i = 0; i < listeners.length; i++) {
                ((ListenerMethod) listeners[i]).receiveEvent(event);
            }

        }
    }

    public boolean hasListeners(Class<?> eventType) {
        if (listenerList != null) {
            for (ListenerMethod lm : listenerList) {
                if (lm.isType(eventType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<?> getListeners(Class<?> eventType) {
        List<Object> listeners = new ArrayList<Object>();
        if (listenerList != null) {
            for (ListenerMethod lm : listenerList) {
                if (lm.isOrExtendsType(eventType)) {
                    listeners.add(lm.getTarget());
                }
            }
        }
        return listeners;
    }
}
