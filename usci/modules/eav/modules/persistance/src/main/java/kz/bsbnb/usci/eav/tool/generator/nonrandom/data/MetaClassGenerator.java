package kz.bsbnb.usci.eav.tool.generator.nonrandom.data;


import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.tool.generator.random.data.AbstractDataGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author abukabayev
 */
public class MetaClassGenerator extends AbstractDataGenerator {
    private static int index,index2;
    private Map<String, MetaClass> metaClasses = new HashMap<String, MetaClass>();

    public void setIndex(int index) {
        MetaClassGenerator.index = index;
    }

    public void setIndex2(int index) {
        MetaClassGenerator.index2 = index;
    }

    public int getIndex() {
        return ++index;
    }

    public int getIndex2() {
        return index2;
    }

    public MetaClassGenerator() {
        super();
    }

    private IMetaAttribute generateMetaAttribute(AttributeTree tree) {
        IMetaAttribute attribute;

        if (tree.hasChildren()) {
            MetaClass metaClass = generateMetaClass(tree);

            metaClass.setComplexKeyType(ComplexKeyTypes.values()[
                    rand.nextInt(ComplexKeyTypes.values().length)]);

            attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), metaClass);
        } else {

            attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(),
                    new MetaValue(DataTypes.values()[rand.nextInt(DataTypes.values().length)]));
        }

        return attribute;
    }

    private MetaClass generateMetaClass(AttributeTree tree) {
        MetaClass meta = new MetaClass(tree.getName()+"_"+getIndex2()+"_"+getIndex());

        meta.setDisabled(false); // todo: rand.nextBoolean();
        meta.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

        for (AttributeTree child:tree.getChildren()) {
            IMetaAttribute type = generateMetaAttribute(child);
            meta.setMetaAttribute(child.getName(),type);
        }

        metaClasses.put(meta.getClassName(),meta);
        return meta;
    }

    public MetaClass generateMetaClass(AttributeTree tree,int index) {
        setIndex(1);
        setIndex2(index);
        return generateMetaClass(tree);
    }

    public Collection<MetaClass> getMetaClasses() {
        return metaClasses.values();
    }
}