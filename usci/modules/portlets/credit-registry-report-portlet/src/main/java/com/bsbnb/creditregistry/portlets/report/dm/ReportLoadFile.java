package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "REPORT_LOAD_FILE")
@SequenceGenerator(name = "REPORT_LOAD_FILE_SEQUENCE", sequenceName = "REPORT_LOAD_FILE_SEQ", initialValue = 1000, allocationSize = 1)
@NamedQueries({
    @NamedQuery(name = "ReportLoadFile.findAll", query = "SELECT r FROM ReportLoadFile r"),
    @NamedQuery(name = "ReportLoadFile.findById", query = "SELECT r FROM ReportLoadFile r WHERE r.id = :id")})
public class ReportLoadFile implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_LOAD_FILE_SEQUENCE")
    @Column(name = "ID")
    private long id;
    @ManyToOne(fetch= FetchType.LAZY) 
    @JoinColumn(name="REPORT_LOAD_ID")
    private ReportLoad reportLoad;
    @Basic(optional=false) 
    @Column(name="FILENAME")
    private String filename;
    
    @Basic(optional=false) 
    @Column(name="MIMETYPE")
    private String mimeType;
    
    @Basic(optional=false) 
    @Column(name="filepath")
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
