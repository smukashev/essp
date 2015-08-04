package kz.bsbnb.usci.eav.model.meta;

import java.io.Serializable;

public class MetaClassName implements Serializable
{
    private String className;
    private String classTitle;
    private long id;
    private long isDisabled;

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getClassTitle()
    {
        return classTitle;
    }

    public void setClassTitle(String classTitle)
    {
        this.classTitle = classTitle;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(long isDisabled) {
        this.isDisabled = isDisabled;
    }
}
