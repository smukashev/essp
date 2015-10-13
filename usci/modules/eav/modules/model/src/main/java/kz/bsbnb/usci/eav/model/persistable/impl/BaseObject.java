package kz.bsbnb.usci.eav.model.persistable.impl;

import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

public class BaseObject extends Persistable implements IBaseObject {
    public BaseObject() {
    }

    public BaseObject(long id) {
        super(id);
    }
}
