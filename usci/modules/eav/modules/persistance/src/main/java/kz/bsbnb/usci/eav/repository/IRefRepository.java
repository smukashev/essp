package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;

public interface IRefRepository {
    long findRef(IBaseEntity baseEntity);

    IBaseEntity getRef(long Id, Date reportDate);

    void setRef(long id, Date reportDate, IBaseEntity baseEntity);

    void delRef(long id);
}