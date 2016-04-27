package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class RefRepository extends JDBCSupport implements IRefRepository {
    private final HashMap<BaseEntityKey, IBaseEntity> cache = new HashMap<>();

    @Override
    public long findRef(IBaseEntity baseEntity) {
        boolean synced;
        int syncCounter = 0;
        do {
            try {
                for (Map.Entry<BaseEntityKey, IBaseEntity> entry : cache.entrySet()) {
                    if (baseEntity.equalsByReference(entry.getValue()))
                        return entry.getValue().getId();
                }
                synced = false;
            } catch (Exception e) {
                synced = true;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        } while (synced && syncCounter++ < 1000);


        return 0;
    }

    @Override
    public IBaseEntity getRef(long id, Date reportDate) {
        IBaseEntity entity = cache.get(new BaseEntityKey(id, reportDate));

        if (entity == null)
            return null;

        if (entity.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E291));

        return entity;
    }

    public void setRef(long id, Date reportDate, IBaseEntity baseEntity) {
        cache.put(new BaseEntityKey(id, reportDate), baseEntity);
    }

    @Override
    public void delRef(long id) {
        Iterator<Map.Entry<BaseEntityKey, IBaseEntity>> entryIterator = cache.entrySet().iterator();
        while (entryIterator.hasNext()) {
            if (id == entryIterator.next().getKey().getId())
                entryIterator.remove();
        }
    }

    private class BaseEntityKey {
        private long id;
        private Date reportDate;

        BaseEntityKey(long id, Date reportDate) {
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

        @Override
        public String toString() {
            return "BaseEntityKey{" +
                    "id=" + id +
                    ", reportDate=" + DataTypes.dateFormatDot.format(reportDate) +
                    '}';
        }
    }
}
