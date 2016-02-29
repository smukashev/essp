package kz.bsbnb.usci.showcase.element;

public class PathElement {
    public final String elementPath;
    public final String attributePath;
    public final String columnName;

    public PathElement(String elementPath, String attributePath, String columnName) {
        this.elementPath = elementPath;
        this.attributePath = attributePath;
        this.columnName = columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathElement that = (PathElement) o;

        if (!elementPath.equals(that.elementPath)) return false;
        if (!attributePath.equals(that.attributePath)) return false;
        return columnName.equals(that.columnName);

    }

    @Override
    public int hashCode() {
        int result = elementPath.hashCode();
        result = 31 * result + attributePath.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PathElement{" +
                "elementPath='" + elementPath + '\'' +
                ", attributePath='" + attributePath + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
