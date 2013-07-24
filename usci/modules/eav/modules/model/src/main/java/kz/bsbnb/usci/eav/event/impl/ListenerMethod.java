package kz.bsbnb.usci.eav.event.impl;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ListenerMethod implements EventListener, Serializable {

    private static final Logger logger = Logger.getLogger(ListenerMethod.class.getName());

    private final Class<?> eventType;

    private final Object target;

    private transient Method method;

    private Object[] arguments;

    private int eventArgumentIndex;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        try {
            out.defaultWriteObject();
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            out.writeObject(name);
            out.writeObject(paramTypes);
        } catch (NotSerializableException e) {
            logger.warning("Error in serialization of the application: Class "
                    + target.getClass().getName()
                    + " must implement serialization.");
            throw e;
        }

    };

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        try {
            String name = (String) in.readObject();
            Class<?>[] paramTypes = (Class<?>[]) in.readObject();
            method = findHighestMethod(target.getClass(), name, paramTypes);
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "Internal deserialization error", e);
        }
    };

    private static Method findHighestMethod(Class<?> cls, String method,
                                            Class<?>[] paramTypes) {
        Class<?>[] ifaces = cls.getInterfaces();
        for (int i = 0; i < ifaces.length; i++) {
            Method ifaceMethod = findHighestMethod(ifaces[i], method,
                    paramTypes);
            if (ifaceMethod != null) {
                return ifaceMethod;
            }
        }
        if (cls.getSuperclass() != null) {
            Method parentMethod = findHighestMethod(cls.getSuperclass(),
                    method, paramTypes);
            if (parentMethod != null) {
                return parentMethod;
            }
        }
        Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(method)) {
                return methods[i];
            }
        }
        return null;
    }

    public ListenerMethod(Class<?> eventType, Object target, Method method,
                          Object[] arguments, int eventArgumentIndex)
            throws java.lang.IllegalArgumentException {

        // Checks that the object is of correct type
        if (!method.getDeclaringClass().isAssignableFrom(target.getClass())) {
            throw new java.lang.IllegalArgumentException();
        }

        // Checks that the event argument is null
        if (eventArgumentIndex >= 0 && arguments[eventArgumentIndex] != null) {
            throw new java.lang.IllegalArgumentException();
        }

        // Checks the event type is supported by the method
        if (eventArgumentIndex >= 0
                && !method.getParameterTypes()[eventArgumentIndex]
                .isAssignableFrom(eventType)) {
            throw new java.lang.IllegalArgumentException();
        }

        this.eventType = eventType;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.eventArgumentIndex = eventArgumentIndex;
    }

    public ListenerMethod(Class<?> eventType, Object target, String methodName,
                          Object[] arguments, int eventArgumentIndex)
            throws java.lang.IllegalArgumentException {

        // Finds the correct method
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

        // Checks that the event argument is null
        if (eventArgumentIndex >= 0 && arguments[eventArgumentIndex] != null) {
            throw new java.lang.IllegalArgumentException();
        }

        // Checks the event type is supported by the method
        if (eventArgumentIndex >= 0
                && !method.getParameterTypes()[eventArgumentIndex]
                .isAssignableFrom(eventType)) {
            throw new java.lang.IllegalArgumentException();
        }

        this.eventType = eventType;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.eventArgumentIndex = eventArgumentIndex;
    }

    public ListenerMethod(Class<?> eventType, Object target, Method method,
                          Object[] arguments) throws java.lang.IllegalArgumentException {

        // Check that the object is of correct type
        if (!method.getDeclaringClass().isAssignableFrom(target.getClass())) {
            throw new java.lang.IllegalArgumentException();
        }

        this.eventType = eventType;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        eventArgumentIndex = -1;
    }

    public ListenerMethod(Class<?> eventType, Object target, String methodName,
                          Object[] arguments) throws java.lang.IllegalArgumentException {

        // Find the correct method
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

        this.eventType = eventType;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        eventArgumentIndex = -1;
    }

    public ListenerMethod(Class<?> eventType, Object target, Method method)
            throws java.lang.IllegalArgumentException {

        // Checks that the object is of correct type
        if (!method.getDeclaringClass().isAssignableFrom(target.getClass())) {
            throw new java.lang.IllegalArgumentException();
        }

        this.eventType = eventType;
        this.target = target;
        this.method = method;
        eventArgumentIndex = -1;

        final Class<?>[] params = method.getParameterTypes();

        if (params.length == 0) {
            arguments = new Object[0];
        } else if (params.length == 1 && params[0].isAssignableFrom(eventType)) {
            arguments = new Object[] { null };
            eventArgumentIndex = 0;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public ListenerMethod(Class<?> eventType, Object target, String methodName)
            throws java.lang.IllegalArgumentException {

        // Finds the correct method
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

        this.eventType = eventType;
        this.target = target;
        this.method = method;
        eventArgumentIndex = -1;

        final Class<?>[] params = method.getParameterTypes();

        if (params.length == 0) {
            arguments = new Object[0];
        } else if (params.length == 1 && params[0].isAssignableFrom(eventType)) {
            arguments = new Object[] { null };
            eventArgumentIndex = 0;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void receiveEvent(EventObject event) {
        // Only send events supported by the method
        if (eventType.isAssignableFrom(event.getClass())) {
            try {
                if (eventArgumentIndex >= 0) {
                    if (eventArgumentIndex == 0 && arguments.length == 1) {
                        method.invoke(target, new Object[] { event });
                    } else {
                        final Object[] arg = new Object[arguments.length];
                        for (int i = 0; i < arg.length; i++) {
                            arg[i] = arguments[i];
                        }
                        arg[eventArgumentIndex] = event;
                        method.invoke(target, arg);
                    }
                } else {
                    method.invoke(target, arguments);
                }

            } catch (final java.lang.IllegalAccessException e) {
                // This should never happen
                throw new java.lang.RuntimeException(
                        "Internal error - please report", e);
            } catch (final java.lang.reflect.InvocationTargetException e) {
                // An exception was thrown by the invocation target. Throw it
                // forwards.
                throw new MethodException("Invocation of method " + method
                        + " failed.", e.getTargetException());
            }
        }
    }

    public boolean matches(Class<?> eventType, Object target) {
        return (this.target == target) && (eventType.equals(this.eventType));
    }

    public boolean matches(Class<?> eventType, Object target, Method method) {
        return (this.target == target)
                && (eventType.equals(this.eventType) && method
                .equals(this.method));
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 31 * hash + eventArgumentIndex;
        hash = 31 * hash + (eventType == null ? 0 : eventType.hashCode());
        hash = 31 * hash + (target == null ? 0 : target.hashCode());
        hash = 31 * hash + (method == null ? 0 : method.hashCode());

        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        // return false if obj is a subclass (do not use instanceof check)
        if ((obj == null) || (obj.getClass() != getClass())) {
            return false;
        }

        // obj is of same class, test it further
        ListenerMethod t = (ListenerMethod) obj;

        return eventArgumentIndex == t.eventArgumentIndex
                && (eventType == t.eventType || (eventType != null && eventType
                .equals(t.eventType)))
                && (target == t.target || (target != null && target
                .equals(t.target)))
                && (method == t.method || (method != null && method
                .equals(t.method)))
                && (arguments == t.arguments || (Arrays.equals(arguments,
                t.arguments)));
    }

    public class MethodException extends RuntimeException implements
            Serializable {

        private final Throwable cause;

        private String message;

        private MethodException(String message, Throwable cause) {
            super(message);
            this.cause = cause;
        }

        @Override
        public Throwable getCause() {
            return cause;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            String msg = super.toString();
            if (cause != null) {
                msg += "\nCause: " + cause.toString();
            }
            return msg;
        }

    }

    public boolean isType(Class<?> eventType) {
        return this.eventType == eventType;
    }

    public boolean isOrExtendsType(Class<?> eventType) {
        return eventType.isAssignableFrom(this.eventType);
    }

    public Object getTarget() {
        return target;
    }

}
