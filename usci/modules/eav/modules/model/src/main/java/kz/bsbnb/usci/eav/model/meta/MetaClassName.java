package kz.bsbnb.usci.eav.model.meta;

import java.io.Serializable;

public class MetaClassName implements Serializable
{
    private String className;
    private String classTitle;

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
}
