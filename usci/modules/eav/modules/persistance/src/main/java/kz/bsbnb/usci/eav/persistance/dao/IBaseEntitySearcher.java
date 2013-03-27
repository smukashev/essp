package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.ArrayList;

public interface IBaseEntitySearcher
{
    public String getClassName();
    public BaseEntity findSingle(BaseEntity entity);
    public ArrayList<BaseEntity> findAll(BaseEntity entity);
}
