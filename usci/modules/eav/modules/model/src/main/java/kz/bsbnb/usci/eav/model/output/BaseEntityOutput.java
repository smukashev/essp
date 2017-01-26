package kz.bsbnb.usci.eav.model.output;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.type.DataTypes;

public class BaseEntityOutput {
    public static String toString(BaseEntity entity) {
        return toString(entity, "");
    }

    public static String toString(BaseEntity entity, String prefix) {
        if (entity == null) return "null";

        String str = entity.getMeta().getClassName() + "(" + entity.getId() + ", ";
        try {
            str += entity.getReportDate() == null ? "-)" : DataTypes.formatDate(entity.getReportDate()) + ");";
        } catch (Exception e) {
            if(entity.getMeta().getClassName().equals("credit")) {
                System.out.println(entity.getEl("primary_contract.no"));
                System.out.println(entity.getEl("primary_contract.date"));
            }
            System.out.println(entity);
            System.out.println(entity.getReportDate());
            throw e;
        }

        MetaClass meta = entity.getMeta();

        for (String memberName : meta.getMemberNames()) {
            IMetaAttribute attribute = meta.getMetaAttribute(memberName);
            IMetaType type = attribute.getMetaType();

            if (meta.isReference() && !(attribute.isKey() || attribute.isOptionalKey()))
                continue;

            IBaseValue value = entity.getBaseValue(memberName);

            String valueToString = "null";
            boolean valueIsNull = false;

            if (value == null) {
                valueToString = "not set";
                valueIsNull = true;
            } else {
                if (value.getValue() == null) {
                    valueToString = "null";
                }
            }

            if (value != null && value.getValue() != null) {
                if (type.isComplex()) {
                    if (!type.isSet()) {
                        valueToString = toString((BaseEntity) value.getValue(), prefix + "\t");
                    } else {
                        valueToString = complexSet((BaseSet) value.getValue(), prefix + "\t", (MetaSet) type);
                    }
                } else {
                    valueToString = value.getValue().toString();
                }
            }

            if (!valueIsNull) {
                if (attribute.isKey() || attribute.isOptionalKey()) {
                    str += "\n" + prefix + "\u001B[1m" + memberName + "\u001B[0m" + " : ";
                } else {
                    str += "\n" + prefix + memberName + " : ";
                }

                if (type.isSet())
                    str += value.getId() + " : ";

                    str += DataTypes.formatDate(value.getRepDate()) + " : " + valueToString;
            }
        }

        return str;
    }

    private static String complexSet(BaseSet set, String prefix, MetaSet metaSet) {
        String str = "";

        for (IBaseValue value : set.get()) {
            if (metaSet.isSet()) {
                if (metaSet.isComplex()) {
                    str += "\n" + prefix + toString((BaseEntity) value.getValue(), prefix + "\t");
                } else {
                    str += value.getValue().toString();
                }
            }
        }

        return str;
    }
}
