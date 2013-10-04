package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Сущность представляет собой описание системной ошибки имеющий идентификатор
 * (id), код системной ошибки (code), тект на русском языке (nameRu), текст на
 * казахском языке (nameKz) и тип ошибки (errorType).
 *
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a>
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -8626348715892412243L;
    
    private long id;
    private String code;
    private String nameRu;
    private String nameKz;
    private String note;

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
