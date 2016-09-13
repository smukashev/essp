package kz.bsbnb.usci.eav.model.exceptions;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.util.Errors;

/**
 * Created by bauka on 9/9/16.
 */
public class ImmutableElementException extends KnownException {

    String message;

    public ImmutableElementException(IBaseEntity immutableEntity){
        super();

        MetaClass meta = immutableEntity.getMeta();

        message = Errors.compose(Errors.E62, meta.getClassName());

        for (String attribute : immutableEntity.getAttributes()) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if(!metaType.isComplex() && metaAttribute.isKey()) {
                IBaseValue baseValue = immutableEntity.getBaseValue(attribute);

                if(baseValue == null || baseValue.getValue() == null)
                    continue;

                message = Errors.compose(Errors.E295, baseValue.getValue(), meta.getClassTitle());
                break;
            }
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
