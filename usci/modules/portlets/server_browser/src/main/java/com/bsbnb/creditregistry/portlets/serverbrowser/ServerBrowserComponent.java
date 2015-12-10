package com.bsbnb.creditregistry.portlets.serverbrowser;

import com.bsbnb.vaadin.messagebox.InputBox;
import com.bsbnb.vaadin.messagebox.InputBoxListener;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.bsbnb.vaadin.messagebox.MessageResult;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ServerBrowserComponent extends VerticalLayout implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

    public static final Logger logger = Logger.getLogger("Explorer");
    private static final long AUTO_UPDATE_THREAD_TIMEOUT_MILLIS = 600 * 1000;

    private final Button.ClickListener pathButtonListener = new Button.ClickListener() {
        @Override
        public void buttonClick(ClickEvent event) {
            PathButton button = (PathButton) event.getButton();
            load(button.getPath());
        }
    };

    private TextField addressField;
    private Button addToFavoritesButton;
    private TextField filenameFilterField;
    private HorizontalLayout addressNavigationLayout;
    private BeanItemContainer<FileRecord> filesContainer;
    private Table filesTable;
    private String currentPath;
    private boolean autoUpdateEnabled = false;
    private HorizontalLayout autoUpdateIndicatorLayout;
    private final FavoritesHandler favoritesHandler = new FavoritesHandler(new PathSelectedListener() {
        @Override
        public void selected(String path) {
            addressField.setValue(path);
            load(path);
        }
    });

    private static final DecimalFormat groupedDecimalFormat;

    static {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(' ');
        groupedDecimalFormat = (DecimalFormat) DecimalFormat.getInstance();
        groupedDecimalFormat.setDecimalFormatSymbols(dfs);
    }

    @Override
    public void attach() {
        setWidth("100%");
        setHeight("100%");

        setSpacing(false);
        addComponent(createAddressFieldLayout());
        addComponent(createAddressNavigationLayout());
        addComponent(createOperationsLayout());
        addComponent(createFilesTable());
        load(null);
    }

    private HorizontalLayout createAddressFieldLayout() {
        addressField = new TextField();
        addressField.addShortcutListener(new ShortcutListener("Enter to load", KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                if (addressField.getValue() != null) {
                    load(addressField.getValue().toString());
                }
            }
        });
        addressField.setInputPrompt("Enter file path");
        addressField.setImmediate(true);
        addressField.setWidth("100%");
        Button loadAddressButton = new Button("Load");
        loadAddressButton.setImmediate(true);
        loadAddressButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (addressField.getValue() != null) {
                    load(addressField.getValue().toString());
                }
            }
        });
        addToFavoritesButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addToFavorites();
            }
        });
        addToFavoritesButton.setIcon(IconResource.EMPTY_STAR_ICON);
        addToFavoritesButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button openFavoriteLocationButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                favoritesHandler.showFavoritesWindow(getWindow());
            }
        });
        openFavoriteLocationButton.setIcon(IconResource.OPEN_ICON);
        openFavoriteLocationButton.setStyleName(BaseTheme.BUTTON_LINK);
        HorizontalLayout addressFieldLayout = new HorizontalLayout();
        addressFieldLayout.addComponent(addressField);
        addressFieldLayout.setComponentAlignment(addressField, Alignment.MIDDLE_LEFT);
        addressFieldLayout.setExpandRatio(addressField, 1.0f);
        addressFieldLayout.addComponent(loadAddressButton);
        addressFieldLayout.setComponentAlignment(loadAddressButton, Alignment.MIDDLE_RIGHT);
        addressFieldLayout.addComponent(addToFavoritesButton);
        addressFieldLayout.setComponentAlignment(addToFavoritesButton, Alignment.MIDDLE_RIGHT);
        addressFieldLayout.addComponent(openFavoriteLocationButton);
        addressFieldLayout.setComponentAlignment(openFavoriteLocationButton, Alignment.MIDDLE_RIGHT);
        addressFieldLayout.setWidth("100%");
        addressFieldLayout.setSpacing(false);
        addressFieldLayout.setImmediate(true);
        return addressFieldLayout;
    }

    private HorizontalLayout createAddressNavigationLayout() {
        addressNavigationLayout = new HorizontalLayout();
        addressNavigationLayout.setSpacing(false);
        return addressNavigationLayout;
    }

    private Table createFilesTable() throws IllegalArgumentException {
        filesContainer = new BeanItemContainer<FileRecord>(FileRecord.class);
        filesTable = new Table(null, filesContainer) {
            @Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
                if ("size".equals(colId)) {
                    Long size = (Long) property.getValue();
                    if (size != null) {
                        return groupedDecimalFormat.format(size);
                    }
                }
                return super.formatPropertyValue(rowId, colId, property);
            }
        };
        filesTable.setVisibleColumns(new String[]{"icon", "name", "size", "deleteButton", "renameButton", "modificationTime"});
        filesTable.setColumnHeaders(new String[]{"", "Name", "Size", "Delete", "Rename", "Last Update"});
        filesTable.setColumnAlignment("size", Table.ALIGN_RIGHT);
        filesTable.setColumnAlignment("modificationTime", Table.ALIGN_RIGHT);
        filesTable.setColumnWidth("icon", 30);
        filesTable.setColumnWidth("size", 90);
        filesTable.setColumnWidth("deleteButton", 50);
        filesTable.setColumnWidth("renameButton", 50);
        filesTable.setColumnWidth("modificationTime", 120);
        filesTable.setWidth("100%");
        return filesTable;
    }

    private HorizontalLayout createOperationsLayout() {
        Button newFolderButton = new Button();
        newFolderButton.setStyleName(BaseTheme.BUTTON_LINK);
        newFolderButton.setDescription("Create new folder");
        newFolderButton.setIcon(IconResource.NEW_FOLDER_ICON);
        newFolderButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                showCreateNewFolderDialog();
            }
        });
        Upload upload = new Upload(null, this);
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
        upload.setImmediate(true);

        CheckBox enableAutoUpdateCheckBox = new CheckBox("Autoupdate", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                boolean enabled = event.getButton().booleanValue();
                setAutoUpdate(enabled);
            }

        });
        enableAutoUpdateCheckBox.setImmediate(true);

        autoUpdateIndicatorLayout = new HorizontalLayout();

        filenameFilterField = new TextField(null, "");
        filenameFilterField.setInputPrompt("Filter");
        filenameFilterField.setWidth(100, Sizeable.UNITS_PIXELS);
        filenameFilterField.addListener(new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                setFilter(event.getText(), false);
            }
        });
        HorizontalLayout operationsLayout = new HorizontalLayout();
        operationsLayout.setSpacing(false);
        operationsLayout.addComponent(newFolderButton);
        operationsLayout.setComponentAlignment(newFolderButton, Alignment.MIDDLE_LEFT);
        operationsLayout.addComponent(upload);
        operationsLayout.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);
        operationsLayout.addComponent(enableAutoUpdateCheckBox);
        operationsLayout.setComponentAlignment(enableAutoUpdateCheckBox, Alignment.MIDDLE_LEFT);
        operationsLayout.addComponent(autoUpdateIndicatorLayout);
        operationsLayout.setComponentAlignment(autoUpdateIndicatorLayout, Alignment.MIDDLE_LEFT);
        operationsLayout.addComponent(filenameFilterField);
        operationsLayout.setComponentAlignment(filenameFilterField, Alignment.MIDDLE_RIGHT);
        operationsLayout.setWidth("100%");
        return operationsLayout;
    }

    private void setAutoUpdate(boolean enabled) {
        if (enabled) {
            autoUpdateEnabled = true;
            ProgressIndicator indicator = new ProgressIndicator();
            indicator.setPollingInterval(500);
            indicator.setIndeterminate(true);
            Thread updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    autoUpdate();
                }
            });
            updateThread.start();
            autoUpdateIndicatorLayout.addComponent(indicator);
        } else {
            autoUpdateEnabled = false;
            autoUpdateIndicatorLayout.removeAllComponents();
        }
    }

    private void autoUpdate() {
        long startTimeMillis = System.currentTimeMillis();
        while (autoUpdateEnabled && System.currentTimeMillis() - startTimeMillis < AUTO_UPDATE_THREAD_TIMEOUT_MILLIS) {
            synchronized (getApplication()) {
                loadCurrentPath();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.log(Level.WARNING, "Thread interrupted", ie);
            }
        }
        autoUpdateEnabled = false;
        autoUpdateIndicatorLayout.removeAllComponents();
    }

    private void downloadFile(File file) {
        FileResource resource = new FileResource(file, getApplication());
        getWindow().open(resource, "_blank");
    }

    public void load(String path) {
        if (currentPath == null || !currentPath.equals(path)) {
            setFilter(null, true);
            filesTable.setSortContainerPropertyId(null);
        }
        currentPath = path;
        try {
            loadCurrentPath();
        } catch (Exception exception) {
            handleException(exception);
        }
        addressField.focus();
    }

    private void handleException(Exception exception) {
        StringBuilder message = new StringBuilder(exception.getClass().getCanonicalName() + ": " + exception.getMessage());
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            message.append("\n\t");
            message.append(stackTraceElement.toString());
        }
        String messageString = message.toString();
        logger.log(Level.INFO, "Exception occured while loading path: {0}", messageString);
        MessageBox.Show(exception.getMessage(), "Error", getWindow());
    }

    private void updateFavoritesIcon() {
        if (favoritesHandler.isFavorite(currentPath)) {
            addToFavoritesButton.setIcon(IconResource.STAR_ICON);
        } else {
            addToFavoritesButton.setIcon(IconResource.EMPTY_STAR_ICON);
        }
    }

    private void loadCurrentPath() {
        updateFavoritesIcon();
        initializeContent();
        initializeAddress();
    }

    private void loadRootLocation() throws Property.ConversionException, Property.ReadOnlyException {
        File[] roots = File.listRoots();
        List<FileRecord> records = new ArrayList<FileRecord>(roots.length);
        for (File root : roots) {
            if (root.exists()) {
                records.add(new FileRecord(root.getAbsolutePath().charAt(0) + "", root, this));
            }
        }
        loadRecords(records);
    }

    public void initializeContent() {
        if (currentPath == null) {
            loadRootLocation();
            return;
        }
        File file = new File(new File(currentPath).getAbsolutePath());
        if (!file.exists()) {
            Window.Notification notification = new Window.Notification("Path doesn't exist", currentPath, Window.Notification.DELAY_FOREVER);
            (getWindow()).showNotification(notification);
            return;
        }
        if (file.isDirectory()) {
            loadDirectory(file);
        } else {
            downloadFile(file);
            loadDirectory(file.getParentFile());
        }
    }

    private void loadDirectory(File file) {
        if (file == null) {
            return;
        }
        File[] childFiles = file.listFiles();
        List<FileRecord> records = new ArrayList<FileRecord>(childFiles.length + 1);
        records.add(new FileRecord("..", file.getParentFile(), this));
        for (File child : childFiles) {
            records.add(new FileRecord(child, this));
        }
        loadRecords(records);
    }

    private void initializeAddress() {
        addressNavigationLayout.removeAllComponents();
        if (currentPath == null) {
            addressField.setValue("");
            return;
        }
        if (!currentPath.equals(addressField.getValue())) {
            addressField.setValue(currentPath);
        }
        File file = new File(currentPath);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }

        List<PathButton> buttons = new ArrayList<PathButton>();

        while (file != null) {
            String name = file.getName();
            if (name.isEmpty()) {
                name = file.getAbsolutePath();
            }
            buttons.add(new PathButton(name, file.getAbsolutePath(), pathButtonListener));
            file = file.getParentFile();
        }
        buttons.add(new PathButton("...", null, pathButtonListener));
        for (int i = buttons.size() - 1; i >= 0; i--) {
            addressNavigationLayout.addComponent(buttons.get(i));
            if (i > 0) {
                Icon breadcrumbIcon = new Icon(IconResource.BREADCRUMB_ICON);
                addressNavigationLayout.addComponent(breadcrumbIcon);
            }
        }
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        File currentDir = new File(currentPath);
        String errorString;
        if (currentDir.exists()) {
            if (currentDir.isDirectory()) {
                try {
                    File newFile = new File(currentDir, filename);
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                        return new FileOutputStream(newFile);
                    } else {
                        errorString = "File already exists";
                    }
                } catch (Exception exception) {
                    StringBuilder errorMessage = new StringBuilder("Exception " + exception.getClass().getCanonicalName() + "<br>\n");
                    for (StackTraceElement ste : exception.getStackTrace()) {
                        errorMessage.append(ste.toString());
                        errorMessage.append("<br>\n");
                    }
                    errorString = errorMessage.toString();
                }
            } else {
                errorString = "Current path is not a directory";
            }
        } else {
            errorString = "Current dir does not exist";
        }
        (getWindow()).showNotification("Error", errorString, Window.Notification.TYPE_ERROR_MESSAGE);
        return null;
    }

    public void uploadSucceeded(SucceededEvent event) {
        load(currentPath);
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        String reasonMessage = event.getReason() == null ? "null" : event.getReason().getMessage();
        MessageBox.Show("Upload failed for reason: " + reasonMessage, "Upload failed", getWindow());
    }

    private void addToFavorites() {
        if (currentPath == null) {
            return;
        }
        if (favoritesHandler.isFavorite(currentPath)) {
            favoritesHandler.remove(currentPath);
            addToFavoritesButton.setIcon(IconResource.EMPTY_STAR_ICON);
        } else {
            favoritesHandler.add(currentPath);
            addToFavoritesButton.setIcon(IconResource.STAR_ICON);
        }
    }

    private void showCreateNewFolderDialog() {
        InputBox.Show("Enter folder name", "New folder", new InputBoxListener() {
            public void inputResult(MessageResult result, String text) {
                try {
                    if (result == MessageResult.OK) {
                        createNewFolder(text);
                    }
                } catch (Exception e) {
                    MessageBox.Show("Failed: " + e.getMessage(), getApplication().getMainWindow());
                }
            }

        }, getApplication().getMainWindow());
    }

    private void createNewFolder(String text) {
        File parentDir = new File(currentPath);
        if (!parentDir.isDirectory()) {
            parentDir = parentDir.getParentFile();
        }
        if (parentDir == null || !parentDir.isDirectory()) {
            return;
        }
        File newDir = new File(parentDir.getAbsolutePath() + "\\" + text);
        if (newDir.exists()) {
            MessageBox.Show("Folder already exists", getWindow());
            return;
        }
        newDir.mkdir();
        load(parentDir.getAbsolutePath());
    }

    private void setFilter(final String text, boolean updateFilterField) {
        filesContainer.removeAllContainerFilters();
        if (text != null) {
            filesContainer.addContainerFilter(new Container.Filter() {

                @Override
                public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                    if (itemId instanceof FileRecord) {
                        FileRecord record = (FileRecord) itemId;
                        String nameString = record.getNameString();
                        if ("..".equals(nameString)) {
                            return true;
                        }
                        return nameString.toLowerCase().contains(text.toLowerCase());
                    }
                    return true;
                }

                @Override
                public boolean appliesToProperty(Object propertyId) {
                    return "nameString".equals(propertyId);
                }
            });
        }
        if (updateFilterField) {
            filenameFilterField.setValue(text == null ? "" : text);
        }
    }

    private void loadRecords(List<FileRecord> records) {
        filesContainer.removeAllItems();
        filesContainer.addAll(records);
        filesTable.sort();
    }
}
