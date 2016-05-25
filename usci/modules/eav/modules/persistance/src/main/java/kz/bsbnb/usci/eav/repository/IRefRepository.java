package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

public interface IRefRepository {
    long prepareRef(IBaseEntity baseEntity);
}
