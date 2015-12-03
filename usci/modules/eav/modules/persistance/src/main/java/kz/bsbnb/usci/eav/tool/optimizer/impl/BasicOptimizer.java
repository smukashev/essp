package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.tool.optimizer.IEavOptimizer;

import java.util.TreeSet;

public class BasicOptimizer implements IEavOptimizer {
    @Override
    public String getKeyString(IBaseEntity iBaseEntity) {
        MetaClass meta = iBaseEntity.getMeta();

        switch (meta.getClassName()) {
            case "subject":
                return new SubjectOptimizer().getKeyString(iBaseEntity);

            default:
                StringBuilder stringBuilder = new StringBuilder();

                for (String name : new TreeSet<>(meta.getAttributeNames())) {
                    IMetaAttribute metaAttribute = meta.getMetaAttribute(name);
                    IMetaType metaType = metaAttribute.getMetaType();

                    if (metaAttribute.isKey()) {
                        IBaseValue baseValue = iBaseEntity.getBaseValue(name);

                        if (metaType.isSet()) {
                            throw new IllegalStateException("Не реализован ключевой оптимизатор для массивов; " +
                                    "\n" + name);
                        } else {
                            if(stringBuilder.length() > 0)
                                stringBuilder.append("-");

                            if (metaType.isComplex()) {
                                stringBuilder.append(new BasicOptimizer().getKeyString((IBaseEntity)
                                        baseValue.getValue()));
                            } else {
                                stringBuilder.append(name).append("=").append(baseValue.getValue());
                            }
                        }
                    }
                }

                if (stringBuilder.length() == 0)
                    throw new IllegalStateException("Не найдены ключевые атрибуты; \n" + iBaseEntity);

                return stringBuilder.toString();
        }
    }
}
