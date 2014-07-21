package kz.bsbnb.usci.core.service;



/**
 * Created by dakkuliyev on 7/20/14.
 */
public interface IBaseEntityMergeService {

    public void mergeBaseEntities(long leftEntityId, long rightEntityId, String json);

}
