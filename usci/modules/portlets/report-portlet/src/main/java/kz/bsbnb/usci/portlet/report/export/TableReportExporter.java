package kz.bsbnb.usci.portlet.report.export;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.ReportPortletResource;
import kz.bsbnb.usci.portlet.report.dm.Report;
import kz.bsbnb.usci.portlet.report.ui.ConstantValues;
import kz.bsbnb.usci.portlet.report.ui.CustomDataSource;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.usci.portlet.report.ReportApplication.getApplicationLocale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TableReportExporter extends AbstractReportExporter {

    private static final String LOCALIZATION_CONTEXT = "TABLE-REPORT-COMPONENT";
    private SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private DecimalFormat defaultNumberFormat = new DecimalFormat("# ###");
    private final Logger logger = Logger.getLogger(TableReportExporter.class);

    protected Table getTable(CustomDataSource customDataSource) {
        defaultNumberFormat.setMaximumFractionDigits(3);
        defaultNumberFormat.setDecimalSeparatorAlwaysShown(false);
        defaultNumberFormat.setGroupingSize(3);
        defaultNumberFormat.setGroupingUsed(true);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(' ');
        dfs.setDecimalSeparator('.');
        defaultNumberFormat.setDecimalFormatSymbols(dfs);
        Table table = new Table("", customDataSource) {

            @Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
                String baseValue = super.formatPropertyValue(rowId, colId, property);
                if (property == null || property.getType() == null || property.getValue() == null) {
                    return baseValue;
                }
                if (property.getValue() instanceof Date) {
                    Date dateValue = (Date) property.getValue();
                    return defaultDateFormat.format(dateValue);
                }
                if (property.getValue() instanceof Double) {
                    Double doubleValue = (Double) property.getValue();
                    return defaultNumberFormat.format(doubleValue);
                }
                return baseValue;
            }

            @Override
            public String getColumnHeader(final Object propertyId) {
                final String originalHeader = super.getColumnHeader(propertyId);
                if (originalHeader != null) {
                    final String layoutedHeader = originalHeader.replaceAll("\n", "<br />");
                    return layoutedHeader;
                }
                return originalHeader;
            }
        };
        table.setWidth("100%");
        localizeTable(table);
        return table;
    }

    private void localizeTable(Table table) {
        try {
            Report report = getTargetReportComponent().getReport();
            final String reportName = report.getName();
            final String reportPath = StaticRouter.getReportFilesCatalog() + reportName + "\\";
            final String resourceFilePath = reportPath + reportName + "_" +
                    getApplicationLocale().getLanguage() + ".properties";

            logger.info("Resources file: "+ resourceFilePath);
            PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new FileInputStream(resourceFilePath));
            Object[] columnNames = table.getVisibleColumns();
            int columnCount = columnNames.length;
            String[] columnHeaders = new String[columnCount];
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                try {
                    columnHeaders[columnIndex] = resourceBundle.getString(columnNames[columnIndex].toString());
                } catch (MissingResourceException mre) {
                    columnHeaders[columnIndex] = columnNames[columnIndex].toString();
                    logger.warn("Resource not found: "+ mre.getMessage());
                }
                logger.info("Column localization: "+columnNames[columnIndex]+"="+columnHeaders[columnIndex]);
            }
            table.setColumnHeaders(columnHeaders);
        } catch (IOException ioe) {
            logger.error("Failed to access resource file: "+ ioe.getMessage(),ioe);
        }
    }

    private String getLocalizedString(String key) {
        return ReportApplication.getResourceString(LOCALIZATION_CONTEXT + "." + key);
    }

    @Override
    public List<Component> getActionComponents() {
        Button loadTableButton = new Button(getLocalizedString("LOAD-TABLE-BUTTON-CAPTION"), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                loadTable();
            }
        });
        loadTableButton.setIcon(ReportPortletResource.TABLE_ICON);
        return Arrays.asList(new Component[]{loadTableButton});
    }

    protected void loadTable() {
        loadStarted();
        getTargetReportComponent().clearOutputComponents();
        try {
            CustomDataSource customDataSource = getTargetReportComponent().loadData();
            if (customDataSource == null) {
                return;
            }
            Table table = getTable(customDataSource);

            getTargetReportComponent().addOutputComponent(table);
            //getTargetReportComponent().getConnect().getUserId()
            //getTargetReportComponent().getReport().getNameRu()
           // ResultSet rs =  getTargetReportComponent().getConnect().runQuery("select report_mail(2319, 'test', sysdate, 'Тест', sysdate) from dual");
        loadFinished();
        } catch (SQLException sqle) {
            logger.error("Sql exception", sqle);
            getTargetReportComponent().addOutputComponent(new Label("Sql exception: " + sqle.getMessage()));
        }
    }

    /**
     * @return the defaultDateFormat
     */
    public String getDefaultDateFormat() {
        return defaultDateFormat.toPattern();
    }

    /**
     * @param defaultDateFormat the defaultDateFormat to set
     */
    public void setDefaultDateFormat(String defaultDateFormat) {
        this.defaultDateFormat = new SimpleDateFormat(defaultDateFormat);
    }
}
