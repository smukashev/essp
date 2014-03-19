package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

/**
 * Created by Alexandr.Motov on 18.03.14.
 */
public abstract class MetaType extends Persistable implements IMetaType {

    protected MetaType() {
    }

    protected MetaType(long id) {
        super(id);
    }

}
