package kz.bsbnb.usci.eav.persistance.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
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

    //TODO: method stub refactoring needed
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
            logger.debug("Testing attribute: " + name);
            if(!meta.getMetaAttribute(name).isKey())
            {
                logger.debug("It's not a key! So skipped.");
                continue;
            }

            logger.debug("It's a key!");

            //todo: add other complex key type
            if(meta.getComplexKeyType() == ComplexKeyTypes.ALL)
            {
                if(!meta.getMemberType(name).isArray())
                {
                    IBaseValue value1 = c1.getBaseValue(name);
                    IBaseValue value2 = c2.getBaseValue(name);

                    if(value1 == null || value2 == null)
                    {
                        throw new IllegalArgumentException("Key attribute " + name + " couldn't be null");
                    }

                    if(!meta.getMemberType(name).isComplex())
                    {
                        boolean tmp = (value1.getValue().equals(value2.
                                getValue()));

                        if (tmp)
                        {
                            logger.debug("Same: " + value1.getValue() + ", " + value2.getValue());
                        }
                        else
                        {
                            logger.debug("Different: " + value1.getValue() + ", " + value2.getValue());
                        }

                        result = result && tmp;
                    }
                    else
                    {
                        result = result && compare((BaseEntity)value1.getValue(),
                                (BaseEntity)value2.getValue());
                    }
                }
                else
                {
                    BaseSet set1 = (BaseSet)c1.getBaseValue(name).getValue();
                    BaseSet set2 = (BaseSet)c2.getBaseValue(name).getValue();

                    Set<IBaseValue> ar1 = set1.get();
                    Set<IBaseValue> ar2 = set2.get();

                    if(ar1.size() != ar2.size())
                        result = false;

                    boolean arrayResult = (((MetaSet)meta.getMemberType(name)).getArrayKeyType() ==
                            ComplexKeyTypes.ALL);

                    for(IBaseValue v1 : ar1)
                    {
                        if(!meta.getMemberType(name).isComplex())
                        {
                            if(((MetaSet)meta.getMemberType(name)).getArrayKeyType() == ComplexKeyTypes.ALL)
                            {
                                arrayResult = arrayResult && ar2.contains(v1);
                            }
                            else
                            {
                                arrayResult = arrayResult || ar2.contains(v1);
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

                            if(((MetaSet)meta.getMemberType(name)).getArrayKeyType() == ComplexKeyTypes.ALL)
                            {
                                arrayResult = arrayResult && found;
                            }
                            else
                            {
                                arrayResult = arrayResult || found;
                            }
                        }
                    }

                    result = result && arrayResult;
                }
            }
        }

        logger.debug("Result is: " + result);

        return result;
    }
}
