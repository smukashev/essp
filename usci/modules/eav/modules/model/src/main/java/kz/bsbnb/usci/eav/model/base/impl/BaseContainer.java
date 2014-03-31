package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public abstract class BaseContainer extends BaseObject implements IBaseContainer {

    public abstract class ValueChangeListener implements IValueChangeListener
    {
        private IBaseContainer target;
        private String identifier;

        public ValueChangeListener(IBaseContainer target, String identifier)
        {
            this.target = target;
            this.identifier = identifier;
        }

        public abstract void valueChange(ValueChangeEvent event);

        public String getIdentifier()
        {
            return identifier;
        }

        public IBaseContainer getTarget()
        {
            return target;
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

    private BaseContainerType baseContainerType;
    private Set<String> modifiedIdentifiers = new HashSet<String>();
    private boolean listening;

    public BaseContainer(BaseContainerType baseContainerType)
    {
        this.baseContainerType = baseContainerType;
    }

    public BaseContainer(long id, BaseContainerType baseContainerType)
    {
        super(id);

        this.baseContainerType = baseContainerType;
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

    public void setListening(boolean listening)
    {
        if (this.listening != listening)
        {
            if (listening)
            {
                setListeners();
            }
            else
            {
                removeListeners();
            }
        }

        this.listening = listening;
    }

    public boolean isListening()
    {
        return listening;
    }

    protected abstract void setListeners();

    protected abstract void removeListeners();

    @Override
    public void clearModifiedIdentifiers()
    {
        modifiedIdentifiers.clear();
    }

    @Override
    public Set<String> getModifiedIdentifiers()
    {
        return modifiedIdentifiers;
    }

    protected void addModifiedIdentifier(String identifier)
    {
        modifiedIdentifiers.add(identifier);
    }

    @Override
    public BaseContainerType getBaseContainerType() {
        return baseContainerType;
    }

    @Override
    public void setBaseContainerType(BaseContainerType baseContainerType) {
        this.baseContainerType = baseContainerType;
    }

}
