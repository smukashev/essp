package kz.bsbnb.usci.eav.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class RefListItem implements Serializable
{
    private long id;
    private String title;
    private String code;

    private HashMap<String, Object> values = new HashMap<String, Object>();

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

    public void addValue(String key, Object value) {
        values.put(key, value);
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Set<String> getKeys() {
        return values.keySet();
    }
}