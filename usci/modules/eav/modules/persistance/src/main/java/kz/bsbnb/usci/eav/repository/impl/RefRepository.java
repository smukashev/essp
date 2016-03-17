package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class RefRepository extends JDBCSupport implements IRefRepository {
    private final HashMap<BaseEntityKey, IBaseEntity> cache = new HashMap<>();

    public IBaseEntity getRef(long Id, Date reportDate) {
        return cache.get(new BaseEntityKey(Id, reportDate));
    }

    public void setRef(long Id, Date reportDate, IBaseEntity baseEntity) {
        cache.put(new BaseEntityKey(Id, reportDate), baseEntity);
    }

    public void delRef(long Id, Date reportDate) {
        cache.remove(new BaseEntityKey(Id, reportDate));
    }

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
}
