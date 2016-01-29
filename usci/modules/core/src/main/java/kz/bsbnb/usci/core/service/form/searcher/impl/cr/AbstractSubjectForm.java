package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 29/01/2016.
 */
public abstract class AbstractSubjectForm extends JDBCSupport implements ISearcherForm {

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    DSLContext context;

    private final Logger logger = LoggerFactory.getLogger(AbstractSubjectForm.class);

    protected int fetchSize = SearchPagination.fetchSize;

    protected void prepareByPageNo(List<Long> subjectIds, List<BaseEntity> entityList, Date reportDate, Long pageNo){
        if(reportDate != null) {
            int i = 0;
            for (Long id : subjectIds) {
                i++;
                try {
                    if ((pageNo - 1) * fetchSize < i && i <= pageNo * fetchSize)
                        entityList.add((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, reportDate));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        } else {
            int i = 0;
            for(Long id : subjectIds) {
                i++;
                try {
                    if ((pageNo - 1) * fetchSize < i && i <= pageNo * fetchSize)
                        entityList.add((BaseEntity) baseEntityLoadDao.load(id));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
