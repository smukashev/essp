package kz.bsbnb.usci.eav.persistance.dao;

/**
 *
 */
public interface IBaseEntityDao extends IPersistableDao {

    public boolean isUsed(long baseEntityId);

}
