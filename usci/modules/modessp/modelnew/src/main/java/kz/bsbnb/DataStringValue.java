package kz.bsbnb;

public class DataStringValue extends DataValue<String> {
    public DataStringValue(String value) {
        super(value);
    }

    public DataStringValue(Object value) {
        this.value = ((String) value);
    }
}
