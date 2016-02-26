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
    public String toString() {
        return "PathElement{" +
                "elementPath='" + elementPath + '\'' +
                ", attributePath='" + attributePath + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
