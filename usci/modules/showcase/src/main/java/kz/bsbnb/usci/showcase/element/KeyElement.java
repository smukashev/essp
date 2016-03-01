package kz.bsbnb.usci.showcase.element;

import kz.bsbnb.usci.eav.showcase.ShowCaseField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyElement {
    public final String[] keys;
    public final Object[] values;
    public String queryKeys = "";

    public KeyElement(HashMap<ValueElement, Object> map, List<ShowCaseField> keyFields) {
        keys = new String[keyFields.size()];
        values = new Object[keyFields.size()];

        int i = 0;
        for (ShowCaseField sf : keyFields) {
            keys[i] = sf.getColumnName();

            for (Map.Entry<ValueElement, Object> entry : map.entrySet()) {
                if (entry.getKey().columnName.equals(sf.getColumnName())) {
                    values[i] = entry.getValue();
                    break;
                }
            }

            queryKeys += sf.getColumnName() + " = ? ";
            if (++i < keyFields.size()) queryKeys += " AND ";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyElement that = (KeyElement) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(keys, that.keys)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(values, that.values)) return false;
        return queryKeys.equals(that.queryKeys);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(keys);
        result = 31 * result + Arrays.hashCode(values);
        result = 31 * result + queryKeys.hashCode();
        return result;
    }
}
