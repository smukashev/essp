package kz.bsbnb.usci.eav.persistance.dao;


import kz.bsbnb.usci.eav.model.Batch;

public interface IBatchDao {

    Batch load(long id);

    long save(Batch batch);

}
