package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.lang.reflect.Method;

/**
 *
 */
public abstract class BaseContainer extends BaseObject implements IBaseContainer {

    public abstract class ValueChangeListener implements IValueChangeListener
    {

        String parentIdentifier;

        public ValueChangeListener(String parentIdentifier)
        {
            this.parentIdentifier = parentIdentifier;
        }

        public abstract void valueChange(ValueChangeEvent event);

        public String getParentIdentifier()
        {
            return parentIdentifier;
        }

    }

    private static final Method VALUE_CHANGE_METHOD;
    static
    {
        try
        {
            VALUE_CHANGE_METHOD = IValueChangeListener.class
                    .getDeclaredMethod("valueChange", new Class[] {  ValueChangeEvent.class });
        }
        catch (NoSuchMethodException ex)
        {
            throw new java.lang.RuntimeException("Internal error, value change method not found.");
        }
    }

    public BaseContainer()
    {

    }

    public BaseContainer(long id)
    {
        super(id);
    }

    @Override
    public void addListener(IValueChangeListener listener)
    {
        addListener(ValueChangeEvent.class, listener, VALUE_CHANGE_METHOD);
    }

    @Override
    public void removeListener(IValueChangeListener listener)
    {
        removeListener(ValueChangeEvent.class, listener, VALUE_CHANGE_METHOD);
    }

    protected void fireValueChange(String attribute)
    {
        fireEvent(new ValueChangeEvent(this, attribute));
    }

}
