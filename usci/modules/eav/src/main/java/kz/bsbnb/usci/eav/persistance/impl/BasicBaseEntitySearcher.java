package kz.bsbnb.usci.eav.persistance.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;

import java.util.ArrayList;

public abstract class BasicBaseEntitySearcher implements IBaseEntitySearcher {
    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public abstract BaseEntity findSingle(BaseEntity meta);

    @Override
    public abstract ArrayList<BaseEntity> findAll(BaseEntity meta);

    @Override
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException {

        // todo: implement
//        if(c1.getMeta() != c2.getMeta())
//            return false;
//
//        MetaClass meta = c1.getMeta();
//
//        boolean result = (meta.getComplexKeyType() == ComplexKeyTypes.ALL);
//
//        Set<String> names = meta.getMemberNames();
//
//        for(String name : names)
//        {
//            if(!meta.getMemberType(name).isKey())
//                continue;
//            if(meta.getComplexKeyType() == ComplexKeyTypes.ALL)
//            {
//                if(!meta.getMemberType(name).isArray())
//                {
//                    if(!meta.getMemberType(name).isComplex())
//                    {
//                        result = result && (c1.getBatchValue(name).getValue().equals(c2.getBatchValue(name).
//                                getValue()));
//                    }
//                    else
//                    {
//                        result = result && compare((BaseEntity)c1.getBatchValue(name).getValue(),
//                                (BaseEntity)c2.getBatchValue(name).getValue());
//                    }
//                }
//                else
//                {
//                    ArrayList<IBaseValue> ar1 = c1.getBatchValueArray(name);
//                    ArrayList<IBaseValue> ar2 = c2.getBatchValueArray(name);
//
//                    if(ar1.size() != ar2.size())
//                        result = false;
//
//                    boolean arrayResult = (((MetaValueArray)meta.getMemberType(name)).getArrayKeyType() ==
//                            ComplexKeyTypes.ALL);
//
//                    for(int i = 0; i < ar1.size(); i++)
//                    {
//                        if(!meta.getMemberType(name).isComplex())
//                        {
//                            if(((MetaValueArray)meta.getMemberType(name)).getArrayKeyType() == ComplexKeyTypes.ALL)
//                            {
//                                arrayResult = arrayResult && ar1.get(i).getValue().equals(ar2.get(i).getValue());
//                            }
//                            else
//                            {
//                                arrayResult = arrayResult || ar1.get(i).getValue().equals(ar2.get(i).getValue());
//                            }
//                        }
//                        else
//                        {
//                            if(((MetaValueArray)meta.getMemberType(name)).getArrayKeyType() == ComplexKeyTypes.ALL)
//                            {
//                                arrayResult = arrayResult && compare((BaseEntity)ar1.get(i).getValue(),
//                                        (BaseEntity)ar2.get(i).getValue());
//                            }
//                            else
//                            {
//                                arrayResult = arrayResult || compare((BaseEntity)ar1.get(i).getValue(),
//                                        (BaseEntity)ar2.get(i).getValue());
//                            }
//                        }
//                    }
//                }
//            }
//        }

        return true;
    }
}
