package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

/**
 * Created by a.tkachenko on 4/8/14.
 */
public class ShowCaseField extends Persistable
{
    private String name;
    private String columnName;
    private String title;
    private long attributeId;
    private String attributeName;
    private String attributePath;

    public ShowCaseField()
    {
    }

    public ShowCaseField(long id, String name, String columnName, String title, long attributeId, String attributeName, String attributePath)
    {
        this.id = id;
        this.name = name;
        this.columnName = columnName;
        this.title = title;
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.attributePath = attributePath;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public long getAttributeId()
    {
        return attributeId;
    }

    public void setAttributeId(long attributeId)
    {
        this.attributeId = attributeId;
    }

    public String getAttributeName()
    {
        return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    public String getAttributePath()
    {
        return attributePath;
    }

    public void setAttributePath(String attributePath)
    {
        this.attributePath = attributePath;
    }
}
