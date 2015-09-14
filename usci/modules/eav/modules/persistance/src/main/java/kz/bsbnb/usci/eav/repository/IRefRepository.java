package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;

/**
 * Created by Bauyrzhan.Ibraimov on 10.09.2015.
 */
public interface IRefRepository {

    public void fillRefRepository();
    public IBaseEntity GetRef(long Id, Date reportDate);

    public void SetRef(long Id, Date reportDate, IBaseEntity baseEntity);
}
