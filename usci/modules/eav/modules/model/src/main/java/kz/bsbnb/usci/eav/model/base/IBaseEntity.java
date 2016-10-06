package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IBaseEntity extends IBaseContainer {
    IMetaAttribute getMetaAttribute(String attribute);

    IBaseEntityReportDate getBaseEntityReportDate();

    void setBaseEntityReportDate(IBaseEntityReportDate baseEntityReportDate);

    Date getReportDate();

    MetaClass getMeta();

    void remove(String attribute);

    OperationType getOperation();

    Object getEl(String path);

    Object getEls(String path);

    List<Object> getElWithArrays(String path);

    Object getInnerValue(String attributeName);

    IBaseValue safeGetValue(String name);

    void calculateValueCount(IBaseEntity baseEntityLoaded);

    UUID getUuid();

    void setBatchId(Long batchId);

    void setIndex(Long index);

    Long getBatchId();

    Long getBatchIndex();

    boolean equalsByKey(IBaseEntity baseEntity);

    boolean containsComplexKey();

    List<IBaseEntity> getKeyElements();

    Set<String> getValidationErrors();

    void setOperation(OperationType type);

    void setUserId(Long userId);

    Long getUserId();

    AdditionalInfo getAddInfo();

    void setAddInfo(IBaseEntity parentEntity, boolean isSet, long attributeId);

    class AdditionalInfo implements Serializable {
        private static final long serialVersionUID = 1L;;
        public IBaseEntity parentEntity;
        public boolean isSet;
        public Long attributeId;

        public AdditionalInfo(IBaseEntity parentEntity, boolean isSet, Long attributeId) {
            this.parentEntity = parentEntity;
            this.isSet = isSet;
            this.attributeId = attributeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AdditionalInfo that = (AdditionalInfo) o;

            if (parentEntity == null || attributeId == null || that.parentEntity == null || that.attributeId == null)
                throw new IllegalStateException(o.toString());

            if (!attributeId.equals(that.attributeId))
                return false;

            if (parentEntity.getId() > 0 && that.parentEntity.getId() > 0 && parentEntity.getId() == that.parentEntity.getId())
                return true;

            if (parentEntity.getId() > 0 || that.parentEntity.getId() > 0)
                return false;

            if (parentEntity.getId() == 0 && that.parentEntity.getId() == 0 && parentEntity.equalsByKey(that.parentEntity))
                return true;

            return false;
        }
    }
}
