package com.bsbnb.creditregistry.portlets.report.ui;

import com.bsbnb.creditregistry.portlets.report.Localization;
import com.bsbnb.creditregistry.portlets.report.dm.DatabaseConnect;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MainLayout extends VerticalLayout{
    
    private DatabaseConnect connect;
    
    public MainLayout(DatabaseConnect connect) {
        this.connect = connect;
    } 
    
    @Override
    public void attach() {
        TabSheet mainTabSheet = new TabSheet();
        Tab reportListTab = mainTabSheet.addTab(new ReportListLayout(connect));
        reportListTab.setCaption(Localization.REPORT_LIST_TAB_PAGE_CAPTION.getValue());
        Tab fileDownloadsTab = mainTabSheet.addTab(new GeneratedFilesLayout(connect));
        fileDownloadsTab.setCaption(Localization.GENERATED_REPORTS_TAB_PAGE_CAPTION.getValue());
        mainTabSheet.setWidth("100%");
        addComponent(mainTabSheet);
    }
    
}
