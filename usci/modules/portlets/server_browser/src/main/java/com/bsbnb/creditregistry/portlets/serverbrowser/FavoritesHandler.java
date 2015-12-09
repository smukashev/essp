package com.bsbnb.creditregistry.portlets.serverbrowser;

import static com.bsbnb.creditregistry.portlets.serverbrowser.ServerBrowserComponent.logger;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
class FavoritesHandler {
    
    private static final String BOOKMARKS_FILE_NAME = "serverbrowser.config";
    private final Set<String> favorites = new TreeSet<String>();
    private final PathSelectedListener listener;
    
    FavoritesHandler(PathSelectedListener listener) {
        this.listener = listener;
        readFavorites();
    }

    private void readFavorites() {
        try {
            File configFile = new File(BOOKMARKS_FILE_NAME);
            if (configFile.exists()) {
                Scanner in = new Scanner(configFile);
                favorites.clear();
                try {
                    while (in.hasNextLine()) {
                        favorites.add(in.nextLine());
                    }
                } finally {
                    in.close();
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "", ioe);
        }
    }

    private void saveFavorites() {
        try {
            File favoritesFile = new File(BOOKMARKS_FILE_NAME);
            if (!favoritesFile.exists()) {
                favoritesFile.createNewFile();
            }
            PrintWriter out = new PrintWriter(favoritesFile);
            try {
                for (String favoriteLocation : favorites) {
                    out.println(favoriteLocation);
                }
            } finally {
                out.close();
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "", ioe);
        }
    }

    void showFavoritesWindow(final Window window) throws Property.ConversionException, IllegalArgumentException, Property.ReadOnlyException {
        if (favorites.isEmpty()) {
            MessageBox.Show("No favorites", "No favorites", window);
            return;
        }
        final Window favoriteLocationsWindow = new Window();
        favoriteLocationsWindow.setModal(true);
        final IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("path", String.class, "");
        for (String favoriteLocation : favorites) {
            Item newItem = container.addItem(favoriteLocation);
            Property pathProperty = newItem.getItemProperty("path");
            pathProperty.setValue(favoriteLocation);
        }
        final Table favoritesTable = new Table("", container);
        favoritesTable.setSelectable(true);
        favoritesTable.setMultiSelect(false);
        favoritesTable.setWidth("100%");
        favoritesTable.setImmediate(true);
        TextField filterField = new TextField();
        filterField.setImmediate(true);
        filterField.addListener(new FieldEvents.TextChangeListener() {
            public void textChange(FieldEvents.TextChangeEvent event) {
                container.removeAllContainerFilters();
                if (event.getText() != null) {
                    container.addContainerFilter(new SimpleStringFilter("path", event.getText(), true, false));
                }
            }
        });
        filterField.setWidth("100%");
        Button selectButton = new Button("Select", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                Object selectedLocation = favoritesTable.getValue();
                if (selectedLocation == null && favoritesTable.getItemIds().size() == 1) {
                    selectedLocation = favoritesTable.getItemIds().iterator().next();
                }
                if (selectedLocation != null) {
                    listener.selected(selectedLocation.toString());
                    window.removeWindow(favoriteLocationsWindow);
                }
            }
        });
        selectButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(filterField);
        layout.addComponent(favoritesTable);
        layout.addComponent(selectButton);
        layout.setSpacing(false);
        favoriteLocationsWindow.setWidth(700, Window.UNITS_PIXELS);
        favoriteLocationsWindow.addComponent(layout);
        favoriteLocationsWindow.setHeight(null);
        favoriteLocationsWindow.setIcon(IconResource.STAR_ICON);
        favoriteLocationsWindow.setCaption("Favorites");
        window.addWindow(favoriteLocationsWindow);
        filterField.focus();
    }
    
    boolean isFavorite(String path) {
        if(path==null) {
            return false;
        }
        return favorites.contains(path);
    }

    void remove(String currentPath) {
        if(currentPath!=null) {
            favorites.remove(currentPath);
        }
        saveFavorites();
    }

    void add(String currentPath) {
        if(currentPath!=null) {
            favorites.add(currentPath);
        }
        saveFavorites();
    }
}
