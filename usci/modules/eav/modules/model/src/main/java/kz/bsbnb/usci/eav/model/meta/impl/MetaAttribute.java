package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class MetaAttribute extends Persistable implements IMetaAttribute {
    private static final long serialVersionUID = 1L;

    private String name;

    private String title;

    private IMetaType metaType;

    private boolean isKey = false;

    private boolean isOptionKey = false;

    private boolean isFinal = false;

    private boolean isRequired = false;

    private boolean isImmutable = false;

    private boolean isCumulative = false;

    private boolean isNullable = true;

    private boolean isDisabled = false;

    public MetaAttribute(boolean isKey, boolean isNullable, IMetaType metaType) {
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
        this.metaType = metaType;
    }

    public MetaAttribute(IMetaType metaType) {
        this.isKey = false;
        this.isNullable = true;
        this.metaType = metaType;
    }

    public MetaAttribute(long id, boolean isKey, boolean isNullable) {
        super(id);
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
    }

    @Override
    public boolean isKey() {
        return isKey;
    }

    @Override
    public boolean isOptionalKey() {
        return isOptionKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
    }

    @Override
    public void setOptionalKey(boolean isOptionKey) {
        this.isOptionKey = isOptionKey;
    }

    @Override
    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean isNullable) {
        this.isNullable = isNullable && !isKey;
    }

    @Override
    public IMetaType getMetaType() {
        return metaType;
    }

    public void setMetaType(IMetaType metaType) {
        this.metaType = metaType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        if (title != null) return title;
        return getName();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setImmutable(boolean isImmutable) {
        this.isImmutable = isImmutable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isCumulative() {
        return isCumulative;
    }

    @Override
    public void setCumulative(boolean isCumulative) {
        this.isCumulative = isCumulative;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MetaAttribute that = (MetaAttribute) o;

        if (isKey != that.isKey) return false;
        if (isOptionKey != that.isOptionKey) return false;
        if (isFinal != that.isFinal) return false;
        if (isRequired != that.isRequired) return false;
        if (isImmutable != that.isImmutable) return false;
        if (isCumulative != that.isCumulative) return false;
        if (isNullable != that.isNullable) return false;
        if (isDisabled != that.isDisabled) return false;
        if (!name.equals(that.name)) return false;
        if (!title.equals(that.title)) return false;
        return metaType.equals(that.metaType);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + metaType.hashCode();
        result = 31 * result + (isKey ? 1 : 0);
        result = 31 * result + (isOptionKey ? 1 : 0);
        result = 31 * result + (isFinal ? 1 : 0);
        result = 31 * result + (isRequired ? 1 : 0);
        result = 31 * result + (isImmutable ? 1 : 0);
        result = 31 * result + (isCumulative ? 1 : 0);
        result = 31 * result + (isNullable ? 1 : 0);
        result = 31 * result + (isDisabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetaAttribute{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", metaType=" + metaType +
                ", isKey=" + isKey +
                ", isOptionKey=" + isOptionKey +
                ", isFinal=" + isFinal +
                ", isRequired=" + isRequired +
                ", isImmutable=" + isImmutable +
                ", isCumulative=" + isCumulative +
                ", isNullable=" + isNullable +
                ", isDisabled=" + isDisabled +
                '}';
    }
}
