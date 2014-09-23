package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
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
    private String downPath =  "";

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

    public String getDownPath() {
        return downPath;
    }

    public void setDownPath(String downPath) {
        if(downPath == null)
            this.downPath = "";
        else
            this.downPath = downPath;
    }

    public void addField(ShowCaseField field) {
        fields.add(field);
    }

    public MetaClass getActualMeta(){
        if(downPath.equals(""))
            return meta;
        if(meta.getElAttribute(downPath).getMetaType().isSet())
            return (MetaClass) ((MetaSet) meta.getEl(downPath) ).getMemberType();
        return (MetaClass) meta.getEl(downPath);
    }

    public void addField(String path, String name) {
        if(meta==null)
            throw new IllegalArgumentException("meta not set for showcase");

        addField(path, name, name);
    }

    public void addField(String path, String name, String columnName) {

        if(meta == null)
            throw new IllegalArgumentException("meta not set for showcase");

        IMetaAttribute attr = getActualMeta().getElAttribute(path + "." + name);

        if (attr == null) {
            throw new IllegalArgumentException(getName() + ": Can't get attribute: " + path + "." + name);
        }

        IMetaType metaType = attr.getMetaType();

        ShowCaseField showCaseField = new ShowCaseField();
        showCaseField.setAttributeId(attr.getId());
        showCaseField.setName(columnName);
        showCaseField.setAttributePath(path);
        showCaseField.setAttributeName(name);
        showCaseField.setColumnName(columnName);
        showCaseField.setTitle(attr.getTitle());

        if(metaType.isComplex()){
            showCaseField.setAttributePath(path.equals("") ? name : path + "." + name );
            showCaseField.setAttributeName("");
        }

        addField(showCaseField);
    }

    public List<ShowCaseField> getFieldsList() {
        return fields;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Name: " + getName() + "\n" );
        ret.append("Table name: " + getTableName() +"\n");
        ret.append("Meta name: " + ( getMeta() == null ? null : getMeta().getClassName()) + "\n");
        ret.append("Down path: " + downPath + "\n");
        ret.append(String.format("fields(%d):\n",getFieldsList().size()));
        for(ShowCaseField showCaseField : getFieldsList()){
            ret.append(" " + showCaseField.getPath() + ":" + showCaseField.getColumnName() + "\n");
        }
        return ret.toString();
    }
}
