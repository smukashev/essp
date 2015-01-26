package kz.bsbnb.usci.eav.model.base;

/**
 * @author alexandr.motov
 */
public interface IBaseSetValue<T> extends IBaseValue<T> {

    public enum HistoryType { RESTRICTED_BY_SET, RESTRICTED_BY_ENTITY, INHERITED }

    public HistoryType getHistoryType();

    public void setHistoryType(HistoryType historyType);

}
