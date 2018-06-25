package kz.bsbnb;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.*;

public class DataEntity {
    DataOperationType dataOperation;
    MetaClass metaClass;
    Map<String, DataValue> values;


    public DataEntity(MetaClass metaCredit) {
        this.metaClass = metaCredit;
        values = new HashMap<>();
    }

    public void setDataValue(String attribute, DataValue dataDoubleValue) {
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
        if(metaAttribute == null)
            throw new RuntimeException("no such attribute: " + attribute);

        values.put(attribute, dataDoubleValue);
    }

    public Object getEl(String path) {
        DataEntity ret = this;
        String[] array = path.split("\\.");
        for (int i = 0; i < array.length; i++) {
            DataValue dataValue = ret.values.get(array[i]);
            if (dataValue == null) {
                return null;
            } else if(i < array.length - 1)
                ret = ((DataEntity) dataValue.getValue());
            else
                return dataValue.getValue();
        }

        return ret;
    }

    public List<DataEntity> getEls(String path){
        List<DataEntity> ret = new LinkedList<>();
        ret.add(this);
        String[] array = path.split("\\.");
        for(String attribute: array) {
            List<DataEntity> nextArray = new LinkedList<>();
            for (DataEntity entity : ret) {
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
                IMetaType metaType = metaAttribute.getMetaType();
                if(metaType.isComplex()) {
                    DataValue dataValue = entity.values.get(attribute);
                    if(metaType.isSet()) {
                        DataSet set = (DataSet) dataValue.getValue();
                        for (DataEntity value : set.values) {
                            nextArray.add(value);
                        }
                    } else {
                        DataEntity childEntity = ((DataEntity) dataValue.getValue());
                        nextArray.add(childEntity);
                    }
                }
            }
            ret = nextArray;
        }

        return ret;
    }

    public MetaClass getMeta() {
        return metaClass;
    }
}
