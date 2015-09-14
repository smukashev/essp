package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
/**
 * Created by Bauyrzhan.Ibraimov on 09.09.2015.
 */


@Component
@Scope(value = "singleton")
public class RefRepository  extends JDBCSupport implements IRefRepository {
    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Autowired
    private DSLContext context;

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    private class BaseEntityKey {
        private long id;
        private Date reportDate;

        public BaseEntityKey(long id, Date reportDate) {
            this.id = id;
            this.reportDate = reportDate;
        }

        public long getId() {
            return id;
        }

        public Date getReportDate() {
            return reportDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BaseEntityKey that = (BaseEntityKey) o;

            if (id != that.id) return false;
            if (!reportDate.equals(that.reportDate)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + reportDate.hashCode();
            return result;
        }
    }

    private HashMap<BaseEntityKey, IBaseEntity> cache = new HashMap<>();

   public void fillRefRepository()
    {
        Select select = context
                .select(EAV_BE_ENTITIES.ID, EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                .from(EAV_BE_ENTITIES, EAV_BE_ENTITY_REPORT_DATES, EAV_M_CLASSES)
                .where(EAV_BE_ENTITIES.ID.equal(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID))
                .and(EAV_BE_ENTITIES.CLASS_ID.equal(EAV_M_CLASSES.ID))
                .and(EAV_M_CLASSES.IS_REFERENCE.equal(DataUtils.convert(true)));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        for(Map<String, Object> row: rows)
        {
            long id = ((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue();

            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE.getName()));

            IBaseEntityReportDateDao baseEntityReportDateDao =
                    persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

            Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, reportDate);

            cache.put(new BaseEntityKey(id, reportDate), baseEntityLoadDao.load(id, maxReportDate,reportDate));

        }
    }

    public IBaseEntity GetRef(long Id, Date reportDate)
    {
        return cache.get(new BaseEntityKey(Id, reportDate));
    }

    public void SetRef(long Id, Date reportDate, IBaseEntity baseEntity)
    {
        cache.put(new BaseEntityKey(Id, reportDate), baseEntity);
    }
}
