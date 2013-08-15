package kz.bsbnb.usci.eav.model.persistable;

import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.io.Serializable;
import java.util.EventListener;
import java.util.EventObject;

/**
 * @author a.motov
 */
public interface IBaseObject extends IPersistable, Serializable {

    public class Event extends EventObject
    {
        public Event(IBaseObject source)
        {
            super(source);
        }

        public IBaseObject getSource()
        {
            return (IBaseObject) super.getSource();
        }
    }

    public interface Listener extends EventListener, Serializable
    {
        public void baseObjectEvent(Event event);
    }

    public void addListener(IBaseObject.Listener listener);

    public void removeListener(IBaseObject.Listener listener);

}
