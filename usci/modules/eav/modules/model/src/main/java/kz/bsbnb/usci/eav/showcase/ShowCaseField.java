package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class ShowCaseField extends Persistable {
    private Long attributeId;
    private Long metaId;
    private String columnName;
    private String attributePath;

    private int type = ShowCaseFieldTypes.DEFAULT;

    public Long getMetaId() {
        return metaId;
    }

    public void setMetaId(Long metaId) {
        this.metaId = metaId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getAttributePath() {
        return attributePath;
    }

    public void setAttributePath(String attributePath) {
        this.attributePath = attributePath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ShowCaseField{" +
                "attributePath='" + attributePath + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }

    public final static class ShowCaseFieldTypes {
        public final static int DEFAULT = 1;
        public final static int CUSTOM = 2;
        public final static int KEY = 3;
    }
}
