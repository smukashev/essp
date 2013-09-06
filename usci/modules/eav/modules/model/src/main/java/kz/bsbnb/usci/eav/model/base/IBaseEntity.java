package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.Set;

/**
 * @author a.motov
 */
public interface IBaseEntity extends IBaseContainer {

    public IMetaAttribute getMetaAttribute(String attribute);

    public IBaseValue getBaseValue(String attribute);

    public Date getReportDate();

    public void setReportDate(Date reportDate);

    public Date getMaxReportDate();

    public Date getMinReportDate();

    public Set<Date> getAvailableReportDates();

    public void setAvailableReportDates(Set<Date> availableReportDates);

    public MetaClass getMeta();

    public Set<String> getAttributeNames();

    public void remove(String attribute);

    public Object getEl(String path);

    public IBaseValue safeGetValue(String name);
}
