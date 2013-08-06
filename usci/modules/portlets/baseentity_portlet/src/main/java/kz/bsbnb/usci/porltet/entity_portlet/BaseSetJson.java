package kz.bsbnb.usci.porltet.entity_portlet;


import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class BaseSetJson
{


    private IMetaType meta;

    private Set<BaseValueJson> data = new HashSet<BaseValueJson>();


    public BaseSetJson(IMetaType meta)
    {
        this.meta = meta;
    }

    public BaseSetJson(long id, IMetaType meta)
    {

        this.meta = meta;
    }

    public IMetaType getMemberType(String name)
    {
        return meta;
    }

    public IMetaType getMemberType()
    {
        return meta;
    }


    public void put(String name,BaseValueJson value)
    {
        data.add(value);
    }

    public void put(BaseValueJson value)
    {
        data.add(value);
    }


    public Set<BaseValueJson> get()
    {
        return data;
    }

    public int getElementCount()
    {
        return data.size();
    }

    @Override
    public String toString()
    {
        String str = "[";
        boolean first = true;

        for (BaseValueJson value : data) {
            if (first) {
                str += value.getValue().toString();
                first = false;
            } else {
                str += ", " + value.getValue().toString();
            }
        }

        str += "]";

        return str;
    }

    public Object getElSimple(String filter)
    {
        if (meta.isComplex() || meta.isSet())
        {
            throw new IllegalArgumentException("Get simple attribute method called for complex attribute or array");
        }

        for (BaseValueJson value : data)
        {
            Object innerValue = value.getValue();
            if (innerValue == null)
            {
                continue;
            }

//            if (((BaseValueJson)value).equalsToString(filter, ((MetaValue)meta).getTypeCode()))
                return innerValue;
        }

        return null;
    }

    public Object getElComplex(String filter)
    {
        if (!meta.isComplex() || meta.isSet())
        {
            throw new IllegalArgumentException("Get complex attribute method called for simple attribute or array");
        }

        StringTokenizer tokenizer = new StringTokenizer(filter, ",");

        Object valueOut = null;
        HashMap<String, String> params = new HashMap<String, String>();

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();

            StringTokenizer innerTokenizer = new StringTokenizer(token, "=");

            String fieldName = innerTokenizer.nextToken().trim();
            if (!innerTokenizer.hasMoreTokens())
                throw new IllegalStateException("Field value expected.");

            String fieldValue = innerTokenizer.nextToken().trim();

            params.put(fieldName, fieldValue);
        }

        for (BaseValueJson value : data)
        {
            Object innerValue = value.getValue();
            if (innerValue == null)
            {
                continue;
            }

            if (((BaseEntity)innerValue).equalsToString(params))
                return innerValue;
        }

        return valueOut;
    }

    public Object getEl(String filter)
    {
        if (meta.isComplex())
            return getElComplex(filter);
        return getElSimple(filter);
    }


}
