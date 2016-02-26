package kz.bsbnb.usci.showcase.element;

public class ValueElement {
    public final String columnName;
    public final Long elementId;
    public final boolean isArray;
    public final boolean isSimple;

    public ValueElement(String columnName, Long elementId) {
        this.columnName = columnName;
        this.elementId = elementId;
        this.isArray = false;
        this.isSimple = false;
    }

    public ValueElement(String columnName, Long elementId, boolean isArray) {
        this.columnName = columnName;
        this.elementId = elementId;
        this.isArray = isArray;
        this.isSimple = false;
    }

    public ValueElement(String columnName, Long elementId, boolean isArray, boolean isSimple) {
        this.columnName = columnName;
        this.elementId = elementId;
        this.isArray = isArray;
        this.isSimple = isSimple;
    }

    @Override
    public String toString() {
        return "ValueElement{" +
                "columnName='" + columnName + '\'' +
                ", elementId=" + elementId +
                ", isArray=" + isArray +
                ", isSimple=" + isSimple +
                '}';
    }

    @SuppressWarnings("all")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueElement that = (ValueElement) o;

        if (isArray != that.isArray) return false;
        if (isSimple != that.isSimple) return false;
        if (!columnName.equals(that.columnName)) return false;
        return elementId.equals(that.elementId);

    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = 31 * result + elementId.hashCode();
        result = 31 * result + (isArray ? 1 : 0);
        result = 31 * result + (isSimple ? 1 : 0);
        return result;
    }
}