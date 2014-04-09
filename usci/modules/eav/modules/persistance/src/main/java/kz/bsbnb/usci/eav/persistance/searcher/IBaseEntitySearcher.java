package kz.bsbnb.usci.eav.persistance.searcher;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.ArrayList;

public interface IBaseEntitySearcher
{
    public String getClassName();
    public Long findSingle(BaseEntity entity);
    public ArrayList<Long> findAll(BaseEntity entity);
}
