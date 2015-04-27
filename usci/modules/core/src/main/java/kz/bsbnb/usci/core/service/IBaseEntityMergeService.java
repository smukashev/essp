package kz.bsbnb.usci.core.service;


import java.util.Date;

public interface IBaseEntityMergeService {
    void mergeBaseEntities(long leftEntityId, long rightEntityId, Date leftReportDate, Date rightReportDate,
                           String json, boolean deleteUnused);
}
