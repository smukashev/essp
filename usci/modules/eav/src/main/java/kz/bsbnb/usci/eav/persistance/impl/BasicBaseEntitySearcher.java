package kz.bsbnb.usci.eav.persistance.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;

import java.util.ArrayList;
import java.util.Set;

/**
 * Used to compare BaseEntity in memory, and to retrieve BaseEntities from storage by example.
 */
public class BasicBaseEntitySearcher implements IBaseEntitySearcher {
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

    @Override
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException {
        //todo: implement
        /*if(c1.getMeta() != c2.getMeta())
            return false;

        MetaClass meta = c1.getMeta();

        boolean result = (meta.getComplexKeyType() == ComplexKeyTypes.ALL);

        Set<String> names = meta.getMemberNames();

        for(String name : names)
        {
            if(!meta.getMemberType(name).isKey())
                continue;

            if(meta.getComplexKeyType() == ComplexKeyTypes.ALL)
            {
                if(!meta.getMemberType(name).isArray())
                {
                    IBatchValue value1 = c1.getBaseValue(name);
                    IBatchValue value2 = c2.getBaseValue(name);

                    if(value1 == null || value2 == null)
                    {
                        throw new IllegalArgumentException("Key attribute " + name + " couldn't be null");
                    }

                    if(!meta.getMemberType(name).isComplex())
                    {
                        result = result && (value1.getValue().equals(value2.
                                getValue()));
                    }
                    else
                    {
                        result = result && compare((BaseEntity)value1.getValue(),
                                (BaseEntity)value2.getValue());
                    }
                }
                else
                {
                    ArrayList<IBatchValue> ar1 = c1.getBatchValueArray(name);
                    ArrayList<IBatchValue> ar2 = c2.getBatchValueArray(name);

                    if(ar1.size() != ar2.size())
                        result = false;

                    boolean arrayResult = (((MetaValueArray)meta.getMemberType(name)).getArrayKeyType() ==
                            ComplexKeyTypes.ALL);

                    for(int i = 0; i < ar1.size(); i++)
                    {
                        if(!meta.getMemberType(name).isComplex())
                        {
                            if(((MetaValueArray)meta.getMemberType(name)).getArrayKeyType() == ComplexKeyTypes.ALL)
                            {
                                arrayResult = arrayResult && ar1.get(i).getValue().equals(ar2.get(i).getValue());
                            }
                            else
                            {
                                arrayResult = arrayResult || ar1.get(i).getValue().equals(ar2.get(i).getValue());
                            }
                        }
                        else
                        {
                            if(((MetaValueArray)meta.getMemberType(name)).getArrayKeyType() == ComplexKeyTypes.ALL)
                            {
                                arrayResult = arrayResult && compare((BaseEntity)ar1.get(i).getValue(),
                                        (BaseEntity)ar2.get(i).getValue());
                            }
                            else
                            {
                                arrayResult = arrayResult || compare((BaseEntity)ar1.get(i).getValue(),
                                        (BaseEntity)ar2.get(i).getValue());
                            }
                        }
                    }
                }
            }
        } */

        return true;
    }
}
