package kz.bsbnb.usci.porltet.batch_entry.model.json;

import java.util.ArrayList;

public class MetaClassList
{
    private int total;

    private ArrayList<MetaClassListEntry> data;

    public MetaClassList()
    {
        data = new ArrayList<MetaClassListEntry>();
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public ArrayList<MetaClassListEntry> getData()
    {
        return data;
    }

    public void setData(ArrayList<MetaClassListEntry> data)
    {
        this.data = data;
    }
}
