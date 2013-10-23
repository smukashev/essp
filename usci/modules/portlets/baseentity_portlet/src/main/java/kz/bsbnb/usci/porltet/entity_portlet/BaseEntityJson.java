package kz.bsbnb.usci.porltet.entity_portlet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;

import java.util.Date;
import java.util.HashMap;

/**
 * @author abukabayev
 */
public class BaseEntityJson {

    @Expose
    private Long id;
    @Expose
    private Date reportDate;
    @Expose
    @SerializedName("attributes") private HashMap<String, BaseValueJson> values =
            new HashMap<String, BaseValueJson>();

    private MetaClass meta;

    public BaseEntityJson() {
    }

    public BaseEntityJson(Date reportDate, HashMap<String, BaseValueJson> values) {
        this.reportDate = reportDate;
        this.values = values;
    }

    public BaseEntityJson(MetaClass meta, Date reportDate)
    {
        this.reportDate = reportDate;
        this.meta = meta;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public HashMap<String, BaseValueJson> getValues() {
        return values;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public void setValues(HashMap<String, BaseValueJson> values) {
        this.values = values;
    }
    public void put(MetaClass meta,String name, BaseValueJson value)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if (value == null)
            throw new IllegalArgumentException("Value not be equal to null.");

        if (value.getValue() != null)
        {
            Class<?> valueClass = value.getValue().getClass();
            Class<?> expValueClass;

            if (type.isComplex())
                if(type.isSet())
                {
                    expValueClass = BaseSetJson.class;
                }
                else
                {
                    expValueClass = BaseEntityJson.class;
                }
            else
            {
                if(type.isSet())
                {
                    MetaSet metaValue = (MetaSet)type;

                    if (type.isSet())
                    {
                        expValueClass = BaseSetJson.class;
                        valueClass = value.getValue().getClass();
                    }
                    else
                    {
                        expValueClass = metaValue.getTypeCode().getDataTypeClass();
                        valueClass = ((MetaValue)(((BaseSetJson)value.getValue()).getMemberType())).getTypeCode().
                                getDataTypeClass();
                    }


                }
                else
                {
                    MetaValue metaValue = (MetaValue)type;
                    expValueClass = metaValue.getTypeCode().getDataTypeClass();
                }

            }

            /*if(expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
                throw new IllegalArgumentException("Type mismatch in class: " +
                        meta.getClassName() + ". Needed " + expValueClass + ", got: " +
                        valueClass);*/
        }

        values.put(name, value);
    }

}
