package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.service.UserLocalServiceUtil;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportLoad implements Serializable {

    public ReportLoad() {
    }
    private static final long serialVersionUID = 1L;
    private Long id;
    private Report report;
    private long portalUserId;
    private Date startTime;
    private Date finishTime;
    private String note;
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
