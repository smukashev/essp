package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class CoreStatus implements Serializable
{
    double avgProcessed;
    double avgInserts;
    double avgSelects;
    long totalProcessed;

    public double getAvgProcessed()
    {
        return avgProcessed;
    }

    public void setAvgProcessed(double avgProcessed)
    {
        this.avgProcessed = avgProcessed;
    }

    public double getAvgInserts()
    {
        return avgInserts;
    }

    public void setAvgInserts(double avgInserts)
    {
        this.avgInserts = avgInserts;
    }

    public double getAvgSelects()
    {
        return avgSelects;
    }

    public void setAvgSelects(double avgSelects)
    {
        this.avgSelects = avgSelects;
    }

    public long getTotalProcessed()
    {
        return totalProcessed;
    }

    public void setTotalProcessed(long totalProcessed)
    {
        this.totalProcessed = totalProcessed;
    }

    @Override
    public String toString()
    {
        return "CoreStatus{" +
                "avgProcessed=" + avgProcessed +
                ", avgInserts=" + avgInserts +
                ", avgSelects=" + avgSelects +
                ", totalProcessed=" + totalProcessed +
                '}';
    }
}
