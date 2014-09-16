package kz.bsbnb.usci.eav.showcase;

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
        if(attributeName == null)
            this.attributeName = "";
        else
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

    public String getPath() {
        return attributePath.equals("") ? attributeName : attributePath + "." + attributeName;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ShowCaseField that = (ShowCaseField) o;

        if (attributeId != that.attributeId) return false;
        if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null)
            return false;
        if (attributePath != null ? !attributePath.equals(that.attributePath) : that.attributePath != null)
            return false;
        if (columnName != null ? !columnName.equals(that.columnName) : that.columnName != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (int) (attributeId ^ (attributeId >>> 32));
        result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
        result = 31 * result + (attributePath != null ? attributePath.hashCode() : 0);
        return result;
    }
}
