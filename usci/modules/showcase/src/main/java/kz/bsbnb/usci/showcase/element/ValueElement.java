package kz.bsbnb.usci.showcase.element;

public class ValueElement {
    public final String columnName;
    public final Long elementId;
    public final boolean isArray;
    public final boolean isSimple;
    public final int index;

    public ValueElement(String columnName, Long elementId, int index) {
        this.columnName = columnName;
        this.elementId = elementId;
        this.isArray = false;
        this.isSimple = false;
        this.index = index;
    }

    public ValueElement(String columnName, Long elementId, boolean isArray, int index) {
        this.columnName = columnName;
        this.elementId = elementId;
        this.isArray = isArray;
        this.isSimple = false;
        this.index = index;
    }

    public ValueElement(String columnName, Long elementId, boolean isArray, boolean isSimple, int index) {
        this.columnName = columnName;
        this.elementId = elementId;
        this.isArray = isArray;
        this.isSimple = isSimple;
        this.index = index;
    }

    @Override
    public String toString() {
        return "ValueElement{" +
                "columnName='" + columnName + '\'' +
                ", elementId=" + elementId +
                ", isArray=" + isArray +
                ", isSimple=" + isSimple +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueElement that = (ValueElement) o;

        if (isArray != that.isArray) return false;
        if (isSimple != that.isSimple) return false;
        if (index != that.index) return false;
        if (!columnName.equals(that.columnName)) return false;
        return elementId.equals(that.elementId);

    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = 31 * result + elementId.hashCode();
        result = 31 * result + (isArray ? 1 : 0);
        result = 31 * result + (isSimple ? 1 : 0);
        result = 31 * result + index;
        return result;
    }
}