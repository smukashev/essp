package com.bsbnb.vaadin.filterableselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.bsbnb.vaadin.messagebox.MessageBoxButtons;
import com.bsbnb.vaadin.messagebox.MessageBoxListener;
import com.bsbnb.vaadin.messagebox.MessageResult;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class FilterableSelect<T> extends VerticalLayout {

    private List<FilterableItem<T>> itemsList;
    private Label displayLabel;
    private TextField selectFilterField;
    private String filterText = "";
    private Label creditorsHeaderLabel;
    private ListSelect elementsSelect;
    private BeanItemContainer<FilterableItem<T>> elementsContainer;
    private CheckBox[] elementsTypeCheckBoxes;
    private Set<String> selectedCreditorTypes;

    public FilterableSelect(List<T> aCreditorsList, Selector<T> selector) {
        this.itemsList = new ArrayList<FilterableItem<T>>(aCreditorsList.size());
        for(T item : aCreditorsList) {
            this.itemsList.add(new FilterableItem<T>(item, selector));
        }
    }

    @Override
    public void attach() {
        if (itemsList.isEmpty()) {
            displayLabel = new Label("<h2>" + Localization.USER_HAS_NO_ACCESS_TO_CREDITORS.getValue() + "</h2>", Label.CONTENT_XHTML);
            addComponent(displayLabel);
        } else if (itemsList.size() == 1) {
            displayLabel = new Label("<h2>" + itemsList.get(0).getCaption() + "</h2>", Label.CONTENT_XHTML);

            Label creditorTypeLabel = null;
            creditorTypeLabel = new Label("", Label.CONTENT_XHTML);

            if(itemsList.get(0).getType() != null)
                creditorTypeLabel = new Label("<h3>" + itemsList.get(0).getType() + "</h3>", Label.CONTENT_XHTML);

            addComponent(displayLabel);
            addComponent(creditorTypeLabel);
        } else {

            creditorsHeaderLabel = new Label("<h2>" + Localization.CREDITORS_HEADER.getValue() + "</h2>", Label.CONTENT_XHTML);

            Set<String> subjectTypes = new HashSet<String>();
            for (FilterableItem<T> item : itemsList) {
                subjectTypes.add(item.getType());
            }
            //checkBoxLayout
            HorizontalLayout creditorTypesLayout = new HorizontalLayout();
            creditorTypesLayout.setSpacing(true);
            selectedCreditorTypes = new HashSet<String>();
            if (subjectTypes.size() > 1) {
                elementsTypeCheckBoxes = new CheckBox[subjectTypes.size()];
                
                int counter = 0;
                for (final String typeName : subjectTypes) {
                    elementsTypeCheckBoxes[counter] = new CheckBox(typeName);
                    elementsTypeCheckBoxes[counter].setImmediate(true);
                    elementsTypeCheckBoxes[counter].addListener(new ValueChangeListener() {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            if (event.getProperty() == null) {
                                return;
                            }
                            Boolean value = (Boolean) event.getProperty().getValue();
                            if (value) {
                                selectedCreditorTypes.add(typeName);
                            } else {
                                selectedCreditorTypes.remove(typeName);
                            }
                            updateCreditorsTable();
                        }
                    });
                    creditorTypesLayout.addComponent(elementsTypeCheckBoxes[counter]);
                    counter++;
                }
                Button markAllButton = new Button(Localization.MARK_ALL_BUTTON_CAPTION.getValue(), new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        for (CheckBox checkBox : elementsTypeCheckBoxes) {
                            checkBox.setValue(true);
                        }
                    }
                });
                markAllButton.setImmediate(true);
                Button unmarkAllButton = new Button(Localization.UNMARK_ALL_BUTTON_CAPTION.getValue(), new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        for (CheckBox checkBox : elementsTypeCheckBoxes) {
                            checkBox.setValue(false);
                        }
                    }
                });
                unmarkAllButton.setImmediate(true);
                creditorTypesLayout.addComponent(markAllButton);
                creditorTypesLayout.addComponent(unmarkAllButton);
            } else {
                selectedCreditorTypes.addAll(subjectTypes);
            }

            selectFilterField = new TextField("");
            selectFilterField.addListener(new TextChangeListener() {

                @Override
                public void textChange(TextChangeEvent event) {
                    filterText = event.getText();
                    if (filterText == null) {
                        filterText = "";
                    }
                    updateCreditorsTable();
                }
            });
            selectFilterField.setWidth("100%");
            selectFilterField.setImmediate(true);

            elementsContainer = new BeanItemContainer<FilterableItem<T>>(FilterableItem.class, itemsList);
            elementsSelect = new ListSelect("", elementsContainer);
            elementsSelect.setMultiSelect(true);
            elementsSelect.setItemCaptionPropertyId("caption");
            elementsSelect.setImmediate(true);
            elementsSelect.setWidth("100%");
            elementsSelect.setNewItemsAllowed(false);
            elementsSelect.setNullSelectionAllowed(false);
            Button selectAllBanksButton = new Button(Localization.SELECT_ALL_BUTTON_CAPTION.getValue());
            selectAllBanksButton.setValue(true);
            selectAllBanksButton.setImmediate(true);
            selectAllBanksButton.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    elementsSelect.setValue(elementsSelect.getItemIds());
                }
            });

            addComponent(creditorsHeaderLabel);
            if (subjectTypes.size() > 1) {
                addComponent(creditorTypesLayout);
                if (elementsTypeCheckBoxes != null) {
                    for (CheckBox checkBox : elementsTypeCheckBoxes) {
                        checkBox.setValue(true);
                    }
                }
            }
            addComponent(selectFilterField);
            addComponent(elementsSelect);
            addComponent(selectAllBanksButton);
            selectFilterField.focus();
        }
    }

    private void updateCreditorsTable() {
        elementsSelect.removeAllItems();
        for (FilterableItem<T> item : itemsList) {
                if (selectedCreditorTypes.contains(item.getType()) && item.getCaption().toLowerCase().contains(filterText.toLowerCase())) {
                elementsSelect.addItem(item);
            }
        }
    }

    public boolean hasElements() {
        return !itemsList.isEmpty();
    }
    
    private List<T> getOriginallyTypedList(Collection<FilterableItem<T>> items) {
        List<T> resultList = new ArrayList<T>(items.size());
        for(FilterableItem<T> item : items) {
            resultList.add(item.getItem());
        }
        return resultList;
    }

    public void getSelectedElements(final SelectionCallback<T> callBack) {
        if (itemsList.size() == 1) {
            callBack.selected(getOriginallyTypedList(itemsList));
            return;
        }
        Map<Object, FilterableItem<T>> tempMap = new HashMap<Object, FilterableItem<T>>();
        Collection<FilterableItem<T>> coll = (Collection<FilterableItem<T>>) elementsSelect.getValue();
        for (FilterableItem<T> c : coll) {
            tempMap.put(c.getValue(), c);
        }
        if (tempMap.isEmpty()) {
            final Collection<FilterableItem<T>> itemIds = elementsContainer.getItemIds();
            if (itemIds.size() == 1) {
                elementsSelect.setValue(itemIds);
                callBack.selected(getOriginallyTypedList(itemIds));
            } else {
                MessageBox.Show(Localization.SELECTION_EMPTY_PROMPT.getValue(), Localization.SELECTION_EMPTY_CAPTION.getValue(), new MessageBoxListener() {

                    @Override
                    public void messageResult(MessageResult result) {
                        if (result == MessageResult.OK) {
                            elementsSelect.setValue(itemIds);
                            callBack.selected(getOriginallyTypedList(itemIds));
                        }
                    }
                }, MessageBoxButtons.OKCancel, getWindow());
            }
        } else {
            callBack.selected(getOriginallyTypedList(tempMap.values()));
        }
    }
    
    public boolean containsElement(Object elementId) {
        for(FilterableItem<T> item : elementsContainer.getItemIds()) {
            if(item.getValue().equals(elementId)) {
                return true;
            }
        }
        return false;
    }
    
    public void selectElements(Object[] elementIds) {
        Collection<FilterableItem<T>> selected = new HashSet<FilterableItem<T>>();
        Set<Object> elementIdsSet = new HashSet<Object>(Arrays.asList(elementIds));
        for(FilterableItem<T> item : elementsContainer.getItemIds()) {
            if(elementIdsSet.contains(item.getValue())) {
                selected.add(item);
            }
        }
        elementsSelect.setValue(selected);
    }
}
