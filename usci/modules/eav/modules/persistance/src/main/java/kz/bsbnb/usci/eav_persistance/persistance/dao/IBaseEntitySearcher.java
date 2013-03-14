package kz.bsbnb.usci.eav_persistance.persistance.dao;

import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;

import java.util.ArrayList;

public interface IBaseEntitySearcher
{
    public String getClassName();
    public BaseEntity findSingle(BaseEntity meta);
    public ArrayList<BaseEntity> findAll(BaseEntity meta);
}
