package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Component
public class SubjectDocFormImpl extends JDBCSupport implements ISearcherForm {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IBaseEntitySearcherPool searcherPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Override
    public List<Pair> getMetaClasses(long userId) {
        MetaClass subject = metaClassRepository.getMetaClass("subject");
        List<Pair> ret = new LinkedList<>();
        ret.add(new Pair(subject.getId(), subject.getClassName(), "субъект кредитной истории по документу"));
        return ret;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass, String prefix) {
        return null;
    }

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix, long creditorId) {
        if( !"subject".equals(metaClass.getClassName()))
            throw new RuntimeException(Errors.compose(Errors.E231));

        long numDocs = Long.parseLong(parameters.get("childCnt"));
        Date reportDate = reportDate = (Date) DataTypes.getCastObject(DataTypes.DATE, parameters.get("date"));
        BaseEntity subject = new BaseEntity(metaClassRepository.getMetaClass("subject"), reportDate, creditorId);
        BaseSet docs = new BaseSet(metaClassRepository.getMetaClass("document"), creditorId);
        int successfullDocCount = 0;


        for(int i=0;i<numDocs;i++) {
            IBaseEntity document = new BaseEntity(metaClassRepository.getMetaClass("document"), reportDate, creditorId);
            IBaseEntity docType = baseEntityLoadDao.load(Long.parseLong(parameters.get("doc_type" + i)));
            document.put("doc_type", new BaseValue(creditorId, reportDate, docType));
            document.put("no", new BaseValue(creditorId, reportDate, parameters.get("no" + i)));
            //if(docType.getEl("is_identification") == true) {
            Long docId = searcherPool.getSearcher("document").findSingle((BaseEntity) document, creditorId);
            if(docId != null) {
                document.setId(docId);
                docs.put(new BaseValue(creditorId, reportDate, document));
                successfullDocCount ++;
            }
        }

        boolean isCreditor = parameters.get("subjectType").equals("isCreditor");
        boolean isPerson = parameters.get("subjectType").equals("isPerson");
        boolean isOrganization = parameters.get("subjectType").equals("isOrganization");

        subject.put("docs", new BaseValue(creditorId, reportDate, docs));
        subject.put("is_creditor", new BaseValue(creditorId, reportDate, isCreditor));
        subject.put("is_person", new BaseValue(creditorId, reportDate, isPerson));
        subject.put("is_organization", new BaseValue(creditorId, reportDate, isOrganization));
        List<BaseEntity> entities = new LinkedList<>();
        ISearchResult ret = new NonPaginableSearchResult();

        if(successfullDocCount > 0) {
            Long id = searcherPool.getSearcher("subject").findSingle(subject, creditorId);
            if (id != null) {
                entities.add((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, reportDate));
            }
        }

        ret.setData(entities);
        return ret;
    }
}
