package kz.bsbnb.usci.showcase.element;

public class ArrayElement {
    public final int index;
    public final ValueElement valueElement;

    public ArrayElement(int index, ValueElement valueElement) {
        this.index = index;
        this.valueElement = valueElement;
    }

    @Override
    public String toString() {
        return "ArrayElement{" +
                "index=" + index +
                ", valueElement=" + valueElement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayElement that = (ArrayElement) o;

        return index == that.index && valueElement.equals(that.valueElement);

    }

    @Override
    public int hashCode() {
        int result = valueElement.hashCode();
        result = 31 * result + index;
        return result;
    }
}