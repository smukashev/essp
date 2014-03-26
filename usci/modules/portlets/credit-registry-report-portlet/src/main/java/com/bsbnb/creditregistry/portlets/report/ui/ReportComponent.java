package com.bsbnb.creditregistry.portlets.report.ui;

import com.bsbnb.creditregistry.portlets.report.export.AbstractReportExporter;
import com.bsbnb.creditregistry.portlets.report.dm.DatabaseConnect;
import com.bsbnb.creditregistry.portlets.report.dm.Report;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportComponent extends VerticalLayout {

    private Report report;
    private DatabaseConnect connect;
    public Report getReport() {
        return report;
    }
    
    private ParametersComponent parametersComponent;
    private HorizontalLayout actionsLayout;
    private VerticalLayout outputLayout;
    
    public ReportComponent(Report report, DatabaseConnect connect) {
        this.connect = connect;
        this.report = report;
        parametersComponent = new ParametersComponent(report, connect);
        actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);
        actionsLayout.setWidth("100%");
        outputLayout = new VerticalLayout();
        outputLayout.setWidth("100%");
        setSpacing(true);
        addComponent(parametersComponent);
        addComponent(actionsLayout);
        addComponent(outputLayout);
    }
    
    public void addReportExporter(AbstractReportExporter actionComponent) {
        actionComponent.setTargetReportComponent(this);
        actionsLayout.addComponent(actionComponent);
    }
    
    public void clearOutputComponents() {
        outputLayout.removeAllComponents();
    }
    
    public void addOutputComponent(Component outputComponent) {
        outputLayout.addComponent(outputComponent);
    }
    
    public CustomDataSource loadData() throws SQLException {
        List<Object> parameterValues = getParameterValues();
        if(parameterValues==null) {
            return null;
        }
        return getConnect().getDataSourceFromStoredProcedure(report.getProcedureName(), parameterValues);
    }
    
    public ResultSet getResultSet(int firstRowNum, int lastRowNum) throws SQLException {
        List<Object> parameterValues = getParameterValues();
        if(parameterValues==null) {
            return null;
        }
        parameterValues.add(0, lastRowNum);
        parameterValues.add(0, firstRowNum);
        return getConnect().getResultSetFromStoredProcedure(report.getProcedureName(), parameterValues);
    }
    
    public List<Object> getParameterValues() {
        return parametersComponent.getParameterValues();
    }
    
    public List<String> getParameterCaptions() {
        return parametersComponent.getParameterCaptions();
    }
    
    public List<String> getParameterNames() {
        return parametersComponent.getParameterNames();
    }
    
    public List<String> getParameterLocalizedNames() {
        return parametersComponent.getParameterLocalizedNames();
    }

    /**
     * @return the connect
     */
    public DatabaseConnect getConnect() {
        return connect;
    }
}
