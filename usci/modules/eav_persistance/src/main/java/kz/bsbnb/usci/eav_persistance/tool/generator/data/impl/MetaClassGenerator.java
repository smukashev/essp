package kz.bsbnb.usci.eav_persistance.tool.generator.data.impl;

import kz.bsbnb.usci.eav_model.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;
import kz.bsbnb.usci.eav_model.model.meta.impl.*;
import kz.bsbnb.usci.eav_model.util.SetUtils;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.AbstractDataGenerator;

import java.util.*;

/**
 * @author a.tkachenko
 */
public class MetaClassGenerator extends AbstractDataGenerator
{
    private int MAX_ATTRIBUTE_NUMBER = 25;
    private int MAX_META_CLASS_RECURSION = 2;
    private int MAX_META_SET_RECURSION = 2;
    private int CLASSES_NUMBER = 0;

    String getNextClassName()
    {
        return "class_" + ++CLASSES_NUMBER;
    }

    private int simpleTypeCount = 0;
    private int complexTypeCount = 0;
    private int simpleTypeSetCount = 0;
    private int complexTypeSetCount = 0;
    private int setOfSimpleSetsCount = 0;
    private int setOfComplexSetsCount = 0;

    private Map<String, MetaClass> metaClasses = new HashMap<String, MetaClass>();

    public MetaClassGenerator(int maxAttributes, int maxMetaClassRecursion, int maxMetaSetRecursion)
    {
        this.MAX_ATTRIBUTE_NUMBER = maxAttributes;
        this.MAX_META_CLASS_RECURSION = maxMetaClassRecursion;
        this.MAX_META_SET_RECURSION = maxMetaSetRecursion;
    }

    private IMetaAttribute generateMetaAttribute(int rec, Set<String> classNames)
    {
        IMetaAttribute attribute;
        int switcher = rand.nextInt(5);

        if(switcher == 0 || switcher == 2 || switcher == 4)
            if(rand.nextInt(3) != 2)
                switcher = 5;

        if(rec > MAX_META_CLASS_RECURSION)
            if(switcher == 0 || switcher == 2 || switcher == 4)
                switcher = 5;

        switch (switcher)
        {
            case 0:
                //complex attribute
                MetaClass metaClass = generateMetaClass(rec + 1, classNames);

                metaClass.setComplexKeyType(ComplexKeyTypes.values()[
                        rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), metaClass);

                break;
            case 1:
                //simple set
                MetaSet a = new MetaSet(new MetaValue(DataTypes.values()[
                        rand.nextInt(DataTypes.values().length)]));

                a.setArrayKeyType(ComplexKeyTypes.values()[
                        rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), a);
                simpleTypeSetCount++;

                break;
            case 2:
                //complex set
                MetaSet cs = new MetaSet(generateMetaClass(rec + 1, classNames));
                cs.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), cs);
                complexTypeSetCount++;

                break;
            case 3:
                //set of simple set
                MetaSet soss = generateMetaSetOfSimpleTypes(0);
                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), soss);
                setOfSimpleSetsCount++;

                break;
            case 4:
                //set of complex set
                MetaSet socs = generateMetaSetOfComplexTypes(0, classNames);
                attribute = new MetaAttribute(rand.nextBoolean(), rand.nextBoolean(), socs);
                setOfComplexSetsCount++;

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

    private MetaClass generateMetaClass(int rec, Set<String> classNames)
    {
        Random random = new Random();

        Set<String> availableClassNames = SetUtils.difference(metaClasses.keySet(), classNames);

        if (random.nextBoolean() || availableClassNames.isEmpty())
        {
            MetaClass metaClass = new MetaClass(getNextClassName());
            int attributesCount = rand.nextInt(MAX_ATTRIBUTE_NUMBER + 1) + 5;

            metaClass.setDisabled(rand.nextBoolean());
            metaClass.setComplexKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

            classNames.add(metaClass.getClassName());

            for (int j = 0; j < attributesCount; j++)
            {
                IMetaAttribute type = generateMetaAttribute(rec + 1, classNames);

                metaClass.setMetaAttribute("attribute_" + System.nanoTime(), type);
            }

            complexTypeCount++;
            metaClasses.put(metaClass.getClassName(), metaClass);

            return metaClass;
        }
        else
        {
            String className = SetUtils.getRandomElement(availableClassNames, random);
            return metaClasses.get(className);
        }
    }

    public MetaClass generateMetaClass() {
        return generateMetaClass(0, new HashSet<String>());
    }

    private MetaSet generateMetaSetOfComplexTypes(int rec, Set<String> classNames) {
        IMetaType metaType;
        if (rec <= (MAX_META_SET_RECURSION - 1))
        {
            metaType = generateMetaSetOfComplexTypes(rec + 1, classNames);
        }
        else
        {
            metaType = generateMetaClass(MAX_META_CLASS_RECURSION + 1, classNames);
        }

        MetaSet metaSet = new MetaSet(metaType);
        metaSet.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

        return metaSet;
    }

    private MetaSet generateMetaSetOfSimpleTypes(int rec) {
        IMetaType metaType;
        if (rec <= (MAX_META_SET_RECURSION - 1))
        {
            metaType = generateMetaSetOfSimpleTypes(rec + 1);
        }
        else
        {
            metaType = new MetaValue(DataTypes.values()[rand.nextInt(DataTypes.values().length)]);
        }

        MetaSet metaSet = new MetaSet(metaType);
        metaSet.setArrayKeyType(ComplexKeyTypes.values()[rand.nextInt(ComplexKeyTypes.values().length)]);

        return metaSet;
    }

    public void printStats()
    {
        System.out.println("Simple  types           : " + simpleTypeCount);
        System.out.println("Complex types           : " + complexTypeCount);
        System.out.println("Simple  type sets       : " + simpleTypeSetCount);
        System.out.println("Complex type sets       : " + complexTypeSetCount);
        System.out.println("Set of simple type sets : " + setOfSimpleSetsCount);
        System.out.println("Set of complex type sets: " + setOfComplexSetsCount);
    }

    public Collection<MetaClass> getMetaClasses()
    {
        return metaClasses.values();
    }
}
