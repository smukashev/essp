package kz.bsbnb.usci.batch.parser.listener.impl;

import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.eav.model.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class ListListener implements IListener
{
    private List<BaseEntity> data = new ArrayList<BaseEntity>();

    @Override
    public void put(BaseEntity baseEntity)
    {
        data.add(baseEntity);
    }

    public List<BaseEntity> getData()
    {
        return data;
    }
}
