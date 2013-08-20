package kz.bsbnb.usci.bconv.xsd;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.parser.XSOMParser;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Component
public class Xsd2MetaClass
{
    static Logger logger = LoggerFactory.getLogger(Xsd2MetaClass.class);

    private boolean noFlatten = false;
    private ArrayList<String> usedClassesNames = new ArrayList<String>();

    public ArrayList<String> listClasses(InputStream schema)
    {
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

        ArrayList<String> out = new ArrayList<String>();
        for (String name : types.keySet()) {
            out.add(name);
        }

        return out;
    }

    public MetaClass convertXSD(InputStream schema, String metaClassName)
    {
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

        usedClassesNames.clear();

        XSComplexType ct = xsSchema.getComplexType(metaClassName);

        if (ct == null) {
            throw new IllegalArgumentException("No such class: " + metaClassName);
        }

        return (MetaClass)processComplexType("ct_package", ct, 1, "");
    }

    private IMetaType processComplexType(String name, XSComplexType ct, int maxOccurs, String prefix)
    {
        String attributeStr = "";
        int attrCount = 0;
        HashMap<String, IMetaType> members = new HashMap<String, IMetaType>();
        boolean itIsArray = false;

        try {
            boolean first = true;
            attrCount = ct.getAttributeUses().size();
            for (XSAttributeUse attributes : ct.getAttributeUses()) {
                //System.out.println(prefix + "Attribute - " + attributes.getDecl().getName());

                if(attributes.getDecl().getType().getPrimitiveType().getName().equals("string"))
                    members.put(attributes.getDecl().getName(), new MetaValue(DataTypes.STRING));
                else if(attributes.getDecl().getType().getPrimitiveType().getName().equals("date"))
                    members.put(attributes.getDecl().getName(), new MetaValue(DataTypes.DATE));
                else if(attributes.getDecl().getType().getPrimitiveType().getName().equals("decimal"))
                    members.put(attributes.getDecl().getName(), new MetaValue(DataTypes.DOUBLE));
                else if(attributes.getDecl().getType().getPrimitiveType().getName().equals("boolean"))
                    members.put(attributes.getDecl().getName(), new MetaValue(DataTypes.BOOLEAN));
                else if(attributes.getDecl().getType().getPrimitiveType().getName().equals("integer"))
                    members.put(attributes.getDecl().getName(), new MetaValue(DataTypes.INTEGER));
                else
                    throw new IllegalStateException("Can't convert \"" +
                            attributes.getDecl().getType().getPrimitiveType().getName() + "\" to " +
                            "MetaValue: unknown simple type");

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
                    itIsArray = true;
                }
            }

            //System.out.println("## " + ct.getBaseType());
            if (!ct.getBaseType().getName().equals("anyType") && ct.getBaseType().isSimpleType())
            {
                IMetaType mt = processType(ct.getBaseType().getName(), ct.getBaseType(), maxOccurs, prefix + offset);
                members.put(ct.getBaseType().getName(), mt);
            }

            if (particles != null) {
                processModelGroupContents(members, particles, prefix + offset);
            }

            if ((particlesCount + attrCount) > 1 ||
                    (!ct.getBaseType().getName().equals("anyType") && ct.getBaseType().isSimpleType()) ||
                    noFlatten) {
                System.out.println(prefix + "}");
            }
        } catch (Exception ex) {
            System.out.println(prefix + "Error in complex type contents!!!");
        }

