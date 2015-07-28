package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class EavGlobal extends Persistable {
    private String type;
    private String code;
    private String value;
    private String description;

    public EavGlobal(Long id, String type, String code, String value, String description) {
        this.id = id;
        this.type = type;
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EavGlobal eavGlobal = (EavGlobal) o;

        if (!getType().equals(eavGlobal.getType())) return false;
        if (!getCode().equals(eavGlobal.getCode())) return false;
        return getValue().equals(eavGlobal.getValue());

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getCode().hashCode();
        result = 31 * result + getValue().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EavGlobal{" +
                "type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
