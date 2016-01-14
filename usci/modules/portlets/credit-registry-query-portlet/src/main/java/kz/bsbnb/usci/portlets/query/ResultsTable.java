package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.formattedtable.FormattedTable;
import com.vaadin.data.Property;
import java.text.SimpleDateFormat;
import java.util.Date;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.VerticalAlignment;
import jxl.write.DateFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WriteException;

public class ResultsTable extends FormattedTable {

    private static final long serialVersionUID = 804740605642799781L;
    private final String dateFormat;
    private final SimpleDateFormat SDF;
    private WritableCellFormat dateCellFormat;

    public ResultsTable(String dateFormat) {
        super();
        this.dateFormat = dateFormat;
        SDF = new SimpleDateFormat(dateFormat);
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {
        Object value = property.getValue();
        if (value == null) {
            return "";
        }
        if (value instanceof Date) {
            Date dateValue = (Date) value;
            String result = SDF.format(dateValue);
            return result;
        }

        if (value instanceof Double) {
            Double d = (Double) value;
            if (d - Math.round(d) < 1e-6) {
                return String.format("%64.0f", d);
            }
        }

        return super.formatPropertyValue(rowId, colId, property);
    }

    @Override
    protected void initFontsAndFormats() throws WriteException {
        super.initFontsAndFormats();
        dateCellFormat = new WritableCellFormat(new DateFormat(dateFormat));
        dateCellFormat.setFont(getMainFont());
        dateCellFormat.setShrinkToFit(true);
        dateCellFormat.setVerticalAlignment(VerticalAlignment.TOP);
        dateCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
    }

    @Override
    protected CellFormat getCellFormat(Object value, Object columnID, String format) {
        if (value instanceof Date) {
            return dateCellFormat;
        }
        return super.getCellFormat(value, columnID, format);
    }

}
