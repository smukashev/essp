package com.bsbnb.creditregistry.portlets.administration.ui;

import com.bsbnb.creditregistry.portlets.administration.PortletIcon;
import com.bsbnb.creditregistry.portlets.administration.data.DataProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import kz.bsbnb.usci.cr.model.Creditor;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Marat Madybayev
 */
public class MainSplitPanel extends HorizontalSplitPanel {

    private static final long serialVersionUID = 9134226451694477874L;
    private static final String[] CREDITOR_COLUMNS = new String[]{"name"};
    private List<Creditor> creditorList;
    private DataProvider provider;
    private ResourceBundle bundle;
    private Table usersTable;
    private Table tableAvailableCreditors;
    private Table tableUserCreditors;
    private BeanItemContainer<Creditor> availableCreditorsContainer;
    private BeanItemContainer<Creditor> userCreditorsContainer;
    private IconButton addButton;
    private IconButton removeButton;
    private BeanItemContainer<User> usersContainer;
    private static final Logger logger = Logger.getLogger(MainSplitPanel.class);

    public MainSplitPanel(ResourceBundle bundle, DataProvider provider, final User user) {
        this.bundle = bundle;
        this.provider = provider;
        setHeight("428px");
        setWidth("100%");
        setSplitPosition(150, Sizeable.UNITS_PIXELS);

        usersTable = new Table();
        usersTable.setHeight("428px");
        usersTable.setWidth("100%");
        usersTable.setSelectable(true);
        try {
            List<User> listUser = UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount());
            Collections.sort(listUser, new Comparator<User>() {

                public int compare(User o1, User o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    return o1.getEmailAddress().compareTo(o2.getEmailAddress());
                }
            });
            usersContainer = new BeanItemContainer<User>(User.class, listUser);
            usersTable.setContainerDataSource(usersContainer);
        } catch (SystemException ex) {
            logger.error(ex, ex);
        }

