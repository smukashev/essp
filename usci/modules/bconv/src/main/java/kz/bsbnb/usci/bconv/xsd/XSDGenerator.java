package kz.bsbnb.usci.bconv.xsd;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by maksat on 7/20/15.
 */
@Component
public class XSDGenerator {

    private static Logger logger = LoggerFactory.getLogger(XSDGenerator.class);

    public void generate(OutputStream schema, List<MetaClass> metaClasses) {
        PrintStream ps = new PrintStream(schema);

        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">");
        ps.println();
        ps.println("<xsd:element name=\"batch\" type=\"batch\"/>");
        ps.println("<xsd:element name=\"entities\" type=\"entities\"/>");
        ps.println();
        ps.println("<xsd:complexType name=\"batch\">");
        ps.println("<xsd:sequence minOccurs=\"1\" maxOccurs=\"1\">");
        ps.println("<xsd:element name=\"entities\" type=\"entities\"/>");
        ps.println("</xsd:sequence>");
        ps.println("</xsd:complexType>");
        ps.println();
        ps.println("<xsd:complexType name=\"entities\">");
        ps.println("<xsd:choice maxOccurs=\"unbounded\">");

        for (MetaClass metaClass : metaClasses) {
            ps.println("<xsd:element name=\"" + metaClass.getClassName() + "\" type=\"" + metaClass.getClassName() + "\" minOccurs=\"0\"/>");
        }

        ps.println("</xsd:choice>");
        ps.println("</xsd:complexType>");
        ps.println();

        for (MetaClass metaClass : metaClasses) {
            ps.println("<xsd:complexType name=\"" + metaClass.getClassName() + "\">");
            ps.println("<xsd:all>");

            for (String attrName : metaClass.getAttributeNames()) {
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attrName);
                printAttribute(metaAttribute, ps);
            }

            ps.println("</xsd:all>");
            ps.println("</xsd:complexType>");
        }

        ps.println("<xsd:complexType name=\"entity\">");
        ps.println("<xsd:attribute name=\"class\">");
        ps.println("<xsd:simpleType>");
        ps.println("<xsd:restriction base=\"xsd:string\">");
        ps.println("<xsd:enumeration value=\"credit\"/>");
        ps.println();

        ps.close();
    }

    private void printAttribute(IMetaAttribute metaAttribute, PrintStream ps) {
        if (metaAttribute.getMetaType().isSet()) {
            printSet(metaAttribute, ps);
        }

        ps.println();
        ps.println();
    }

    private void printSet(IMetaAttribute metaAttribute, PrintStream ps) {
        ps.println("<xsd:element name=\"" + metaAttribute.getName() + "\" type=\"" + metaAttribute.getName() + "\"/>");
    }

}
