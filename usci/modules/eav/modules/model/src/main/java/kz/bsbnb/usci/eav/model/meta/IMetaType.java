package kz.bsbnb.usci.eav.model.meta;

import java.io.Serializable;

public interface IMetaType extends Serializable {
    boolean isSet();

    boolean isComplex();

    String toString(String prefix);

    String toJava(String prefix);

    boolean isReference();

    void setReference(boolean value);
}