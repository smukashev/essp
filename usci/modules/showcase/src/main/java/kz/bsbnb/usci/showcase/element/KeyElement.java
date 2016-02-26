package kz.bsbnb.usci.showcase.element;

import kz.bsbnb.usci.eav.showcase.ShowCaseField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyElement {
    public final Object[] keys;
    public final Object[] values;
    public String queryKeys = "";

    public KeyElement(HashMap<ValueElement, Object> map, List<ShowCaseField> keyFields) {
        keys = new Object[keyFields.size()];
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
}
