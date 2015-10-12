package kz.bsbnb.usci.cr.model;

import java.io.Serializable;

public class Shared implements Serializable {
    private static final long serialVersionUID = 8656348715892462142L;

    private long id;

    private String code;

    private String type;

    private String nameRu;

    private String nameKz;

    private Integer orderNum;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameKz() {
        return nameKz;
    }

    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }

    public String getStringIdentifier() {
        return code;
    }

    @Override
    public String toString() {
        return "Shared {id=" + getId() + ", code=" + getCode() + ", nameRu=" + getNameRu() + ", nameKz=" +
                getNameKz() + ", type=" + getType() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass() && this.id == ((Shared) obj).id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) this.id;
        return hash;
    }
}
