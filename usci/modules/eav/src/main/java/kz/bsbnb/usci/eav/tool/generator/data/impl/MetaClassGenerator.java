package kz.bsbnb.usci.eav.tool.generator.data.impl;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.tool.generator.data.AbstractDataGenerator;

import java.util.ArrayList;

/**
 * @author a.tkachenko
 */
public class MetaClassGenerator extends AbstractDataGenerator
{
    private int MAX_ATTRIBUTE_NUMBER = 25;
    private int MAX_RECURSION = 2;
    private int CLASSES_NUMBER = 0;

    String getNextClassName()
    {
        return "class_" + ++CLASSES_NUMBER;
    }

    private int simpleTypeCount = 0;
    private int complexTypeCount = 0;
    private int simpleTypeArrayCount = 0;
    private int complexTypeArrayCount = 0;

    private ArrayList<MetaClass> metaClasses = new ArrayList<MetaClass>();

    public MetaClassGenerator(int maxAttributes, int maxRecursion)
    {
        this.MAX_ATTRIBUTE_NUMBER = maxAttributes;
        this.MAX_RECURSION = maxRecursion;
    }

    IMetaAttribute generateMetaAttribute(int rec)
    {
        IMetaAttribute attribute;
        int switcher = rand.nextInt(4);

        if(switcher == 0 || switcher == 2)
            if(rand.nextInt(3) != 2)
                switcher = 3;

        if(rec > MAX_RECURSION)
            if(switcher == 0 || switcher == 2)
                switcher = 3;

        switch (switcher)
        {
            case 0:
                //complex attribute
                MetaClass metaClass = generateMetaClass(rec + 1);

                metaClass.setComplexKeyType(ComplexKeyTypes.values()[
                        rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), metaClass);

                break;
            case 1:
                //simple array
                MetaSet a = new MetaSet(new MetaValue(DataTypes.values()[
                        rand.nextInt(DataTypes.values().length)]));

                a.setArrayKeyType(ComplexKeyTypes.values()[
                        rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), a);
                simpleTypeArrayCount++;

                break;
            case 2:
                //complex array
                MetaSet ca = new MetaSet(generateMetaClass(rec + 1));
                ca.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), ca);
                complexTypeArrayCount++;

                break;
            default:
                //simple attribute
                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(),
                        new MetaValue(DataTypes.values()[rand.nextInt(DataTypes.values().length)]));

                simpleTypeCount++;

                break;
        }

        return attribute;
    }

    public MetaClass generateMetaClass(int rec)
    {
        MetaClass metaClass = new MetaClass(getNextClassName());
        int attributesCount = rand.nextInt(MAX_ATTRIBUTE_NUMBER + 1) + 5;

        metaClass.setDisabled(rand.nextBoolean());
        metaClass.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

        for (int j = 0; j < attributesCount; j++)
        {
            IMetaAttribute type = generateMetaAttribute(rec + 1);

            metaClass.setMetaAttribute("attribute_" + System.nanoTime(), type);
        }

        complexTypeCount++;
        metaClasses.add(metaClass);

        return metaClass;
    }

    public void printStats()
    {
        System.out.println("Simple  types      : " + simpleTypeCount);
        System.out.println("Complex types      : " + complexTypeCount);
        System.out.println("Simple  type arrays: " + simpleTypeArrayCount);
        System.out.println("Complex type arrays: " + complexTypeArrayCount);
    }

    public ArrayList<MetaClass> getMetaClasses()
    {
        return metaClasses;
    }
}
