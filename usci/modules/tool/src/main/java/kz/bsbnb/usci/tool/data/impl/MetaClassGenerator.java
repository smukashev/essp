package kz.bsbnb.usci.tool.data.impl;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.tool.data.AbstractGenerator;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author a.tkachenko
 */
public class MetaClassGenerator  extends AbstractGenerator
{
    private int maxAttributes = 25;
    private int maxRecursion = 2;
    private int classesNumber = 0;

    String getNextClassName()
    {
        return "class_" + ++classesNumber;
    }

    private int simpleTypeCount = 0;
    private int complexTypeCount = 0;
    private int simpleTypeArrayCount = 0;
    private int complexTypeArrayCount = 0;

    private ArrayList<MetaClass> metaClasses = new ArrayList<MetaClass>();

    public MetaClassGenerator(int maxAttributes, int maxRecursion)
    {
        this.maxAttributes = maxAttributes;
        this.maxRecursion = maxRecursion;
    }

    IMetaType generateMetaType(int rec)
    {
        IMetaType type;
        int switcher = rand.nextInt(4);

        if(switcher == 0 || switcher == 2)
            if(rand.nextInt(3) != 2)
                switcher = 3;

        if(rec > maxRecursion)
            if(switcher == 0 || switcher == 2)
                switcher = 3;

        switch (switcher)
        {
            case 0:
                //complex attribute
                MetaClass metaClass = generateMetaClass(rec + 1);
                metaClass.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);
                type = new MetaClassHolder(metaClass);

                break;
            case 1:
                //simple array
                MetaValueArray a = new MetaValueArray(DataTypes.values()[rand.nextInt(DataTypes.values().length)],
                        rand.nextBoolean(), rand.nextBoolean());

                a.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

                type = a;
                simpleTypeArrayCount++;

                break;
            case 2:
                //complex array
                MetaClassArray ca = new MetaClassArray(generateMetaClassHolder(rec + 1));
                ca.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

                type = ca;
                complexTypeArrayCount++;

                break;
            default:
                //simple attribute
                type = new MetaValue(DataTypes.values()[rand.nextInt(DataTypes.values().length)],
                        rand.nextBoolean(), rand.nextBoolean());

                simpleTypeCount++;

                break;
        }

        return type;
    }

    public MetaClassHolder generateMetaClassHolder(int rec)
    {
        return new MetaClassHolder(generateMetaClass(rec));
    }

    public MetaClass generateMetaClass(int rec)
    {
        MetaClass metaClass = new MetaClass(getNextClassName());
        int attributesCount = rand.nextInt(maxAttributes + 1) + 5;

        metaClass.setDisabled(rand.nextBoolean());
        metaClass.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

        for (int j = 0; j < attributesCount; j++)
        {
            IMetaType type = generateMetaType(rec + 1);

            long nanoTime = System.nanoTime();

            if(type instanceof MetaValueArray)
            {
                MetaClassHolder metaClassHolder = new MetaClassHolder("simple_array_" + nanoTime);
                metaClassHolder.getMeta().setMemberType("simple_attribute_" + nanoTime, type);

                metaClass.setMemberType("simple_array_" + nanoTime, metaClassHolder);
            }
            else if(type instanceof MetaClassArray)
            {
                MetaClassHolder metaClassHolder = new MetaClassHolder("complex_array_" + nanoTime);

                metaClassHolder.getMeta().setMemberType(((MetaClassArray) type).getMembersType()
                        .getMeta().getClassName(), type);

                metaClass.setMemberType("complex_array_" + nanoTime, metaClassHolder);
            }
            else if(type instanceof MetaClassHolder)
            {
                MetaClassHolder metaClassHolder = (MetaClassHolder) type;
                metaClass.setMemberType(metaClassHolder.getMeta().getClassName(), type);
            }
            else
            {
                metaClass.setMemberType("attribute_" + nanoTime, type);
            }
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
