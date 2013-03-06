package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;

import java.util.ArrayList;

public interface IBaseEntitySearcher
{
    public String getClassName();
    public BaseEntity findSingle(BaseEntity meta);
    public ArrayList<BaseEntity> findAll(BaseEntity meta);
}
