package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.PaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class CreditFormImpl extends JDBCSupport implements ISearcherForm {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IBaseEntitySearcherPool searcherPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    @Qualifier("devSearcherForm")
    ISearcherForm devForm;

    @Override
    public List<Pair> getMetaClasses(long userId) {
        MetaClass credit = metaClassRepository.getMetaClass("credit");
        List<Pair> ret = new LinkedList<>();
        ret.add(new Pair(credit.getId(), credit.getClassName(), "договор по номеру и дате"));
        return ret;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass, String prefix) {
        return null;
    }

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix, long creditorId) {
        if(!metaClass.getClassName().equals("credit"))
            throw new RuntimeException(Errors.compose(Errors.E231));

        if(parameters.get("dev") != null) {
            return devForm.search(parameters, metaClass, prefix, creditorId);
        }

        Date reportDate = new Date();
        if(parameters.get("date") != null)
            reportDate = (Date) DataTypes.getCastObject(DataTypes.DATE, parameters.get("date"));
        IBaseEntitySearcher searcher = searcherPool.getSearcher("credit");
        ISearchResult result = new PaginableSearchResult();
        BaseEntity credit = new BaseEntity(metaClass, reportDate, creditorId);

        BaseEntity primaryContract = new BaseEntity(metaClassRepository.getMetaClass("primary_contract"),
                reportDate, creditorId);
        primaryContract.put("no", new BaseValue(creditorId, reportDate, parameters.get("no")));
        primaryContract.put("date", new BaseValue(creditorId, reportDate,
                DataTypes.getCastObject(DataTypes.DATE, parameters.get("pDate"))));
        credit.put("primary_contract", new BaseValue(creditorId, reportDate, primaryContract));

        BaseEntity creditor = new BaseEntity(metaClassRepository.getMetaClass("ref_creditor"), reportDate, creditorId);
        creditor.setId(creditorId);
        credit.put("creditor",new BaseValue(creditorId, reportDate, creditor));

        baseEntityProcessorDao.prepare(credit, creditorId);
        Long id = credit.getId();

        List<BaseEntity> entityList = new ArrayList<>();
        result.setData(entityList);
        SearchPagination pagination = new SearchPagination(0);
        result.setPagination(pagination);
        if(id != null && id > 0 ) {
            entityList.add((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, reportDate));
            pagination.setTotalCount(1);
        }
        return result;
    }
}
