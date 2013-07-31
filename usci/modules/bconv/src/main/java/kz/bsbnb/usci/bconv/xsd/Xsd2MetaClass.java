package kz.bsbnb.usci.bconv.xsd;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.parser.XSOMParser;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class Xsd2MetaClass
{
    static Logger logger = LoggerFactory.getLogger(Xsd2MetaClass.class);

    static private boolean noFlatten = false;

    public static MetaClass convertXSD(InputStream schema, String metaClassName)
    {
        MetaClass meta = new MetaClass();
        XSSchema xsSchema;

        if (schema == null) {
            throw new IllegalArgumentException("Schema stream can't be null");
        }

        try {
            XSOMParser parser = new XSOMParser();
            parser.parse(schema);
            XSSchemaSet schemaSet = parser.getResult();
            xsSchema = schemaSet.getSchema(1);
        }
        catch (Exception exp) {
            logger.error("Can't open schema");
            throw new IllegalArgumentException("Can't open schema");
        }

        Map<String, XSComplexType> types = xsSchema.getComplexTypes();

        System.out.println("------- All types ----------");
        for (String name : types.keySet()) {
            System.out.println(name);
        }

        System.out.println("----------------------------");
        XSComplexType ct = xsSchema.getComplexType(metaClassName);

        if (ct == null) {
            throw new IllegalArgumentException("No such class: " + metaClassName);
        }

        processComplexType("ct_package", ct, 1, "");

        return meta;
    }

    private static void processComplexType(String name, XSComplexType ct, int maxOccurs, String prefix)
    {
        String attributeStr = "";
        int attrCount = 0;

        try {
            boolean first = true;
            attrCount = ct.getAttributeUses().size();
            for (XSAttributeUse attributes : ct.getAttributeUses()) {
                //System.out.println(prefix + "Attribute - " + attributes.getDecl().getName());
                if(first) {
                    attributeStr += attributes.getDecl().getName() + " : " + attributes.getDecl().getType().
                            getPrimitiveType().getName();
                    first = false;
                } else {
                    attributeStr += "," + attributes.getDecl().getName() + " : " + attributes.getDecl().getType().
                            getPrimitiveType().getName();
                }
            }
        } catch (Exception e) {
            attributeStr = "";
        }

        if (attributeStr.length() > 0)
            attributeStr = " (" + attributeStr + ")";

        String subtypesStr = "";
        try {
            boolean first = true;
            for (XSComplexType subTypes : ct.getSubtypes()) {
                //System.out.println(prefix + "Subtype - " + subTypes.getName());
                if(first) {
                    subtypesStr += subTypes.getName();
                    first = false;
                } else {
                    subtypesStr += "," + subTypes.getName();
                }
            }
        } catch (Exception e) {
            subtypesStr = "";
        }

        if (subtypesStr.length() > 0)
            subtypesStr = " -> " + subtypesStr;

        String comppType = "";
        if (ct.getName() != null)
            comppType = " : " + ct.getName();
        else
            comppType = " : ct_" + name;

        try {
            XSParticle[] particles = getParticles(ct);
            String offset = "";
            int particlesCount = 0;
            if (particles != null)
                particlesCount = particles.length;

            if ((particlesCount + attrCount) > 1 ||
                    (!ct.getBaseType().getName().equals("anyType") && ct.getBaseType().isSimpleType()) ||
                    noFlatten)
            {
                offset = "  ";
                if (maxOccurs == 1) {
                    System.out.println(prefix + name + comppType + subtypesStr + attributeStr + " {");
                } else {
                    if (maxOccurs == -1) {
                        System.out.println(prefix + name + "[]" + comppType + subtypesStr + attributeStr + " {");
                    } else {
                        System.out.println(prefix + name + "[" + maxOccurs + "]" +
                                comppType + subtypesStr + attributeStr + " {");
                    }
                }
            }

            //System.out.println("## " + ct.getBaseType());
            if (!ct.getBaseType().getName().equals("anyType") && ct.getBaseType().isSimpleType())
                processType(ct.getBaseType().getName(), ct.getBaseType(), maxOccurs, prefix + offset);

            if (particles != null) {
                processModelGroupContents(particles, prefix + offset);
            }

            if ((particlesCount + attrCount) > 1 ||
                    (!ct.getBaseType().getName().equals("anyType") && ct.getBaseType().isSimpleType()) ||
                    noFlatten)
                System.out.println(prefix + "}");
        } catch (Exception ex) {
            System.out.println(prefix + "Error in complex type contents!!!");
        }
    }

    private static void processModelGroupContents(XSParticle[] particles, String prefix)
    {
        for(XSParticle p : particles ){
            XSTerm pterm = p.getTerm();
            if(pterm.isElementDecl()){ //xs:element inside complex type
                //System.out.println(prefix + "Element - " + pterm.asElementDecl().getName());
                processElement(pterm, p.getMaxOccurs(), prefix);
            }
            if(pterm.isModelGroup()){ //xs:element inside complex type
                if(p.getMaxOccurs() != 1 || pterm.asModelGroup().getCompositor() == XSModelGroup.Compositor.CHOICE) {
                    if (p.getMaxOccurs() != 1) {
                        if (p.getMaxOccurs() == -1)
                            System.out.print(prefix + "ModelGroup - unbound - ");
                        else
                            System.out.print(prefix + "ModelGroup - " + p.getMaxOccurs() + " - ");
                    } else {
                        System.out.print(prefix + "ModelGroup - ");
                    }
                    if(pterm.asModelGroup().getCompositor() == XSModelGroup.Compositor.ALL)
                        System.out.println("All [");
                    if(pterm.asModelGroup().getCompositor() == XSModelGroup.Compositor.CHOICE)
                        System.out.println("Choice [");
                    if(pterm.asModelGroup().getCompositor() == XSModelGroup.Compositor.SEQUENCE)
                        System.out.println("Sequence [");
                }

                XSParticle[] pp = getParticles(p);
                if (pp != null)
                {
                    if(p.getMaxOccurs() != 1 ||
                            pterm.asModelGroup().getCompositor() == XSModelGroup.Compositor.CHOICE) {
                        processModelGroupContents(pp, prefix + "  ");
                        System.out.println(prefix + "]");
                    } else {
                        processModelGroupContents(pp, prefix);
                    }
                }
            }
        }
    }

    private static void processSimpleType(String name, XSSimpleType ct, int maxOccurs, String prefix)
    {
        if (maxOccurs == 1) {
            System.out.println(prefix + name + " : " + ct.getPrimitiveType().getName());
        } else {
            if (maxOccurs == -1) {
                System.out.println(prefix + name + "[] : " + ct.getPrimitiveType().getName());
            } else {
                System.out.println(prefix + name + "[" + maxOccurs + "] : " + ct.getPrimitiveType().getName());
            }
        }
    }

    private static void processElement(XSTerm el, int maxOccurs, String prefix)
    {
        XSType type = el.asElementDecl().getType();

        processType(el.asElementDecl().getName(), type, maxOccurs, prefix);
    }

    private static void processType(String name, XSType type, int maxOccurs, String prefix)
    {
        if (type.isComplexType())
        {
            processComplexType(name, type.asComplexType(), maxOccurs, prefix);
        }

        if (type.isSimpleType())
        {
            processSimpleType(name, type.asSimpleType(), maxOccurs, prefix);
        }
    }

    private static XSParticle[] getParticles(XSParticle particle)
    {
        if(particle != null){
            XSTerm term = particle.getTerm();
            if(term.isModelGroup()){
                XSModelGroup xsModelGroup = term.asModelGroup();
                return xsModelGroup.getChildren();
            }
        }

        return null;
    }

    private static XSParticle[] getParticles(XSComplexType type)
    {
        return getParticles(asParticle(type));
    }

    private static XSParticle asParticle(XSComplexType type)
    {
        XSContentType xsContentType = type.getContentType();
        return xsContentType.asParticle();
    }
}
