package com.bsbnb.creditregistry.portlets.report.ui;

import com.bsbnb.creditregistry.portlets.report.dm.Report;
import com.vaadin.ui.Button;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportButton extends Button{
    private Report report;

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
    
    public ReportButton(String caption, Report report, Button.ClickListener listener) {
        super(caption, listener);
        this.report = report;
    }
}
