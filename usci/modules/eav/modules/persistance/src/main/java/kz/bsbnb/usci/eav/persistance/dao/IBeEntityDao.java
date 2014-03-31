package kz.bsbnb.usci.eav.persistance.dao;

/**
 *
 */
public interface IBeEntityDao extends IPersistableDao {

    public boolean isUsed(long baseEntityId);

}
