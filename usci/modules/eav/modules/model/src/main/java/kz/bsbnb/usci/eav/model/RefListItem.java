package kz.bsbnb.usci.eav.model;

import java.io.Serializable;

public class RefListItem implements Serializable
{
    private long id;
    private String title;
    private String code;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}
