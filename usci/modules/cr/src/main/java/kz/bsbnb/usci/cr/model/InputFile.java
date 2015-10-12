package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;

public class InputFile implements Serializable {
    private static final long serialVersionUID = 2125312865112267120L;

    private long id;
    
    private InputInfo inputInfo;
    
    private String filePath;

    private String md5;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public InputInfo getInputInfo()
    {
        return inputInfo;
    }

    public void setInputInfo(InputInfo inputInfo)
    {
        this.inputInfo = inputInfo;
    }

    public String getMd5()
    {
        return md5;
    }

    public void setMd5(String md5)
    {
        this.md5 = md5;
    }
}
