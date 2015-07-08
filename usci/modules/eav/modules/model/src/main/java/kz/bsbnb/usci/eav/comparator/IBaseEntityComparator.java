package kz.bsbnb.usci.eav.comparator;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

public interface IBaseEntityComparator {
    boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException;
}
