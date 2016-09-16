package kz.bsbnb.usci.bconv.xsd;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ps.println("<xsd:simpleType name=\"operation\">");
        ps.println("<xsd:restriction base=\"xsd:string\">");
        ps.println("<xsd:enumeration value=\"DELETE\"/>");
        ps.println("<xsd:enumeration value=\"CLOSE\"/>");
        ps.println("<xsd:enumeration value=\"INSERT\"/>");
        ps.println("<xsd:enumeration value=\"NEW\"/>");
        ps.println("</xsd:restriction>");
        ps.println("</xsd:simpleType>");

        ps.println();
        ps.println("<xsd:complexType name=\"entities\">");
        ps.println("<xsd:choice minOccurs=\"1\" maxOccurs=\"unbounded\">");
        ps.println("<xsd:element name=\"credit\">");
        ps.println("<xsd:complexType>");
        ps.println("<xsd:complexContent>");
        ps.println("<xsd:extension base=\"credit\">");
        ps.println("<xsd:attribute name=\"operation\" type=\"operation\" use=\"optional\"/>");
        ps.println("</xsd:extension>");
        ps.println("</xsd:complexContent>");
        ps.println("</xsd:complexType>");
        ps.println("</xsd:element>");
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
                printAttribute(metaClass, metaAttribute, ps, setMemberTypes);
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
        ps.println("<xsd:pattern value=\"[0-9][0-9].[0-9][0-9].[0-9][0-9][0-9][0-9]\"/>");
        ps.println("</xsd:restriction>");
        ps.println("</xsd:simpleType>");
    }

    private void printAttribute(MetaClass metaClass, IMetaAttribute metaAttribute, PrintStream ps, Map<String, Boolean> setTypes) {
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
            MetaClass mClass = (MetaClass) metaAttribute.getMetaType();
            element = Element.create(
                    metaAttribute.getName(), mClass.getClassName(), minOccurs, maxOccurs, nillable);
        } else {
            MetaValue metaValue = (MetaValue) metaAttribute.getMetaType();
            element = Element.create(
                    metaAttribute.getName(), getSimpleType(metaValue.getTypeCode()), minOccurs, maxOccurs, nillable, !metaClass.isReference() && metaAttribute.isKey());
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
        private final boolean isSimpleKey;

        private Element(String name, String type, String minOccurs, String maxOccurs, String nillable, boolean isSimpleKey) {
            this.name = name;
            this.type = type;
            this.minOccurs = minOccurs;
            this.maxOccurs = maxOccurs;
            this.nillable = nillable;
            this.isSimpleKey = isSimpleKey;
        }

        public static Element create(String name, String type, String minOccurs, String maxOccurs, String nillable) {
            return new Element(name, type, minOccurs, maxOccurs, nillable, false);
        }

        public static Element create(String name, String type, String minOccurs, String maxOccurs, String nillable, boolean isSimpleKey) {
            return new Element(name, type, minOccurs, maxOccurs, nillable, isSimpleKey);
        }
    }

    private void printElement(PrintStream ps, Element element) {
        if (element.isSimpleKey) {
            ps.printf("<xsd:element name=\"%s\" minOccurs=\"%s\" maxOccurs=\"%s\" nillable=\"%s\">\n",
                    element.name, element.minOccurs, element.maxOccurs, element.nillable);
                ps.println("<xsd:complexType>");
                    ps.println("<xsd:simpleContent>");
                        ps.printf("<xsd:extension base=\"%s\">\n", element.type);
                            ps.println("<xsd:attribute type=\"operation\" name=\"operation\" use=\"optional\"/>");
                            ps.printf("<xsd:attribute type=\"%s\" name=\"data\" use=\"optional\"/>\n", element.type);
                        ps.println("</xsd:extension>");
                    ps.println("</xsd:simpleContent>");
                ps.println("</xsd:complexType>");
            ps.println("</xsd:element>");
        } else {
            ps.printf("<xsd:element name=\"%s\" type=\"%s\" minOccurs=\"%s\" maxOccurs=\"%s\" nillable=\"%s\"/>\n",
                    element.name, element.type, element.minOccurs, element.maxOccurs, element.nillable);
        }
    }

}
