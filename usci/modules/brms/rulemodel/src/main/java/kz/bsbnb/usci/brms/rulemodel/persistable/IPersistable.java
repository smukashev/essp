package kz.bsbnb.usci.brms.rulemodel.persistable;

import java.io.Serializable;

/**
 * @author abukabayev
 */
public interface IPersistable extends Serializable {

    long getId();
    void setId(long id);
}
