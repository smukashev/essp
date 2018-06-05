package kz.bsbnb;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataEntity {
    DataOperationType dataOperation;
    MetaClass meta;
    Map<String, DataValue> values;


    public DataEntity(MetaClass metaCredit) {
        this.meta = metaCredit;
        values = new HashMap<>();
    }

    public void setDataValue(String attribute, DataValue dataDoubleValue) {
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        if(metaAttribute == null)
            throw new RuntimeException("no such attribute: " + attribute);

        values.put(attribute, dataDoubleValue);
    }

    public Object getEl(String path) {
        DataEntity ret = this;
        String[] array = path.split("\\.");
        for (int i = 0; i < array.length; i++) {
            DataValue dataValue = ret.values.get(array[i]);
            if(i < array.length - 1)
                ret = ((DataEntity) dataValue.getValue());
            else
                return dataValue.getValue();
        }

        return ret;
    }
}