        usersTable.setVisibleColumns(new Object[]{"login"});
        usersTable.setColumnHeaders(new String[]{bundle.getString("Login")});
        usersTable.setImmediate(true);
        usersTable.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                tableAvailableCreditors.setValue(null);
                tableUserCreditors.setValue(null);
                updateTables();
            }
        });

        tableAvailableCreditors = new Table();
        tableAvailableCreditors.setHeight("200px");
        tableAvailableCreditors.setWidth("100%");
        tableAvailableCreditors.setSelectable(true);
        tableAvailableCreditors.setMultiSelect(true);
        // получаем список банков
        creditorList = provider.getAllCreditors();
        availableCreditorsContainer = new BeanItemContainer<Creditor>(Creditor.class, creditorList);
        tableAvailableCreditors.setContainerDataSource(availableCreditorsContainer);
        tableAvailableCreditors.setVisibleColumns(CREDITOR_COLUMNS);
        tableAvailableCreditors.setColumnHeaders(new String[]{bundle.getString("Name")});
        tableAvailableCreditors.setImmediate(true);
        tableAvailableCreditors.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                Collection selectedCreditors = (Collection) event.getProperty().getValue();
                if (selectedCreditors == null || selectedCreditors.isEmpty()) {
                    addButton.setEnabled(false);
                } else {
                    addButton.setEnabled(true);
                }
            }
        });

        tableUserCreditors = new Table();
        tableUserCreditors.setHeight("200px");
        tableUserCreditors.setWidth("100%");
        tableUserCreditors.setSelectable(true);
        tableUserCreditors.setImmediate(true);
        tableUserCreditors.setMultiSelect(true);
        tableUserCreditors.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                Collection selectedCreditors = (Collection) event.getProperty().getValue();
                if (selectedCreditors == null || selectedCreditors.isEmpty()) {
                    removeButton.setEnabled(false);
                } else {
                    removeButton.setEnabled(true);
                }
            }
        });


        userCreditorsContainer = new BeanItemContainer<Creditor>(Creditor.class);
        tableUserCreditors.setContainerDataSource(userCreditorsContainer);
        tableUserCreditors.setVisibleColumns(CREDITOR_COLUMNS);
        tableUserCreditors.setColumnHeaders(new String[]{bundle.getString("Name")});

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        addButton = new IconButton(bundle.getString("Add"), PortletIcon.DOWN_ICON, new ClickListener() {

            public void buttonClick(ClickEvent event) {
                addSelectedCreditors(user);
            }
        });
        addButton.setEnabled(false);
        removeButton = new IconButton(bundle.getString("Remove"), PortletIcon.UP_ICON, new ClickListener() {

            public void buttonClick(ClickEvent event) {
                removeSelectedCreditors(user);
            }
        });
        removeButton.setEnabled(false);
        buttonsLayout.addComponent(addButton);
        buttonsLayout.addComponent(removeButton);
        TextField filterField = new TextField();
        filterField.setWidth("100%");
        filterField.setImmediate(true);
        filterField.addListener(new TextChangeListener() {

            public void textChange(TextChangeEvent event) {
                usersContainer.removeAllContainerFilters();
                String filterText = event.getText();
                if (filterText != null && !filterText.isEmpty()) {
                    usersContainer.addContainerFilter(new SimpleStringFilter("login", filterText, true, false));
                }
            }
        });
        VerticalLayout leftPanel = new VerticalLayout();
        leftPanel.addComponent(filterField);
        leftPanel.addComponent(usersTable);
        leftPanel.setHeight("100%");

        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.addComponent(tableAvailableCreditors);
        rightPanel.addComponent(buttonsLayout);
        rightPanel.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
        rightPanel.addComponent(tableUserCreditors);
        rightPanel.setHeight("100%");
        addComponent(leftPanel);
        addComponent(rightPanel);
    }

    private static boolean isNationalBankEmployee(User user) {
        try {
            return ExpandoValueLocalServiceUtil.getData(user.getCompanyId(), User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, "isNb", user.getPrimaryKey(), false);
        } catch (PortalException pe) {
            logger.error(null, pe);
        } catch (SystemException se) {
            logger.error(null, se);
        }
        return false;
    }

    public void addSelectedCreditors(User user) {
        User selectedUser = (User) usersTable.getValue();
        if (selectedUser != null) {
            boolean isUserNBEmployee = isNationalBankEmployee(selectedUser);
            int userCreditorsCount = tableUserCreditors.size();
            Collection<Creditor> creditorsToAdd = (Collection<Creditor>) tableAvailableCreditors.getValue();
            for (Creditor creditorToAdd : creditorsToAdd) {
                if ((isUserNBEmployee) || (!isUserNBEmployee && userCreditorsCount == 0)) {
                    logger.info("Adding creditor: " + creditorToAdd.getName());
                    provider.addUserCreditor(selectedUser, creditorToAdd);
                    provider.addUserLogs("ADMINISTRATION",user.getFullName(),"ADD: "+selectedUser.getFullName()+" to "+creditorToAdd.getName());
                    userCreditorsCount++;
                } else {
                    showUnexpectedError(bundle.getString("NAddCreditorCaption"), bundle.getString("NAddCreditorDescrNB"));
                    break;
                }
            }
            tableAvailableCreditors.setValue(null);
            updateTables();
        }
    }

    public void removeSelectedCreditors(User user) {
        Collection<Creditor> creditorsToRemove = (Collection<Creditor>) tableUserCreditors.getValue();
        User selectedUser = (User) usersTable.getValue();
        if (selectedUser != null && creditorsToRemove != null && !creditorsToRemove.isEmpty()) {
            for (Creditor creditorToRemove : creditorsToRemove) {
                provider.removeUserCreditor(selectedUser, creditorToRemove);
                provider.removeUserLogs("ADMINISTRATION",user.getFullName(),"REMOVE: "+selectedUser.getFullName()+" "+creditorToRemove.getName());
            }
            tableUserCreditors.setValue(null);
            updateTables();
        } else {
            showUnexpectedError(bundle.getString("NRemoveCreditorCaption"), bundle.getString("NSelDescr"));
        }
    }

    public void showUnexpectedError(String caption, String desc) {
        getWindow().showNotification(caption, desc);
    }

    public void updateTables() {
        userCreditorsContainer.removeAllItems();
        availableCreditorsContainer.removeAllItems();
        User selectedUser = (User) usersTable.getValue();
        if (selectedUser != null) {
            List<Creditor> userCreditorList = provider.getUsersCreditors(selectedUser);
            userCreditorsContainer.addAll(userCreditorList);
            ArrayList<Creditor> availableCreditorsList = new ArrayList<Creditor>(creditorList);
            availableCreditorsList.removeAll(userCreditorList);
            availableCreditorsContainer.addAll(availableCreditorsList);
        }
    }
}
