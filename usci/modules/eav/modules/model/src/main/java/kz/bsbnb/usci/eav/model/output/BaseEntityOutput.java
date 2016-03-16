package kz.bsbnb.usci.eav.model.output;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BaseEntityOutput {
    protected static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public static String toString(BaseEntity entity) {
        return toString(entity, "");
    }

    public static String toString(BaseEntity entity, String prefix) {
        String str = entity.getMeta().getClassName() + "(" + entity.getId() + ", ";
        str += entity.getReportDate() == null ? "-)" : dateFormat.format(entity.getReportDate()) + ");";

        MetaClass meta = entity.getMeta();

        if (meta.isReference())
            return entity.getId() + "*";

        for (String memberName : meta.getMemberNames()) {
            IMetaAttribute attribute = meta.getMetaAttribute(memberName);
            IMetaType type = attribute.getMetaType();

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

            if(!valueIsNull)
                str += "\n" + prefix + memberName + " : " + value.getCreditorId() + " : " + valueToString;
        }

        return str;
    }

    /*
   *   converting baseEntity object to Java code
   *
   *  */
    public static String toJava(BaseEntity entity, String prefix, int counter) {
        MetaClass meta = entity.getMeta();
        String str = " ";
        // creates new entity object

        if (counter == 0) {
            str += "\n BaseEntity " + meta.getClassName() +
                    "Entity = new BaseEntity(meta" + meta.getClassName().toString().substring(0, 1).toUpperCase()
                    + meta.getClassName().toString().substring(1) + "Holder, reportDate);";
        } else {
            str += "\n BaseEntity " + meta.getClassName() +
                    "Entity" + counter + " = new BaseEntity(meta" +
                    meta.getClassName().toString().substring(0, 1).toUpperCase()
                    + meta.getClassName().toString().substring(1) + "Holder, reportDate);";
        }

        for (String memberName : meta.getMemberNames()) {
            IMetaAttribute attribute = meta.getMetaAttribute(memberName);
            IMetaType type = attribute.getMetaType();
            IBaseValue value = entity.getBaseValue(memberName);

            String valueToString = "";

            if (value != null && value.getValue() != null) {
                if (type.isComplex()) //  set or  entity
                {
                    if (!type.isSet()) //  not a set
                    {
                        if (type.isComplex()) // puts entity value
                        {
                            str += toJava((BaseEntity) value.getValue(), prefix, 0) + "\n " + meta.getClassName()
                                    + "Entity.put( \"" + memberName +
                                    "\" , new BaseValue(batch, " + memberName.toString()
                                    + "Entity));";
                        }
                    } else // if a set
                    {
                        if (type.isSetOfSets()) //if set in a set
                        {
                            valueToString = toJava((BaseEntity) value.getValue(), prefix, 0);
                        }
                        if (type.isSet())//creates new set object
                        {
                            str += "\n BaseSet " + meta.getClassName() +
                                    "Set = new BaseSet(((MetaSet)( " + meta.getClassName().toString()
                                    + "Entity.getMemberType(\"" + memberName + "\"))).getMemberType());" +
                                    complexJavaSet((BaseSet) value.getValue(), prefix, (MetaSet) type,
                                            meta.getClassName());
                            if (type.isSet()) {

                                str += "\n " + meta.getClassName() + "Entity.put( \"" + memberName +
                                        "\" , new BaseValue(batch, " + meta.getClassName()
                                        + "Set));";
                            }
                        }
                    }
                } else  // if not an array or entity
                {
                    String var = value.getValue().toString();

                    if (type.toString("").contains(DataTypes.STRING.toString())) {
                        var = "\"" + value.getValue() + "\"";
                    }
                    if (type.isSet())//creates new set object
                    {
                        str += "\n BaseSet " + meta.getClassName() +
                                "Set = new BaseSet(((MetaSet)( " + meta.getClassName().toString()
                                + "Entity.getMemberType(\"" + memberName + "\"))).getMemberType());" +
                                complexJavaSet((BaseSet) value.getValue(), prefix, (MetaSet) type,
                                        meta.getClassName());
                        if (type.isSet()) {

                            str += "\n " + meta.getClassName() + "Entity.put( \"" + memberName +
                                    "\" , new BaseValue(batch, " + meta.getClassName() + "Set));";

                        }
                    } else //puts simple values
                    {
                        if (counter == 0) {
                            valueToString = "\n " + meta.getClassName() + "Entity.put( \"" + memberName +
                                    "\" , new BaseValue(batch, " + var + "));";
                        } else {

                            valueToString = "\n " + meta.getClassName() + "Entity" + counter + ".put( \"" +
                                    memberName +
                                    "\" , new BaseValue(batch, " + var + "));";
                        }

                    }
                }
            }
            str += "" + prefix + valueToString;
        }
        return str;
    }

    private static String complexJavaSet(BaseSet set, String prefix, MetaSet metaSet, String memberName) {
        String str = " ";
        int counter = 0;
        for (IBaseValue value : set.get()) {
            if (metaSet.isSet()) {
                if (metaSet.isSetOfSets()) {
                    str += complexJavaSet((BaseSet) value.getValue(), prefix, (MetaSet) metaSet.getMemberType(),
                            memberName);
                } else if (metaSet.isComplex())//if an entity
                {
                    str += toJava((BaseEntity) value.getValue(), prefix, counter);
                } else {
                    str += "\n " + memberName + "Set.put(new BaseValue(batch," + value.getValue().toString() + "));";
                }
            }
            if (metaSet.isComplex()) {
                if (counter == 0) {
                    str += "\n " + memberName + "Set.put(new BaseValue(batch," +
                            memberName.subSequence(0, memberName.length() - 1) + "Entity" + "));";
                } else {
                    str += "\n " + memberName + "Set.put(new BaseValue(batch," +
                            memberName.subSequence(0, memberName.length() - 1) + "Entity" + counter + "));";
                }

            }
            counter++;
        }
        return str;
    }

    public static String getJavaFunction(String fName, BaseEntity entity) {
        String str = "protected BaseEntity " + fName +
                "(Batch batch)\n{\n java.util.Date reportDate = new java.util.Date();\n";
        str += toJava(entity, "", 0);
        str += "\n\n return " + entity.getMeta().getClassName() + "Entity;\n";
        str += "}";
        return str;
    }


    private static String complexSet(BaseSet set, String prefix, MetaSet metaSet) {
        String str = "";

        for (IBaseValue value : set.get()) {
            if (metaSet.isSet()) {
                if (metaSet.isSetOfSets()) {
                    str += complexSet((BaseSet) value.getValue(), prefix + "\t", (MetaSet) metaSet.getMemberType());
                } else if (metaSet.isComplex()) {
                    str += "\n" + prefix + toString((BaseEntity) value.getValue(), prefix + "\t");
                } else {
                    str += value.getValue().toString();
                }
            }
        }

        return str;
    }
}
