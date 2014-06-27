package kz.bsbnb.usci.eav.manager.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dakkuliyev on 6/23/14.
 */
public class MergeManagerKey<T>
{
    private List<T> keyStore;


    public MergeManagerKey(T attribute)
    {
        keyStore = new ArrayList<T>();
        keyStore.add(attribute);
    }

    public MergeManagerKey(T leftEntity, T rightEntity)
    {
        keyStore = new ArrayList<T>();
        keyStore.add(leftEntity);
        keyStore.add(rightEntity);
    }

    public List<T> getKeyStore()
    {
        return keyStore;
    }

    @Override
    public int hashCode()
    {
        String keyHash = null;
        for(T key : keyStore)
        {
            keyHash += key.toString();
        }
        return keyHash.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        MergeManagerKey key = (MergeManagerKey) obj;
        if(key.getKeyStore().size() != this.keyStore.size())
        {
            return false;
        }
        for(int i=0; i<key.getKeyStore().size(); i++)
        {
            if(key.getKeyStore().get(i) != keyStore.get(i))
            {
                return false;
            }
        }
        return true;
    }
}