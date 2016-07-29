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
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntitySearcher;
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
public class PortfolioFormImpl extends JDBCSupport implements ISearcherForm {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IBaseEntitySearcherPool searcherPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    ImprovedBaseEntitySearcher baseEntitySearcher;

    @Override
    public List<Pair> getMetaClasses(long userId) {
        MetaClass subject = metaClassRepository.getMetaClass("portfolio");
        List<Pair> ret = new LinkedList<>();
        ret.add(new Pair(subject.getId(), subject.getClassName(), "портфель однородных кредитов"));
        return ret;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass, String prefix) {
        return null;
    }

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix, long creditorId) {

        Date reportDate = (Date) DataTypes.getCastObject(DataTypes.DATE, parameters.get("date"));

        BaseEntity portfolioData = new BaseEntity(metaClassRepository.getMetaClass("portfolio_data"), reportDate, creditorId);
        BaseEntity creditor = new BaseEntity(metaClassRepository.getMetaClass("ref_creditor"), reportDate, creditorId);
        creditor.setId(creditorId);
        portfolioData.put("creditor", new BaseValue(creditorId, reportDate, creditor));

        List<Long> portfolioDatas = baseEntitySearcher.findAll(portfolioData,creditorId);
        List<BaseEntity> entities = new LinkedList<>();
        ISearchResult ret = new NonPaginableSearchResult();

        for (Long portfolioDataId : portfolioDatas) {
            entities.add(((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(portfolioDataId, reportDate)));
        }
        ret.setData(entities);

        return ret;
    }
}
