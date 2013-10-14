package com.bsbnb.creditregistry.portlets.report.dm;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import java.io.Serializable;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportLoadFile implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private ReportLoad reportLoad;
    private String filename;
    
    private String mimeType;
    
    private String path;

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
     * @return the reportLoad
     */
    public ReportLoad getReportLoad() {
        return reportLoad;
    }

    /**
     * @param reportLoad the reportLoad to set
     */
    public void setReportLoad(ReportLoad reportLoad) {
        this.reportLoad = reportLoad;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
}
