package kz.bsbnb.usci.eav_persistance.tool.generator2.data;

import kz.bsbnb.usci.eav_model.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav_model.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.AbstractDataGenerator;

/**
 * @author abukabayev
 */
public class MetaClassGenerator extends AbstractDataGenerator{

    private String className;

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {

        return className;
    }

    public MetaClassGenerator() {

    }


    private IMetaAttribute generateMetaAttribute(AttributeTree tree){
        IMetaAttribute attribute;

        if (tree.hasChildren())
        {
                MetaClass metaClass = generateMetaClass(tree);

                metaClass.setComplexKeyType(ComplexKeyTypes.values()[
                        rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), metaClass);
        }else{

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(),
                        new MetaValue(DataTypes.values()[rand.nextInt(DataTypes.values().length)]));
        }


        return attribute;
    }

    private MetaClass generateMetaClass(AttributeTree tree){
        MetaClass meta = new MetaClass(tree.getName()+"_"+getClassName());

        meta.setDisabled(rand.nextBoolean());
        meta.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

        for (AttributeTree child:tree.getChildren()){
            IMetaAttribute type = generateMetaAttribute(child);
            meta.setMetaAttribute(child.getName(),type);
        }
        return meta;
    }

    public MetaClass generateMetaClass(AttributeTree tree,int index){
        setClassName(String.valueOf(index));
        return generateMetaClass(tree);
    }
}