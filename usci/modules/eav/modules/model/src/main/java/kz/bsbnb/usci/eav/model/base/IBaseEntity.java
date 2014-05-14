package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author a.motov
 */
public interface IBaseEntity extends IBaseContainer {

    public IMetaAttribute getMetaAttribute(String attribute);

    public IBaseEntityReportDate getBaseEntityReportDate();

    public void setBaseEntityReportDate(IBaseEntityReportDate baseEntityReportDate);

    public Date getReportDate();

    public void setReportDate(Date reportDate);

    public MetaClass getMeta();

    public void remove(String attribute);

    public Object getEl(String path);

    public List<Object> getElWithArrays(String path);

    public IBaseValue safeGetValue(String name);

    public void calculateValueCount();

    public UUID getUuid();

}
