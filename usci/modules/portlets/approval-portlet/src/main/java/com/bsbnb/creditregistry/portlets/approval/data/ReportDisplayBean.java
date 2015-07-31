package com.bsbnb.creditregistry.portlets.approval.data;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.util.ReportStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportDisplayBean {
    
    private Report report;
    private List<ReportDisplayListener> reportDisplayListeners = new ArrayList<ReportDisplayListener>();
    private CreditorNameLink creditorNameLink;
    private CrossCheckLink statusLink;
    private int rownum = 0;

    public ReportDisplayBean(Report report) {
        this.report = report;
        creditorNameLink = new CreditorNameLink(report.getCreditor().getName(), new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                callReportDisplay();
            }
        });
        statusLink = new CrossCheckLink(report.getStatus().getNameRu(),report.getStatus().getId(), report.getCreditor().getId(), report.getReportDate());
    }

    /**
     * @return the rownum
     */
    public int getRownum() {
        return rownum;
    }

    /**
     * @param rownum the rownum to set
     */
    public void setRownum(int rownum) {
        this.rownum = rownum;
    }

    private void callReportDisplay() {
        for (ReportDisplayListener listener : reportDisplayListeners) {
            listener.displayReport(report.getCreditor(), report.getReportDate());
        }
    }

    public void addReportDisplayListener(ReportDisplayListener listener) {
        reportDisplayListeners.add(listener);
    }

    public CreditorNameLink getCreditorNameLink() {
        return creditorNameLink;
    }
    
    public CrossCheckLink getStatusLink() {
        return statusLink;
    }
    
    public Long getActualCount() {
        return report.getActualCount();
    }
    
    public Date getBeginDate() {
        return report.getBeginningDate();
    }
    
    public Date getEndDate() {
        return report.getEndDate();
    }
    
    public String getColor() {
        Shared status = report.getStatus();
        if(ReportStatus.COMPLETED.code().equals(status.getCode())) {
            return "lightblue";
        }
        if(ReportStatus.ORGANIZATION_APPROVED.code().equals(status.getCode())) {
            return "orange";
        }
        if(ReportStatus.WOE.code().equals(status.getCode())) {
            return "lightgreen";
        }
        if(ReportStatus.WE.code().equals(status.getCode())) {
            return "red";
        }
        return "white";
    }
    
    public Report getReport() {
        return report;
    }
    
    public String getCreditorName() {
        return report.getCreditor().getName();
    }
    
    public String getStatusName() {
        return report.getStatus().getNameRu();
    }
    
    private static class CreditorNameLink extends Button implements Comparable<Object> {
        
        public CreditorNameLink(String creditorName, Button.ClickListener listener) {
            super(creditorName, listener);
            setStyleName(BaseTheme.BUTTON_LINK);
            addStyleName("word-wrap-link-button");
            setWidth("200px");
        }   
        
        @Override
        public int compareTo(Object o) {
            CreditorNameLink other = (CreditorNameLink) o;
            if(getCaption()==null) {
                return -1;
            }
            return getCaption().compareTo(other.getCaption());
        }
        
    }

    public interface ReportDisplayListener {

        public void displayReport(Creditor creditor, Date reportDate);
    }
}

