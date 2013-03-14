package kz.bsbnb.usci.eav_model.model.persistable;

import java.io.Serializable;

/**
 * @author k.tulbassiyev
 */
public interface IPersistable extends Serializable
{
    long getId();

    void setId(long id);
}
