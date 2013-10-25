package com.bsbnb.creditregistry.portlets.crosscheck.model;

import java.math.BigInteger;

/**
 * Сущность для работы со служебным справочником
 * @author Alexandr.Motov
 */
//@Entity
//@Table(name="SHARED", schema="REF")
//@SequenceGenerator(name="SHARED_SEQUENCE", allocationSize=1, initialValue=1000, schema="REF", sequenceName="SHARED_SEQ")
public class Shared {
    private static final long serialVersionUID = -5343254753555605569L;
    
    //<editor-fold defaultstate="collapsed" desc="Simple fields">
//    @Id
//    @GeneratedValue(generator="SHARED_SEQUENCE", strategy=GenerationType.SEQUENCE)
//    @Column(name="ID", unique=true, nullable=false)
    private BigInteger id;
//    @Column(name="CODE")
    private String code;
//    @Column(name="TYPE_", nullable=false)
    private String type;
//    @Column(name="NAME_RU")
    private String nameRu;
//    @Column(name="NAME_KZ")
    private String nameKz;
//    @Column(name="ORDER_NUM")
    private Integer orderNum;
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(BigInteger id) {
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
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    //</editor-fold>
    
}
