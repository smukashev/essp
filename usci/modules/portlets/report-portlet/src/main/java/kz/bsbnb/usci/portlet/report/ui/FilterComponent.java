package kz.bsbnb.usci.portlet.report.ui;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.client.ui.AlignmentInfo;
import com.vaadin.ui.*;
import kz.bsbnb.usci.portlet.report.dm.ValuePair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Bauyrzhan.Ibraimov on 12.05.2017.
 */
public class FilterComponent extends VerticalLayout  {
    List<ValuePair> values;

    public void createNewFilter() {
    final HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.setSpacing(true);
    addComponent(horizontalLayout);
    ComboBox attributes = new ComboBox();
    attributes.setCaption("Показатель");
    attributes.setDescription("attributes");
    attributes.setImmediate(true);
    attributes.setNewItemsAllowed(false);
    attributes.setNullSelectionAllowed(false);
    attributes.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
    attributes.removeAllItems();
    for (ValuePair value : values) {
        if(!value.toString().equals("OPEN_DATE") && !value.toString().equals("CLOSE_DATE") && !value.toString().equals("REP_DATE"))
        attributes.addItem(value);
    }
    final ComboBox filters = new ComboBox();
    filters.setCaption("Фильтр");
    filters.setDescription("filter");
    filters.setNullSelectionAllowed(false);
    final Button deleteButton = new Button("Удалить фильтр");
    final Component component = new TextField();
    component.setCaption("Значение");
    attributes.addListener(new Property.ValueChangeListener() {
        @Override
        public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
            Component valueComponent;
            String type = ((ValuePair) valueChangeEvent.getProperty().getValue()).getValue();
            if(type.equals("DATE")) {
                valueComponent = new DateField();
                ((DateField)valueComponent).setDateFormat("dd.MM.yyyy");
            } else if(type.equals("BOOLEAN")) {
                valueComponent = new ComboBox();
                ((ComboBox)valueComponent).addItem(booleanValues.FALSE);
                ((ComboBox)valueComponent).addItem(booleanValues.TRUE);
            } else valueComponent = new TextField();
            setFilters(type, filters);
            valueComponent.setCaption("Значение");
            horizontalLayout.replaceComponent(horizontalLayout.getComponent(2), valueComponent);
            horizontalLayout.setComponentAlignment(valueComponent, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM | AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
            horizontalLayout.removeComponent(deleteButton);
            horizontalLayout.addComponent(deleteButton);
            horizontalLayout.setComponentAlignment(deleteButton, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM | AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
        }
    });
    horizontalLayout.addComponent(attributes);
    horizontalLayout.setComponentAlignment(attributes, new Alignment(AlignmentInfo.Bits.ALIGNMENT_VERTICAL_CENTER | AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
    horizontalLayout.addComponent(filters);
    horizontalLayout.setComponentAlignment(filters, new Alignment(AlignmentInfo.Bits.ALIGNMENT_VERTICAL_CENTER | AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
    horizontalLayout.addComponent(component);
    horizontalLayout.setComponentAlignment(component, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM | AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
    horizontalLayout.addComponent(deleteButton);
    horizontalLayout.setComponentAlignment(deleteButton, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM | AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
    deleteButton.addListener(new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent clickEvent) {
            horizontalLayout.removeAllComponents();
            horizontalLayout.detach();

        }
    });
}

    public void setAttributes(List<ValuePair> values) {
        this.values=values;
    }

    public String getParameterCaption() {
        String s=" AND";
        for(int i=0; i<getComponentCount(); i++)
        {
            HorizontalLayout layout = (HorizontalLayout) getComponent(i);
            for(int j=0; j<layout.getComponentCount(); j++) {
               Component component = layout.getComponent(j);
                if(component instanceof ComboBox) {
                    if(((ComboBox)component).getValue() instanceof filter) {
                        s = s+" "+((filter)((ComboBox)component).getValue()).getValue();
                    } else {
                        s = s + " " + ((ComboBox) component).getValue().toString();
                    }
                } else if (component instanceof TextField) {
                    s = s+" "+((TextField)component).getValue().toString();
                } else if (component instanceof DateField) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    s =  s+" to_date("+"\'"+dateFormat.format(((DateField)component).getValue())+"\', \'dd.mm.yyyy\')";
                }
            }
        }
        return s;
    }

    public String getValue() {
        String s="";
        for(int i=0; i<getComponentCount(); i++)
        {
            HorizontalLayout layout = (HorizontalLayout) getComponent(i);
            for(int j=0; j<layout.getComponentCount(); j++) {
                Component component = layout.getComponent(j);
                if(component instanceof ComboBox) {
                    if(((ComboBox)component).getDescription().equals("attributes"))
                        s = s+" AND ";
                    if(((ComboBox)component).getValue() instanceof filter) {
                        s = s+" "+((filter)((ComboBox)component).getValue()).getValue();
                    } else {
                        s = s + " " + ((ComboBox) component).getValue().toString();
                    }
                } else if (component instanceof TextField) {
                    s = s+" \'"+((TextField)component).getValue().toString()+"\'";
                } else if (component instanceof DateField) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    s = s+" to_date("+"\'"+dateFormat.format(((DateField)component).getValue())+"\', \'dd.mm.yyyy\')";
                }
            }
        }
        return s;
    }

    public void setFilters(String type, Component component) {
        ((ComboBox)component).removeAllItems();
        if(type.equals("VARCHAR2")) {
           ((ComboBox)component).addItem(stringFilter.EQUAL);
           ((ComboBox)component).addItem(stringFilter.NOTEQUAL);
       } else if(type.equals("BOOLEAN")) {
           ((ComboBox)component).addItem(booleanFilter.IS);
       } else {
           ((ComboBox)component).addItem(dateAndNumberFilter.EQUAL);
           ((ComboBox)component).addItem(dateAndNumberFilter.LESS);
           ((ComboBox)component).addItem(dateAndNumberFilter.LESSOREQUAL);
           ((ComboBox)component).addItem(dateAndNumberFilter.MORE);
           ((ComboBox)component).addItem(dateAndNumberFilter.MOREOREQUAL);
           ((ComboBox)component).addItem(dateAndNumberFilter.NOTEQUAL);
       }
    }

    public enum stringFilter implements filter {
        EQUAL("EQUAL", "="), NOTEQUAL("NOTEQUAL", "<>");

        private String description;
        private String value;

        stringFilter (String description, String value) {
            this.description = description;
            this.value = value;
        }

        @Override
        public String toString() {
            return description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum booleanFilter implements filter{
        IS("IS", "=");
        private String value;
        private String description;
        booleanFilter (String description, String value) {
            this.description = description;
            this.value = value;
        }
        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
        @Override
        public String toString() {
            return description;
        }
    }

    public enum booleanValues implements filter {
        TRUE("TRUE","TRUE"),
        FALSE("FALSE", "FALSE");
        private String value;
        private String description;
        booleanValues (String description, String value) {
            this.description = description;
            this.value = value;
        }
        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
        @Override
        public String toString() {
            return description;
        }
    }
    public enum dateAndNumberFilter implements filter {
        EQUAL("EQUAL","="),
        NOTEQUAL("NOTEQUAL", "<>"),
        MORE("MORE",">"),
        LESS("LESS","<"),
        MOREOREQUAL("MOREOREQUAL", ">="),
        LESSOREQUAL("LESSOREQUAL", "<=");

        private String value;
        private String description;
        dateAndNumberFilter (String description, String value) {
            this.description = description;
            this.value = value;
        }
        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
        @Override
        public String toString() {
            return description;
        }
    }

    public interface filter {
        public String getDescription();
        public String getValue();
    }
}
