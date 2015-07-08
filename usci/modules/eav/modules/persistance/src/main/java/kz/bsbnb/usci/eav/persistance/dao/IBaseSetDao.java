package kz.bsbnb.usci.eav.persistance.dao;

public interface IBaseSetDao extends IPersistableDao {
    boolean deleteRecursive(long baseSetId);
}
