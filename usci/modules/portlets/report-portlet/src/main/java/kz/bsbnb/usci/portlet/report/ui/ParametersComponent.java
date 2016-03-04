package kz.bsbnb.usci.portlet.report.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlet.report.dm.DatabaseConnect;
import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.dm.Report;
import kz.bsbnb.usci.portlet.report.dm.ReportInputParameter;
import kz.bsbnb.usci.portlet.report.dm.ValuePair;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ParametersComponent extends VerticalLayout {
    private DatabaseConnect connect;

    private static final String LOCALIZATION_PREFIX = "PARAMETERS-COMPONENT";

    private static String getResourceString(String key) {
        return ReportApplication.getResourceString(LOCALIZATION_PREFIX + "." + key);
    }
    private ReportInputParameter[] parameters;
    private Component[] parameterComponents;

    public ParametersComponent(Report report, DatabaseConnect connect) {
        this.connect = connect;
        
        List<ReportInputParameter> reportParameters = report.getInputParameters();
        parameters = reportParameters.toArray(new ReportInputParameter[0]);
        Arrays.sort(parameters, new Comparator<ReportInputParameter>() {

            public int compare(ReportInputParameter o1, ReportInputParameter o2) {
                Integer a = o1.getOrderNumber();
                Integer b = o2.getOrderNumber();
                if (a == null || b == null) {
                    return 1;
                }
                return a.compareTo(b);
            }
        });
        parameterComponents = new Component[parameters.length];
    }

    @Override
    public void attach() {
        setSpacing(true);
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            ReportInputParameter parameter = parameters[parameterIndex];
            String localizedParameterName = parameter.getLocalizedName();
            Component parameterComponent = null;
            switch (parameter.getParameterType()) {
                case DATE:
                    DateField dateField = new DateField();
                    dateField.setDateFormat("dd.MM.yyyy");
                    dateField.setCaption(localizedParameterName);
                    dateField.setValue(new Date());
                    parameterComponent = dateField;
                    break;
                case TIME: 
                    DateField timeField = new DateField();
                    timeField.setDateFormat("dd.MM.yyyy HH:mm:ss");
                    timeField.setCaption(localizedParameterName);
                    timeField.setValue(new Date());
                    parameterComponent = timeField;
                    break;
                case LIST:
                    List<ValuePair> values = connect.getValueListFromStoredProcedure(parameter.getProcedureName());
                    if (values.size() == 1) {
                        final ValuePair value = values.get(0);
                        value.setDisplayName("<h1>" + value.getDisplayName() + "</h1>");
                        Property property = new AbstractProperty() {

                            public Object getValue() {
                                return value;
                            }

                            public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
                                throw new UnsupportedOperationException(Errors.getMessage(Errors.E206));
                            }

                            public Class<?> getType() {
                                return ValuePair.class;
                            }
                        };
                        Label label = new Label(property, Label.CONTENT_XHTML);
                        parameterComponent = label;
                    } else {
                        ComboBox comboBox = new ComboBox();
                        for (ValuePair value : values) {
                            comboBox.addItem(value);
                        }
                        comboBox.setNullSelectionAllowed(false);
                        comboBox.setNewItemsAllowed(false);
                        comboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
                        comboBox.setWidth("200px");
                        comboBox.setCaption(localizedParameterName);
                        parameterComponent = comboBox;
                    }
                    break;
                default:
                    TextField textField = new TextField();
                    textField.setWidth("200px");
                    textField.setCaption(localizedParameterName);
                    parameterComponent = textField;
            }
            parameterComponents[parameterIndex] = parameterComponent;
            addComponent(parameterComponent);
        }
        for(int i=0; i<parameterComponents.length; i++) {
            if(parameterComponents[i] instanceof Focusable) {
                Focusable focusableComponent = (Focusable) parameterComponents[i];
                focusableComponent.focus();
                break;
            }
        }
        setWidth("100%");
    }
    
    public List<String> getParameterLocalizedNames() {
        List<String> result = new ArrayList<String>();
        for(ReportInputParameter parameter : parameters) {
            result.add(parameter.getLocalizedName());
        }

        return result;
    }
    
    public List<String> getParameterNames() {
        List<String> result = new ArrayList<String>();
        for(ReportInputParameter parameter : parameters) {
            result.add(parameter.getParameterName());
        }
        return result;
    }

    public List<String> getParameterCaptions() {
        List<String> result = new ArrayList<String>();
        for (Component parameterComponent : parameterComponents) {
            if (parameterComponent instanceof DateField) {
                DateField dateField = (DateField) parameterComponent;
                Date value = (Date) dateField.getValue();
                if (value == null) {
                    result.add(null);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateField.getDateFormat());
                    result.add(sdf.format(value));
                }
            } else if (parameterComponent instanceof ComboBox) {
                ComboBox comboBox = (ComboBox) parameterComponent;
                result.add(comboBox.getItemCaption(comboBox.getValue()));
            } else if (parameterComponent instanceof TextField) {
                TextField textField = (TextField) parameterComponent;
                result.add((String) textField.getValue());
            } else if(parameterComponent instanceof Label) {
                Label label = (Label) parameterComponent;
                String caption = ((ValuePair) label.getValue()).getDisplayName();
                result.add(caption.replaceAll("<h1>", "").replace("</h1>", ""));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    public List<Object> getParameterValues() {
        List<Object> result = new ArrayList<Object>();
        List<String> notFilledComponentsCaptions = new ArrayList<String>();
        for (int parameterIndex = 0; parameterIndex < parameterComponents.length; parameterIndex++) {
            Component parameterComponent = parameterComponents[parameterIndex];
            if (parameterComponent instanceof Button) {
                continue;
            }
            Object item = null;
            try {
                if (parameterComponent instanceof DateField) {
                    item = getValue((DateField) parameterComponent);
                } else if (parameterComponent instanceof ComboBox) {
                    item = getValue((ComboBox) parameterComponent);
                } else if (parameterComponent instanceof TextField) {
                    item = getValue((TextField) parameterComponent);
                } else if (parameterComponent instanceof Label) {
                    item = getValue((Label) parameterComponent);
                }
                result.add(item);
            } catch (ParameterException pe) {
                notFilledComponentsCaptions.add(pe.getMessage());
            }
        }
        if (!notFilledComponentsCaptions.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(getResourceString("EMPTY-PARAMETER-MESSAGE"));
            for (String componentCaption : notFilledComponentsCaptions) {
                errorMessage.append("<br>");
                errorMessage.append(componentCaption);
            }
            MessageBox.Show(errorMessage.toString(), getResourceString("EMPTY-PARAMETER-CAPTION"), getWindow());
            return null;
        }
        return result;
    }

    private Object getValue(DateField dateField) throws ParameterException {
        Date value = (Date) dateField.getValue();
        if (value == null) {
            dateField.setComponentError(new UserError(getResourceString("ERROR.FILL-DATE-COMPONENT")));
            throw new ParameterException(dateField.getCaption());
        }
        dateField.setComponentError(null);
        return value;
    }

    private Object getValue(Label label) {
        return ((ValuePair) label.getValue()).getValue();
    }

    private Object getValue(ComboBox comboBox) throws ParameterException {
        ValuePair value = (ValuePair) comboBox.getValue();
        if (value == null) {
            comboBox.setComponentError(new UserError(getResourceString("ERROR.SELECT-COMBOBOX-COMPONENT-VALUE")));
            throw new ParameterException(comboBox.getCaption());
        }
        comboBox.setComponentError(null);
        return value.getValue();
    }

    private Object getValue(TextField textField) throws ParameterException {
        String value = (String) textField.getValue();
        if (value == null) {
            textField.setComponentError(new UserError(getResourceString("ERROR.FILL-TEXTFIELD-VALUE")));
            throw new ParameterException(textField.getCaption());
        }
        textField.setComponentError(null);
        return value;
    }
}
