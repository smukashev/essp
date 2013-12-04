package kz.bsbnb.usci.eav.model.json;

public class BatchSign
{
    private long batchId;
    private long userId;
    private String sign;
    private String fileName;
    private String type = "sign";
    private String md5;

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public String getSign()
    {
        return sign;
    }

    public void setSign(String sign)
    {
        this.sign = sign;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getType()
    {
        return type;
    }

    public String getMd5()
    {
        return md5;
    }

    public void setMd5(String md5)
    {
        this.md5 = md5;
    }

    public long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }
}
