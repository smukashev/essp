package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

/**
 * @author alexandr.motov
 */
public abstract class MetaType extends Persistable implements IMetaType {

    protected MetaType() {
    }

    protected MetaType(long id) {
        super(id);
    }

}
