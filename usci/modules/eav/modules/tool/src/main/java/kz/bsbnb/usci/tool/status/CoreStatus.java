package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class CoreStatus implements Serializable
{
    private double avgProcessed;

    private double avgInserts;

    private double avgSelects;

    private double avgDeletes;

    private double avgUpdates;

    private long totalProcessed;

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

    public double getAvgDeletes() {
        return avgDeletes;
    }

    public void setAvgDeletes(double avgDeletes) {
        this.avgDeletes = avgDeletes;
    }

    public double getAvgUpdates() {
        return avgUpdates;
    }

    public void setAvgUpdates(double avgUpdates) {
        this.avgUpdates = avgUpdates;
    }

    @Override
    public String toString() {
        return "CoreStatus{" +
                "avgProcessed=" + avgProcessed +
                ", avgInserts=" + avgInserts +
                ", avgSelects=" + avgSelects +
                ", avgDeletes=" + avgDeletes +
                ", avgUpdates=" + avgUpdates +
                ", totalProcessed=" + totalProcessed +
                '}';
    }
}
