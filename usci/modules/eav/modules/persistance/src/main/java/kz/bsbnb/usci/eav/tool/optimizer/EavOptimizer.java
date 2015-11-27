package kz.bsbnb.usci.eav.tool.optimizer;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

public class EavOptimizer {
    public String getOptimizerKey(BaseEntity baseEntity) {
        StringBuilder keyBuiler = new StringBuilder();

        MetaClass meta = baseEntity.getMeta();

        for (String name : meta.getAttributeNames()) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(name);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaAttribute.isKey()) {
                if (metaType.isSet()) {

                } else {

                }
            }
        }

        return keyBuiler.toString();
    }
}
