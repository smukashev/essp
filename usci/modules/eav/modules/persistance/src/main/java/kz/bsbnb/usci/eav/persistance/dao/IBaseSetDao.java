package kz.bsbnb.usci.eav.persistance.dao;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
public interface IBaseSetDao extends IPersistableDao {

    public boolean deleteRecursive(long baseSetId);

}