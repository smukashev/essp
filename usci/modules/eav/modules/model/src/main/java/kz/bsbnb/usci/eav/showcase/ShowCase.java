package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a.tkachenko on 4/8/14.
 */
public class ShowCase extends Persistable
{
    private String name;
    private String tableName;
    private String title;
    private MetaClass meta;

    private ArrayList<ShowCaseField> fields = new ArrayList<ShowCaseField>();

    public ShowCase()
    {
    }

    public ShowCase(long id, String name, String tableName, String title)
    {
        this.id = id;
        this.name = name;
        this.tableName = tableName;
        this.title = title;
    }

    public MetaClass getMeta()
    {
        return meta;
    }

    public void setMeta(MetaClass meta)
    {
        this.meta = meta;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void addField(ShowCaseField field) {
        fields.add(field);
    }

    public void addField(MetaClass meta, String path, String name) {
        addField(meta, path, name, name);
    }

    public void addField(MetaClass meta, String path, String name, String columnName) {
        IMetaAttribute attr = meta.getElAttribute(path + "." + name);

        if (attr == null) {
            throw new IllegalArgumentException("Can't get attribute: " + path + "." + name);
        }

        ShowCaseField showCaseField = new ShowCaseField();
        showCaseField.setAttributeId(attr.getId());
        showCaseField.setName(columnName);
        showCaseField.setAttributePath(path);
        showCaseField.setAttributeName(name);
        showCaseField.setColumnName(columnName);
        showCaseField.setTitle(attr.getTitle());

        addField(showCaseField);
    }

    public List<ShowCaseField> getFieldsList() {
        return fields;
    }
}
