package kz.bsbnb.usci.eav.stresstest;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;

import java.util.ArrayList;
import java.util.Random;

class MetaDataGenerator
{
    private Random rand = new Random();

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

    public MetaDataGenerator(int maxAttributes, int maxRecursion)
    {
        this.maxAttributes = maxAttributes;
        this.maxRecursion = maxRecursion;
    }

    IMetaType generateMetaType(int rec)
    {
        IMetaType type;
        int switcher = rand.nextInt(4);

        if(switcher == 0 || switcher == 2)
        {
            if(rand.nextInt(3) != 2)
                switcher = 3;
        }

        if(rec > maxRecursion)
            if(switcher == 0 || switcher == 2)
                switcher = 3;

        switch (switcher)
        {
            case 0:
                //complex attribute
                MetaClass c = generateMetaClass(rec + 1);
                c.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

                type = c;
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
                MetaClassArray ca = new MetaClassArray(generateMetaClass(rec + 1));
                ca.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);
                ca.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

                type = ca;
                complexTypeArrayCount++;
                break;
            default:
                //simple attribute
                type = new MetaValue(DataTypes.values()[rand.nextInt(DataTypes.values().length)], rand.nextBoolean(),
                        rand.nextBoolean());
                simpleTypeCount++;
                break;
        }

        return type;
    }

    public MetaClass generateMetaClass(int rec)
    {
        MetaClass metaClass = new MetaClass(getNextClassName());
        int attributesCount = rand.nextInt(maxAttributes + 1) + 5;

        metaClass.setDisabled(rand.nextBoolean());

        for (int j = 5; j < attributesCount; j++)
        {
            IMetaType type;

            type = generateMetaType(rec + 1);

            metaClass.setMemberType("attribute_" + j, type);
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
