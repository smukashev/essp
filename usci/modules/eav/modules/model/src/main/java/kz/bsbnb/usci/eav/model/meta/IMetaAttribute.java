package kz.bsbnb.usci.eav.model.meta;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;

/**
 * @author k.tulbassiyev
 */
public interface IMetaAttribute extends IPersistable {
    boolean isKey();

    boolean isNullable();

    boolean isCumulative();

    IMetaType getMetaType();

    void setKey(boolean isKey);

    void setNullable(boolean isNullable);

    void setCumulative(boolean isCumulative);

    void setMetaType(IMetaType metaType);

    String getTitle();

    boolean isFinal();

    void setFinal(boolean isFinal);

    boolean isRequired();

    void setRequired(boolean isRequired);

    boolean isImmutable();

    void setImmutable(boolean immutable);

    boolean isDisabled();

    void setDisabled(boolean immutable);


    String getName();

    void setName(String name);
}
