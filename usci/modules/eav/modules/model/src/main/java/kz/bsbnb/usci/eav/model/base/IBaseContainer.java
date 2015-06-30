package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.base.impl.BaseContainerType;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainer;
import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer extends IBaseObject {

    class ValueChangeEvent extends IBaseObject.Event {
        private String identifier;

        public ValueChangeEvent(IBaseContainer source, String identifier) {
            super(source);
            this.identifier = identifier;
        }

        public IBaseContainer getSource() {
            return (IBaseContainer) super.getSource();
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    interface IValueChangeListener {
        void valueChange(ValueChangeEvent event);
    }

    void addListener(IValueChangeListener listener);

    void removeListener(IValueChangeListener listener);

    void setListening(boolean listening);

    boolean isListening();

    Set<String> getAttributes();

    void put(String identifier, IBaseValue value);

    Collection<IBaseValue> get();

    IBaseValue getBaseValue(String identifier);

    IMetaType getMemberType(String identifier);

    void clearModifiedIdentifiers();

    Set<String> getModifiedIdentifiers();

    boolean isSet();

    int getValueCount();

    BaseContainerType getBaseContainerType();

    void setBaseContainerType(BaseContainerType baseContainerType);

}
