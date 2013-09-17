package kz.bsbnb.usci.eav.comparator.impl;

import kz.bsbnb.usci.eav.comparator.IBaseEntityComparator;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author k.tulbassiyev
 */
public class BasicBaseEntityComparator implements IBaseEntityComparator
{
    Logger logger = Logger.getLogger(BasicBaseEntityComparator.class);

    private boolean compareValue(IMetaType type, IBaseValue value1, IBaseValue value2)
    {
        boolean res;

        if(!type.isComplex())
        {
            res = (value1.getValue().equals(value2.
                    getValue()));
        }
        else
        {
            res = compare((BaseEntity)value1.getValue(),
                    (BaseEntity)value2.getValue());
        }

        if (res)
            logger.debug("Same: " + value1.getValue() + ", " + value2.getValue());
        else
            logger.debug("Different: " + value1.getValue() + ", " + value2.getValue());

        return res;
    }

    private boolean compareSet(IMetaType type, IBaseValue value1, IBaseValue value2)
    {
        BaseSet set1 = (BaseSet)value1.getValue();
        BaseSet set2 = (BaseSet)value2.getValue();

        Collection<IBaseValue> ar1 = set1.get();
        Collection<IBaseValue> ar2 = set2.get();

        if(ar1.size() != ar2.size())
            return false;


        boolean res = (((MetaSet)type).getArrayKeyType() == ComplexKeyTypes.ALL);

        for(IBaseValue v1 : ar1)
        {
            if(!type.isComplex())
            {
                if(((MetaSet)type).getArrayKeyType() == ComplexKeyTypes.ALL)
                    res = res && ar2.contains(v1);
                else
                    res = res || ar2.contains(v1);
            }
            else
            {
                boolean found = false;

                for(IBaseValue v2 : ar2)
                {
                    if (compare((BaseEntity)v1.getValue(), (BaseEntity)v2.getValue()))
                    {
                        found = true;
                        break;
                    }
                }

                if(((MetaSet)type).getArrayKeyType() == ComplexKeyTypes.ALL)
                    res = res && found;
                else
                    res = res || found;
            }
        }

        if (res)
            logger.debug("Same: " + value1.getValue() + ", " + value2.getValue());
        else
            logger.debug("Different: " + value1.getValue() + ", " + value2.getValue());

        return res;
    }

    @Override
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException
    {
        if(!c1.getMeta().equals(c2.getMeta()))
        {
            logger.debug("Classes are different: " + c1.getMeta().getClassName() + ", " + c2.getMeta().getClassName());
            return false;
        }

        if (!c1.getMeta().isSearchable() || !c2.getMeta().isSearchable()) {
            return false;
        }

        MetaClass meta = c1.getMeta();

        boolean result = (meta.getComplexKeyType() == ComplexKeyTypes.ALL);

        Set<String> names = meta.getMemberNames();

        for(String name : names)
        {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Testing attribute: " + name);
            if(!attribute.isKey())
            {
                logger.debug("It's not a key! So skipped.");
                continue;
            }

            logger.debug("It's a key!");

            IBaseValue value1 = c1.safeGetValue(name);
            IBaseValue value2 = c2.safeGetValue(name);

            if(value1 == null || value2 == null)
            {
                throw new IllegalArgumentException("Key attribute " + name + " couldn't be null");
            }

            if(meta.getComplexKeyType() == ComplexKeyTypes.ALL)
            {
                if(!type.isSet())
                    result = result && compareValue(type, value1, value2);
                else
                    result = result && compareSet(type, value1, value2);
            }
            else
            {
                if(!type.isSet())
                    result = result || compareValue(type, value1, value2);
                else
                    result = result || compareSet(type, value1, value2);
            }
        }

        logger.debug("Result is: " + result);

        return result;
    }

    public List<String> intersect(BaseEntity c1, BaseEntity c2) throws IllegalStateException
    {
        ArrayList<String> paths = new ArrayList<String>();

        MetaClass meta = c1.getMeta();

        Set<String> names = meta.getMemberNames();

        for(String name : names)
        {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Testing attribute: " + name);

            if (type.isComplex() && !type.isSet()) {
                IBaseValue value1 = c1.safeGetValue(name);
                if (value1 != null) {
                    BaseEntity entity1 = (BaseEntity)(value1.getValue());

                    List<String> subClasses = c2.getMeta().getAllPaths((MetaClass)type);

                    Iterator<String> i = subClasses.iterator();

                    while(i.hasNext()) {
                        String path = i.next();

                        BaseEntity entity2 = (BaseEntity)c2.getEl(path);

                        if (entity1 == null || entity2 == null || !compare(entity1, entity2)) {
                            i.remove();
                        }
                    }

                    paths.addAll(subClasses);
                    paths.addAll(intersect(entity1, c2));
                }
            }
        }

        return paths;
    }
}