        if (members.size() > 1) {
            int i = 1;
            String actualName = name;
            while (usedClassesNames.contains(actualName))
            {
                actualName = name + i++;
            }

            MetaClass metaClass = new MetaClass(actualName);
            usedClassesNames.add(actualName);

            for(String memberName : members.keySet()) {
                MetaAttribute attr = new MetaAttribute(members.get(memberName));
                metaClass.setMetaAttribute(memberName, attr);
            }

            if (!itIsArray)
                return metaClass;
            else
                return new MetaSet(metaClass);
        } else {
            if (members.size() < 1)
                throw new IllegalStateException("MetaClass with no members: " + name);
            if (itIsArray) {
                return new MetaSet(members.get(members.keySet().toArray()[0]));
            } else {
                return members.get(members.keySet().toArray()[0]);
            }
        }
    }

    private void processModelGroupContents(HashMap<String, IMetaType> members,
                                                  XSParticle[] particles, String prefix)
    {
        for(XSParticle p : particles ){
            XSTerm pterm = p.getTerm();
            if(pterm.isElementDecl()){ //xs:element inside complex type
                //System.out.println(prefix + "Element - " + pterm.asElementDecl().getName());
                IMetaType mt = processElement(pterm, p.getMaxOccurs(), prefix);
                members.put(pterm.asElementDecl().getName(), mt);
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
                        processModelGroupContents(members, pp, prefix + "  ");
                        System.out.println(prefix + "]");
                    } else {
                        processModelGroupContents(members, pp, prefix);
                    }
                }
            }
        }
    }

    private IMetaType processSimpleType(String name, XSSimpleType ct, int maxOccurs, String prefix)
    {
        if (maxOccurs == 1) {
            System.out.println(prefix + name + " : " + ct.getPrimitiveType().getName());

            if(ct.getPrimitiveType().getName().equals("string"))
                return new MetaValue(DataTypes.STRING);
            else if(ct.getPrimitiveType().getName().equals("date"))
                return new MetaValue(DataTypes.DATE);
            else if(ct.getPrimitiveType().getName().equals("decimal"))
                return new MetaValue(DataTypes.DOUBLE);
            else if(ct.getPrimitiveType().getName().equals("boolean"))
                return new MetaValue(DataTypes.BOOLEAN);
            else if(ct.getPrimitiveType().getName().equals("integer"))
                return new MetaValue(DataTypes.INTEGER);
            else
                throw new IllegalStateException("Can't convert \"" + ct.getPrimitiveType().getName() + "\" to " +
                    "MetaValue: unknown simple type");
        } else {
            if (maxOccurs == -1) {
                System.out.println(prefix + name + "[] : " + ct.getPrimitiveType().getName());
            } else {
                System.out.println(prefix + name + "[" + maxOccurs + "] : " + ct.getPrimitiveType().getName());
            }

            if(ct.getPrimitiveType().getName().equals("string"))
                return new MetaSet(new MetaValue(DataTypes.STRING));
            else if(ct.getPrimitiveType().getName().equals("date"))
                return new MetaSet(new MetaValue(DataTypes.DATE));
            else if(ct.getPrimitiveType().getName().equals("decimal"))
                return new MetaSet(new MetaValue(DataTypes.DOUBLE));
            else if(ct.getPrimitiveType().getName().equals("boolean"))
                return new MetaSet(new MetaValue(DataTypes.BOOLEAN));
            else if(ct.getPrimitiveType().getName().equals("integer"))
                return new MetaSet(new MetaValue(DataTypes.INTEGER));
            else
                throw new IllegalStateException("Can't convert \"" + ct.getPrimitiveType().getName() + "\" to " +
                        "MetaValue: unknown simple type");
        }
    }

    private IMetaType processElement(XSTerm el, int maxOccurs, String prefix)
    {
        XSType type = el.asElementDecl().getType();

        return processType(el.asElementDecl().getName(), type, maxOccurs, prefix);
    }

    private IMetaType processType(String name, XSType type, int maxOccurs, String prefix)
    {
        if (type.isComplexType())
        {
            return processComplexType(name, type.asComplexType(), maxOccurs, prefix);
        }

        if (type.isSimpleType())
        {
            return processSimpleType(name, type.asSimpleType(), maxOccurs, prefix);
        }

        throw new IllegalStateException("Type is not complex or simple.");
    }

    private XSParticle[] getParticles(XSParticle particle)
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

    private XSParticle[] getParticles(XSComplexType type)
    {
        return getParticles(asParticle(type));
    }

    private XSParticle asParticle(XSComplexType type)
    {
        XSContentType xsContentType = type.getContentType();
        return xsContentType.asParticle();
    }

    public boolean isNoFlatten()
    {
        return noFlatten;
    }

    public void setNoFlatten(boolean noFlatten)
    {
        this.noFlatten = noFlatten;
    }
}
