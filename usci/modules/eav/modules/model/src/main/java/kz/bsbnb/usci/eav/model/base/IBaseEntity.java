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

    AdditionalInfo getAdditionalInfo();

    class AdditionalInfo implements Serializable {
        public boolean isSet;
        public Long parentId;
        public Long attributeId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AdditionalInfo that = (AdditionalInfo) o;

            if (isSet != that.isSet) return false;
            if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
            return attributeId != null ? attributeId.equals(that.attributeId) : that.attributeId == null;

        }

        @Override
        public int hashCode() {
            int result = (isSet ? 1 : 0);
            result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
            result = 31 * result + (attributeId != null ? attributeId.hashCode() : 0);
            return result;
        }
    }
}
