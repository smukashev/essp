package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

public interface IRefRepository {
    long prepareRef(final IBaseEntity baseEntity);

    void installRef(final IBaseEntity entity);

    IBaseEntity get(IBaseEntity entity);
}
