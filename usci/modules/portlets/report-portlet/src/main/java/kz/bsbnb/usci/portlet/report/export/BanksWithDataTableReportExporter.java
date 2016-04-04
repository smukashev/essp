package kz.bsbnb.usci.portlet.report.export;

import kz.bsbnb.usci.portlet.report.ui.ConstantValues;
import kz.bsbnb.usci.portlet.report.ui.CustomDataSource;
import com.vaadin.data.Item;
import com.vaadin.ui.Table;
import org.apache.log4j.Logger;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BanksWithDataTableReportExporter extends TableReportExporter {

    private static final Logger logger = Logger.getLogger(BanksWithDataTableReportExporter.class);

    @Override
    protected Table getTable(CustomDataSource customDataSource) {
        if (getApplication() != null) {
            logger.info("Theme: "+ getApplication().getTheme());
        }
        final Table table = super.getTable(customDataSource);

        table.setStyleName("colored-table");
        table.setCellStyleGenerator(new Table.CellStyleGenerator() {
            public String getStyle(Object itemId, Object propertyId) {
                Item item = table.getItem(itemId);
                String statusIDString = null;

                if(item.getItemProperty("STATUS-ID") != null &&
                        item.getItemProperty("STATUS-ID").getValue() != null)
                    statusIDString = item.getItemProperty("STATUS-ID").getValue().toString();

                int statusID = 0;
                if(statusIDString != null)
                    statusID = (int) Double.parseDouble(statusIDString);

                switch (statusID) {
                    case ConstantValues.REPORT_STATUS_OK:
                        return "lightblue";
                    case ConstantValues.CROSS_CHECK_SUCCESS:
                        return "lightgreen";
                    case ConstantValues.CROSS_CHECK_FAIL:
                        return "red";
                    case ConstantValues.ORGANIZATION_APPROVED:
                        return "orange";
                }

                return "white";
            }
        });
        
        table.setVisibleColumns(new String[]{"NAME", "STATUS-NAME", "ACTUAL-COUNT", "BEGIN-DATE", "END-DATE"});
        table.setColumnWidth("NAME", 200);
        table.setColumnWidth("ACTUAL-COUNT", 140);
        table.setColumnWidth("BEGIN-DATE", 150);
        table.setColumnWidth("END-DATE", 140);
        table.setColumnWidth("STATUS-NAME", 170);

        table.setPageLength(Math.min(10, customDataSource.getItemIds().size()));
        table.setHeight(null);
        table.setWidth("100%");
        return table;
    }
}
