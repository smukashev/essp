package kz.bsbnb.usci.bconv.xsd;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by maksat on 7/20/15.
 */
@Component
public class XSDGenerator {

    public void generate(OutputStream schema, List<MetaClass> metaClasses) {
        PrintStream ps = new PrintStream(schema);

        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">");
        ps.println();
        ps.println("<xsd:element name=\"batch\" type=\"batch\"/>");
        ps.println("<xsd:element name=\"entities\" type=\"entities\"/>");
        ps.println();
        ps.println("<xsd:complexType name=\"batch\">");
        ps.println("<xsd:all>");
        ps.println("<xsd:element name=\"entities\" type=\"entities\"/>");
        ps.println("</xsd:all>");
        ps.println("</xsd:complexType>");
        ps.println();
        ps.println("<xsd:complexType name=\"entities\">");
        ps.println("<xsd:choice minOccurs=\"1\" maxOccurs=\"unbounded\">");

        for (MetaClass metaClass : metaClasses) {
            ps.println("<xsd:element name=\"" + metaClass.getClassName() + "\" type=\"" + metaClass.getClassName() + "\"/>");
        }

        ps.println("</xsd:choice>");
        ps.println("</xsd:complexType>");

        // true if complex
        Map<String, Boolean> setMemberTypes = new HashMap<>();

        for (MetaClass metaClass : metaClasses) {
            ps.println();
            ps.println("<xsd:complexType name=\"" + metaClass.getClassName() + "\">");
            ps.println("<xsd:all>");

            for (String attrName : metaClass.getAttributeNames()) {
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attrName);
                printAttribute(metaAttribute, ps, setMemberTypes);
            }

            ps.println("</xsd:all>");
            ps.println("</xsd:complexType>");
        }

        for (String setMemberType : setMemberTypes.keySet()) {
            ps.println();
            ps.println("<xsd:complexType name=\"" + setMemberType + "_set" + "\">");
            ps.println("<xsd:sequence>");

            boolean isComplex = setMemberTypes.get(setMemberType);
            String elementType = isComplex ? setMemberType : getSimpleType(DataTypes.valueOf(setMemberType));
            printElement(ps, Element.create("item", elementType, "0", "unbounded", "false"));

            ps.println("</xsd:sequence>");
            ps.println("</xsd:complexType>");
        }

        ps.println();

        printDateType(ps);

        ps.println();

        ps.println("</xsd:schema>");

        ps.close();
    }

    private void printDateType(PrintStream ps) {
        ps.println("<xsd:simpleType name=\"date\">");
        ps.println("<xsd:restriction base=\"xsd:string\">");
        ps.println("<xsd:pattern value=\"([1-9]|0[1-9]|1[0-9]|2[0-9]|3[01])[.](0?[1-9]|1[0-2])[.](19|20)\\d\\d\"/>");
        ps.println("<xsd:pattern value=\"(19|20)\\d\\d[\\-](0?[1-9]|1[0-2])[\\-]([1-9]|0[1-9]|1[0-9]|2[0-9]|3[01])\"/>");
        ps.println("</xsd:restriction>");
        ps.println("</xsd:simpleType>");
    }

    private void printAttribute(IMetaAttribute metaAttribute, PrintStream ps, Map<String, Boolean> setTypes) {
        Element element;
        String minOccurs = metaAttribute.isKey() ? "1" : "0";
        String maxOccurs = "1";
        String nillable = metaAttribute.isKey() ? "false" : "true";

        if (metaAttribute.getMetaType().isSet()) {
            if (metaAttribute.getMetaType().isComplex()) {
                MetaSet metaSet = (MetaSet) metaAttribute.getMetaType();
                MetaClass memberMetaClass = (MetaClass) metaSet.getMemberType();
                element = Element.create(
                        metaAttribute.getName(), memberMetaClass.getClassName() + "_set", minOccurs, maxOccurs, nillable);
                setTypes.put(memberMetaClass.getClassName(), true);
            } else {
                MetaSet metaSet = (MetaSet) metaAttribute.getMetaType();
                DataTypes memberTypeCode = metaSet.getTypeCode();
                element = Element.create(
                        metaAttribute.getName(), memberTypeCode.name() + "_set", minOccurs, maxOccurs, nillable);
                setTypes.put(memberTypeCode.name(), false);
            }
        } else if (metaAttribute.getMetaType().isComplex()) {
            MetaClass metaClass = (MetaClass) metaAttribute.getMetaType();
            element = Element.create(
                    metaAttribute.getName(), metaClass.getClassName(), minOccurs, maxOccurs, nillable);
        } else {
            MetaValue metaValue = (MetaValue) metaAttribute.getMetaType();
            element = Element.create(
                    metaAttribute.getName(), getSimpleType(metaValue.getTypeCode()), minOccurs, maxOccurs, nillable);
        }

        printElement(ps, element);
    }

    private String getSimpleType(DataTypes typeCode) {
        switch (typeCode) {
            case INTEGER:
                return "xsd:int";
            case STRING:
                return "xsd:string";
            case DOUBLE:
                return "xsd:double";
            case BOOLEAN:
                return "xsd:boolean";
            case DATE:
                return "date";
            default:
                throw new IllegalArgumentException();
        }
    }

    private static class Element {
        private final String name;
        private final String type;
        private final String minOccurs;
        private final String maxOccurs;
        private final String nillable;

        private Element(String name, String type, String minOccurs, String maxOccurs, String nillable) {
            this.name = name;
            this.type = type;
            this.minOccurs = minOccurs;
            this.maxOccurs = maxOccurs;
            this.nillable = nillable;
        }

        public static Element create(String name, String type, String minOccurs, String maxOccurs, String nillable) {
            return new Element(name, type, minOccurs, maxOccurs, nillable);
        }
    }

    private void printElement(PrintStream ps, Element element) {
        ps.printf("<xsd:element name=\"%s\" type=\"%s\" minOccurs=\"%s\" maxOccurs=\"%s\" nillable=\"%s\"/>\n",
                element.name, element.type, element.minOccurs, element.maxOccurs, element.nillable);
    }

}
