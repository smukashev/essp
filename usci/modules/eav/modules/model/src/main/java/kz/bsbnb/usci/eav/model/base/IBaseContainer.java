package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

import java.io.Serializable;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer extends Serializable
{

    public class AttributeChangeEvent extends IBaseObject.Event
    {
        private String attribute;

        public AttributeChangeEvent(IBaseObject source, String attribute)
        {
            super(source);
            this.attribute = attribute;
        }

        public IBaseContainer getBaseContainer()
        {
            return (IBaseContainer)getBaseObject();
        }

        public String getAttribute()
        {
            return attribute;
        }
    }

    public interface AttributeChangeListener
    {

        public void attributeChange(AttributeChangeEvent event);

    }

    public void addListener(AttributeChangeListener listener);

    public void removeListener(AttributeChangeListener listener);

    public void put(String name, IBaseValue value);

    public Set<IBaseValue> get();

    public IMetaType getMemberType(String name);
}
