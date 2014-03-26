package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.service.UserLocalServiceUtil;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "REPORT_LOAD")
@SequenceGenerator(name = "REPORT_LOAD_SEQUENCE", sequenceName = "REPORT_LOAD_SEQ", initialValue = 1000, allocationSize = 1)
@NamedQueries({
    @NamedQuery(name = "ReportLoad.findAll", query = "SELECT r FROM ReportLoad r"),
    @NamedQuery(name = "ReportLoad.findById", query = "SELECT r FROM ReportLoad r WHERE r.id = :id")})
public class ReportLoad implements Serializable {

    public ReportLoad() {
    }
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_LOAD_SEQUENCE")
    @Column(name = "ID")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REPORT_ID")
    private Report report;
    @Basic(optional = false)
    @Column(name = "PORTAL_USER_ID")
    private long portalUserId;
    @Basic(optional = false)
    @Column(name = "START_TIME")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startTime;
    @Basic(optional = false)
    @Column(name = "FINISH_TIME")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date finishTime;
    @Basic(optional = false)
    @Column(name = "NOTE")
    private String note;
    @OneToMany(targetEntity = com.bsbnb.creditregistry.portlets.report.dm.ReportLoadFile.class, cascade = CascadeType.ALL, mappedBy = "reportLoad")
    @OrderBy(value = "id")
    private List<ReportLoadFile> files;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    /**
     * @param report the report to set
     */
    public void setReport(Report report) {
        this.report = report;
    }

    /**
     * @return the portalUserId
     */
    public long getPortalUserId() {
        return portalUserId;
    }

    /**
     * @param portalUserId the portalUserId to set
     */
    public void setPortalUserId(long portalUserId) {
        this.portalUserId = portalUserId;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the finishTime
     */
    public Date getFinishTime() {
        return finishTime;
    }

    /**
     * @param finishTime the finishTime to set
     */
    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        String newValue = note;
        if (newValue == null) {
            newValue = "";
        }
        if (newValue.length() > 200) {
            newValue = newValue.substring(0, 200);
        }
        this.note = newValue;
    }

    /**
     * @return the files
     */
    public List<ReportLoadFile> getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(List<ReportLoadFile> files) {
        for (ReportLoadFile file : files) {
            file.setReportLoad(this);
        }
        this.files = files;
    }

    public String getReportName() {
        return report.getLocalizedName();
    }

    public void addFile(ReportLoadFile file) {
        file.setReportLoad(this);
        this.files.add(file);
    }

    public String getUsername() {
        try {
            return UserLocalServiceUtil.getUser(portalUserId).getFullName();
        } catch (PortalException ex) {
        } catch (SystemException ex) {
        }
        return "";
    }
}
