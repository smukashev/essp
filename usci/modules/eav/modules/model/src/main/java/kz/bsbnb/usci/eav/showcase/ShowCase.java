package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;
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

    public List<ShowCaseField> getFieldsList() {
        return fields;
    }
}
