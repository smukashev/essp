package kz.bsbnb.usci.batch.parser.listener;

import kz.bsbnb.usci.eav.model.BaseEntity;

/**
 * @author k.tulbassiyev
 */
public interface IListener
{
    public void put(BaseEntity baseEntity);
}
