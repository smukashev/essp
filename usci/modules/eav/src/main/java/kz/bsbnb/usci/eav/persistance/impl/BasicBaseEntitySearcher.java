package kz.bsbnb.usci.eav.persistance.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaSet;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;

/**
 * Used to compare BaseEntity in memory, and to retrieve BaseEntities from storage by example.
 */
public class BasicBaseEntitySearcher implements IBaseEntitySearcher {
    Logger logger = LoggerFactory.getLogger(BasicBaseEntitySearcher.class);

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public BaseEntity findSingle(BaseEntity meta)
    {
        //TODO: method stub
        return null;
    }

    @Override
    public ArrayList<BaseEntity> findAll(BaseEntity meta)
    {
        //TODO: method stub
        return null;
    }

    private IBaseValue safeGetValue(BaseEntity entity, String name)
    {
        try
        {
            return entity.getBaseValue(name);
        }
        catch(Exception e)
        {
            return null;
        }
    }

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
        {
            logger.debug("Same: " + value1.getValue() + ", " + value2.getValue());
        }
        else
        {
            logger.debug("Different: " + value1.getValue() + ", " + value2.getValue());
        }

        return res;
    }

    private boolean compareSet(IMetaType type, IBaseValue value1, IBaseValue value2)
    {
        BaseSet set1 = (BaseSet)value1.getValue();
        BaseSet set2 = (BaseSet)value2.getValue();

        Set<IBaseValue> ar1 = set1.get();
        Set<IBaseValue> ar2 = set2.get();

        if(ar1.size() != ar2.size())
            return false;

        boolean res = (((MetaSet)type).getArrayKeyType() ==
                ComplexKeyTypes.ALL);

        for(IBaseValue v1 : ar1)
        {
            if(!type.isComplex())
            {
                if(((MetaSet)type).getArrayKeyType() == ComplexKeyTypes.ALL)
                {
                    res = res && ar2.contains(v1);
                }
                else
                {
                    res = res || ar2.contains(v1);
                }
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
                {
                    res = res && found;
                }
                else
                {
                    res = res || found;
                }
            }
        }

        if (res)
        {
            logger.debug("Same: " + value1.getValue() + ", " + value2.getValue());
        }
        else
        {
            logger.debug("Different: " + value1.getValue() + ", " + value2.getValue());
        }

        return res;
    }

    @Override
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException {
        if(!c1.getMeta().equals(c2.getMeta()))
        {
            logger.debug("Classes are different: " + c1.getMeta().getClassName() + ", " + c2.getMeta().getClassName());
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

            IBaseValue value1 = safeGetValue(c1, name);
            IBaseValue value2 = safeGetValue(c2, name);

            if(value1 == null || value2 == null)
            {
                throw new IllegalArgumentException("Key attribute " + name + " couldn't be null");
            }

            //todo: add other complex key type
            if(meta.getComplexKeyType() == ComplexKeyTypes.ALL)
            {
                if(!type.isArray())
                {
                    result = result && compareValue(type, value1, value2);
                }
                else
                {
                    result = result && compareSet(type, value1, value2);
                }
            }
            else
            {
                if(!type.isArray())
                {
                    result = result || compareValue(type, value1, value2);
                }
                else
                {
                    result = result || compareSet(type, value1, value2);
                }
            }
        }

        logger.debug("Result is: " + result);

        return result;
    }
}
