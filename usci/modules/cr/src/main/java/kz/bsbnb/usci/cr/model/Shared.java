package kz.bsbnb.usci.cr.model;

//import com.bsbnb.creditregistry.dm.ref.shared.SharedCode;
//import com.bsbnb.creditregistry.dm.ref.shared.SharedType;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * Сущность для работы со служебным справочником
 * @author Alexandr.Motov
 */
public class Shared implements Serializable {
    private long id;
    private String code;
    private String type;
    private String nameRu;
    private String nameKz;
    private Integer orderNum;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * @return the code
     */
        public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the type
     */
        public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the orderNum
     */
    public Integer getOrderNum() {
        return orderNum;
    }

    /**
     * @param orderNum the orderNum to set
     */
    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    /**
     * @return the nameRu
     */
        public String getNameRu() {
        return nameRu;
    }

    /**
     * @param nameRu the nameRu to set
     */
    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    /**
     * @return the nameKz
     */
        public String getNameKz() {
        return nameKz;
    }

    /**
     * @param nameKz the nameKz to set
     */
    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }
    
    public String getStringIdentifier() {
        return code;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="toString, hashCode, equals">
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{id=");
        sb.append(getId());
        sb.append(", code=");
        sb.append(getCode());
        sb.append(", nameRu=");
        sb.append(getNameRu());
        sb.append(", nameKz=");
        sb.append(getNameKz());
        sb.append(", type=");
        sb.append(getType());
        sb.append("}");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Shared other = (Shared) obj;
        if (this.id != other.id && !(this.id == other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int)this.id;
        return hash;
    }


//    public boolean equalCode(SharedCode sharedCode) {
//        return this.getCode().equals(sharedCode.getCode());
//    }
//
//    public boolean equalType(SharedType sharedType) {
//        return this.getCode().equals(sharedType.getType());
//    }
//
//    public boolean equalCodeType(SharedCode sharedCode, SharedType sharedType) {
//        return equalCode(sharedCode) && equalType(sharedType);
//    }

}
