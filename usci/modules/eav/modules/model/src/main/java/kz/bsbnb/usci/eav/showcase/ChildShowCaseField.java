package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class ChildShowCaseField extends Persistable {
    private Long attributeId;

    private String columnName;

    private String attributePath;

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
        return "ChildShowCaseField{" +
                "attributeId=" + attributeId +
                ", columnName='" + columnName + '\'' +
                ", attributePath='" + attributePath + '\'' +
                ", type=" + type +
                '}';
    }

    private int type =ChildShowCaseFieldTypes.DEFAULT;

    public final static class ChildShowCaseFieldTypes {
        public final static int DEFAULT = 1;
        public final static int KEY = 2;
    }
}
