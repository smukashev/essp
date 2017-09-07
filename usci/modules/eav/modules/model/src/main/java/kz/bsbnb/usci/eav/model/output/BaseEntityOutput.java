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
    protected final boolean PRINT_CLOSE = false;

    public static String toString(BaseEntity entity) {
        BaseEntityOutput output = new BaseEntityOutput();
        return output.toString("", entity, "");
    }

    private String complexSet(String suffix, BaseSet set, String prefix, MetaSet metaSet) {
        String str = "";
        //str += suffix;
        for (IBaseValue value : set.get()) {
            if (metaSet.isSet()) {
                if (metaSet.isComplex()) {
                    //str += " [ComSet]";
                    str += "\n" + prefix + toString(suffix(value), (BaseEntity) value.getValue(), prefix + "\t");
                } else {
                    //str += " [SimSet]";
                    str += suffix(value) + " " + value.getValue().toString();
                }
            }
        }

        return str;
    }

    public String toString(String suffix, BaseEntity entity, String prefix) {

        if (entity == null) return "null";

        String str = entity.getMeta().getClassName() + "(" + entity.getId() + ", ";
        if (entity.getMeta().isSet()) {
            if (entity.getMeta().isComplex())
                str += "[ComSet] ";
            else
                str += "[SimSet] ";
        } else {
            if (entity.getMeta().isComplex())
                str += "[ComAtr] ";
            else
                str += "[SimAtr] ";
        }
        try {
            str += "/RP " + (entity.getReportDate() == null ? "NO/" : DataTypes.formatDate(entity.getReportDate())) + "/";
            str += suffix == null ? "-)" : " " + suffix;
            str += ");";
        } catch (Exception e) {
            if (entity.getMeta().getClassName().equals("credit")) {
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

            if (value == null) {
                valueToString = "not set";
            } else {
                if (value.getValue() == null) {
                    valueToString = "null";
                }
            }

            String chSuffix = suffix(value);

            if (value != null && value.getValue() != null) {
                if (type.isComplex()) {
                    if (!type.isSet()) {
                        valueToString = "";
                        valueToString += toString(chSuffix, (BaseEntity) value.getValue(), prefix + "\t");
                    } else {
                        valueToString = "";
                        valueToString += complexSet(chSuffix, (BaseSet) value.getValue(), prefix + "\t", (MetaSet) type);
                    }
                } else {
                    valueToString = "";
                    valueToString += value.getValue().toString();
                }
            }

            if (value != null) {
                if (attribute.isKey() || attribute.isOptionalKey()) {
                    str += "\n" + prefix + memberName + " : ";
                } else {
                    str += "\n" + prefix + memberName + " : ";
                }

                if (type.isSet())
                    str += value.getId() + " : ";

                if (value.getMetaAttribute().getMetaType().isSet()) {
                    if (value.getMetaAttribute().getMetaType().isComplex())
                        str += "[ComSet] ";
                    else
                        str += "[SimSet] ";
                } else {
                    if (value.getMetaAttribute().getMetaType().isComplex())
                        str += "[ComAtr] ";
                    else
                        str += "[SimAtr] ";
                }

                str += "/RP " + DataTypes.formatDate(value.getRepDate()) + "/ " + chSuffix + " : " + valueToString;
            }
        }

        return str;
    }

    private String suffix(IBaseValue value) {
        if (!PRINT_CLOSE) return "";
        if (value == null) return "";
        return " /CL " + (value.getCloseDate() != null ? "" + DataTypes.formatDate(value.getCloseDate()) : "NO ") + Boolean.toString(value.isClosed()) + "/";
    }

}



