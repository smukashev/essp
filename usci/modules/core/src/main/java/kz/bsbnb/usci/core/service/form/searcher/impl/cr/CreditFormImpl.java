package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
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
            throw new RuntimeException("incorrect use");
        Date reportDate = new Date();
        IBaseEntitySearcher searcher = searcherPool.getSearcher("credit");
        ISearchResult result = new NonPaginableSearchResult();
        BaseEntity credit = new BaseEntity(metaClass, reportDate);

        BaseEntity primaryContract = new BaseEntity(metaClassRepository.getMetaClass("primary_contract"), reportDate);
        primaryContract.put("no", new BaseValue(creditorId, reportDate, parameters.get("no")));
        primaryContract.put("date", new BaseValue(creditorId, reportDate, DataTypes.fromString(DataTypes.DATE, parameters.get("date"))));
        credit.put("primary_contract", new BaseValue(creditorId, reportDate, primaryContract));
        Long id = searcher.findSingle(credit, creditorId);
        if(id != null) {
            List<BaseEntity> res = new ArrayList<>();
            res.add((BaseEntity)baseEntityLoadDao.load(id));
            result.setData(res);
        }
        return result;
    }
}
