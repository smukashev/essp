package kz.bsbnb.usci.eav.model.output;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;

public class BaseEntityOutput
{
    public static String toString(BaseEntity entity)
    {
        return toString(entity, "");
    }

    public static String toString(BaseEntity entity, String prefix)
    {
        String str = "baseEntity;";
        MetaClass meta = entity.getMeta();

        for (String memberName : meta.getMemberNames())
        {
            IMetaAttribute attribute = meta.getMetaAttribute(memberName);
            IMetaType type = attribute.getMetaType();

            IBaseValue value = entity.getBaseValue(memberName);

            String valueToString = "null";

            if (value == null) {
                valueToString = "not set";
            } else {
                if (value.getValue() == null) {
                    valueToString = "null";
                }
            }


            if (value != null && value.getValue() != null) {
                if(type.isComplex()) {
                    if (!type.isSet()) {
                        valueToString = toString((BaseEntity)value.getValue(), prefix + "\t");
                    } else {
                        valueToString = complexSet((BaseSet)value.getValue(), prefix + "\t", (MetaSet)type);
                    }
                } else {
                    valueToString = value.getValue().toString();
                }
            }

            str += "\n" + prefix + memberName + ": " + " ( " + valueToString + " )";
        }

        return str;
    }

    private static String complexSet(BaseSet set, String prefix, MetaSet metaSet)
    {
        String str = "";

        for (IBaseValue value : set.get())
        {
            if (metaSet.isSet())
            {
                if (metaSet.isSetOfSets())
                {
                    str += complexSet((BaseSet)value.getValue(), prefix + "\t", (MetaSet)metaSet.getMemberType());
                } else if (metaSet.isComplex())
                {
                    str += "\n" + prefix + toString((BaseEntity)value.getValue(), prefix + "\t");
                }
                else
                {
                    str += value.getValue().toString();
                }
            }
        }

        return str;
    }
}
