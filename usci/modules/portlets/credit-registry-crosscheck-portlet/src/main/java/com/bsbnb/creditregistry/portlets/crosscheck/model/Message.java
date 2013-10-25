package com.bsbnb.creditregistry.portlets.crosscheck.model;

import java.io.Serializable;
import java.math.BigInteger;


/**
 * Сущность представляет собой описание системной ошибки имеющий идентификатор
 * (id), код системной ошибки (code), тект на русском языке (nameRu), текст на
 * казахском языке (nameKz) и тип ошибки (errorType).
 *
 * @see {@link com.bsbnb.creditregistry.ejb.maintenance.ErrorMessageBean},
 * {@link com.bsbnb.creditregistry.ejb.maintenance.api.ErrorMessageCommonBusiness},
 * {@link com.bsbnb.creditregistry.ejb.maintenance.api.ErrorMessageLocalBusiness},
 * {@link com.bsbnb.creditregistry.ejb.maintenance.api.ErrorMessageRemoteBusiness},
 * {@link com.bsbnb.creditregistry.Message.maintenance.dm.ErrorMessage};
 *
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a>
 */
//@Entity
//@SequenceGenerator(name = "MESSAGE_SEQUENCE", sequenceName = "MESSAGE_SEQ", schema = "MAINTENANCE", initialValue = 1000, allocationSize = 1)
//@Table(name = "MESSAGE", schema = "MAINTENANCE")
public class Message implements Serializable {
    private static final long serialVersionUID = -8626348715892412243L;
    
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_SEQUENCE")
//    @Column(name = "ID", nullable = false, unique = true)
    private BigInteger id;
//    @Column(name = "CODE", nullable = false, length = 10)
    private String code;
//    @Column(name = "NAME_RU", nullable = true, length = 4000)
    private String nameRu;
//    @Column(name = "NAME_KZ", nullable = true, length = 4000)
    private String nameKz;
//    @Column(name = "NOTE", nullable = true, length = 4000)
    private String note;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
