package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer extends IBaseObject, Serializable
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

    public void put(String name, IBaseValue value);

    public Collection<IBaseValue> get();

    public IMetaType getMemberType(String name);

    public void setListeners();

    public void removeListeners();

    public Set<String> getModifiedObjects();

}
