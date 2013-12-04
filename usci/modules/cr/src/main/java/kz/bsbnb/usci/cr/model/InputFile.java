package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class InputFile implements Serializable {
    private long id;
    
    private InputInfo inputInfo;
    
    private String filePath;

    private String md5;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
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
