package com.bsbnb.creditregistry.portlets.report.ui;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ConstantValues {
    private ConstantValues() {
        
    }
    //FEATURE: Получать расположение каталога отчетов из Sysconfig
    public static final String REPORT_FILES_CATALOG = "C:\\Portal_afn\\Report\\";
    //FEATURE: Получать id статусов из Sysconfig
    public static final int REPORT_STATUS_OK = 92;
    public static final int REPORT_STATUS_IN_PROGRESS = 90;
    public static final int CROSS_CHECK_FAIL = 76;
    public static final int CROSS_CHECK_SUCCESS = 77;
    public static final int ORGANIZATION_APPROVED = 128;
}
