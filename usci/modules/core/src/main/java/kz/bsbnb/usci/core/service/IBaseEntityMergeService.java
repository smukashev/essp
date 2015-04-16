package kz.bsbnb.usci.core.service;


import java.util.Date;

/**
 * Created by dakkuliyev on 7/20/14.
 */
public interface IBaseEntityMergeService {

    public void mergeBaseEntities(long leftEntityId, long rightEntityId, Date leftReportDate, Date rightReportDate, String json, boolean deleteUnused);

}
