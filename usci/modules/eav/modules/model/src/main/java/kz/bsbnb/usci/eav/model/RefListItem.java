package kz.bsbnb.usci.eav.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class RefListItem implements Serializable
{
    private static final long serialVersionUID = 0L;

    private long id;
    private String title;

    @Deprecated
    private String code;

    @Deprecated
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

    @Deprecated
    public String getCode()
    {
        return code;
    }

    @Deprecated
    public void setCode(String code)
    {
        this.code = code;
    }

    @Deprecated
    public void addValue(String key, Object value) {
        values.put(key, value);
    }

    @Deprecated
    public Object getValue(String key) {
        return values.get(key);
    }

    @Deprecated
    public Set<String> getKeys() {
        return values.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefListItem that = (RefListItem) o;

        if (id != that.id) return false;

        return true;
    }

}
