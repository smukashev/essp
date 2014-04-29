package kz.bsbnb.usci.eav.persistance.dao;

/**
 * @author alexandr.motov
 */
public interface IBaseSetDao extends IPersistableDao {

    public boolean deleteRecursive(long baseSetId);

}
