package kz.bsbnb.usci.eav.model.persistable;

import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.io.Serializable;
import java.util.EventListener;
import java.util.EventObject;

/**
 * @author a.motov
 */
public interface IBaseObject extends IPersistable, Serializable, Cloneable {
}
