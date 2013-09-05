package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer extends IBaseObject
{

    public class ValueChangeEvent extends IBaseObject.Event
    {
        private String identifier;

        public ValueChangeEvent(IBaseContainer source, String identifier)
        {
            super(source);
            this.identifier = identifier;
        }

        public IBaseContainer getSource()
        {
            return (IBaseContainer)super.getSource();
        }

        public String getIdentifier()
        {
            return identifier;
        }
    }

    public interface IValueChangeListener
    {

        public void valueChange(ValueChangeEvent event);

    }

    public void addListener(IValueChangeListener listener);

    public void removeListener(IValueChangeListener listener);

    public void setListening(boolean listening);

    public boolean isListening();

    public Set<String> getIdentifiers();

    public void put(String identifier, IBaseValue value);

    public Collection<IBaseValue> get();

    public IBaseValue getBaseValue(String identifier);

    public IMetaType getMemberType(String identifier);

    public void clearModifiedIdentifiers();

    public Set<String> getModifiedIdentifiers();

}
