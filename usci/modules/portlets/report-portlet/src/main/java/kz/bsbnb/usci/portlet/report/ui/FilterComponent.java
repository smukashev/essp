package kz.bsbnb.usci.portlet.report.ui;

import com.vaadin.data.Property;
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
    Component oldComponent;

public void createNewFilter() {
    final HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.setSpacing(true);
    horizontalLayout.setMargin(true);
    addComponent(horizontalLayout);
    ComboBox attributes = new ComboBox();
    attributes.setCaption("Столбцы");
    attributes.setDescription("attributes");
    attributes.setImmediate(true);
    attributes.setNewItemsAllowed(false);
    attributes.setNullSelectionAllowed(false);
    attributes.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
    attributes.removeAllItems();
    for (ValuePair value : values) {
        attributes.addItem(value);
    }
    final ComboBox filters = new ComboBox();
    filters.setCaption("Фильтр");
    filters.setDescription("filter");
    final Button deleteButton = new Button("Удалить фильтр");
    final Component component = new TextField();
    oldComponent = component;
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
            horizontalLayout.replaceComponent(oldComponent, valueComponent);
            oldComponent = valueComponent;
            horizontalLayout.removeComponent(deleteButton);
            horizontalLayout.addComponent(deleteButton);
        }
    });
    horizontalLayout.addComponent(attributes);
    horizontalLayout.addComponent(filters);
    horizontalLayout.addComponent(component);
    horizontalLayout.addComponent(deleteButton);
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
                    s = s+" "+((ComboBox)component).getValue().toString();
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
                    s = s+" "+((ComboBox)component).getValue().toString();
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

    public enum stringFilter {
        EQUAL("="), NOTEQUAL("<>");

        private String name;
        stringFilter (String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }
    public enum booleanFilter {
        IS("=");
        private String name;
        booleanFilter (String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum booleanValues {
        TRUE("TRUE"),
        FALSE("FALSE");
        private String name;
        booleanValues (String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    public enum dateAndNumberFilter {
        EQUAL("="),
        NOTEQUAL("<>"),
        MORE(">"),
        LESS("<"),
        MOREOREQUAL(">="),
        LESSOREQUAL("<=");

        private String name;
        dateAndNumberFilter (String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
