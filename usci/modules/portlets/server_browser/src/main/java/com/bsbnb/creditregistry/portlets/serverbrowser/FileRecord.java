package com.bsbnb.creditregistry.portlets.serverbrowser;

import static com.bsbnb.creditregistry.portlets.serverbrowser.ServerBrowserComponent.logger;
import com.bsbnb.vaadin.messagebox.InputBox;
import com.bsbnb.vaadin.messagebox.InputBoxListener;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.bsbnb.vaadin.messagebox.MessageBoxButtons;
import com.bsbnb.vaadin.messagebox.MessageBoxListener;
import com.bsbnb.vaadin.messagebox.MessageBoxType;
import com.bsbnb.vaadin.messagebox.MessageResult;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.themes.BaseTheme;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class FileRecord {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private File file;
    private Icon icon;
    private String name;
    private Button deleteButton;
    private Button renameButton;
    private ServerBrowserComponent component;
    private NameLink nameLink;

    FileRecord(File file, ServerBrowserComponent component) {
        this(null, file, component);
    }

    FileRecord(String name, File file, ServerBrowserComponent component) {
        this.file = file;
        this.component = component;
        this.name = name;
        icon = createIcon(file);
        nameLink = createNameLink();
        initializeButtons();
    }

    private Icon createIcon(File f) {
        if (f == null) {
            return null;
        }
        if (f.isDirectory()) {
            if (f.getParentFile() == null) {
                return new Icon(IconResource.DRIVE_ICON);
            }
            return new Icon(IconResource.FOLDER_ICON);
        }
        return new Icon(IconResource.FILE_ICON);
    }

    private void initializeButtons() {
        if (file == null) {
            return;
        }

        if (canBeRenamed()) {
            renameButton = createTableButton(IconResource.RENAME_ICON, new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    showRenameDialog();
                }

            });
        }
        if (canBeDeleted()) {
            deleteButton = createTableButton(IconResource.DELETE_ICON, new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    showDeleteDialog();
                }
            });
        }
    }

    private boolean canBeRenamed() {
        return !"..".equals(name) && file.getParent() != null;
    }

    protected boolean canBeDeleted() {
        if (!file.isDirectory()) {
            return true;
        }
        if (file.getParent() == null) {
            return false;
        }
        String[] children = file.list();
        return children != null && children.length == 0;
    }

    private Button createTableButton(IconResource iconResource, Button.ClickListener clickListener) {
        Button button = new Button();
        button.setIcon(iconResource);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.setImmediate(true);
        button.addListener(clickListener);
        return button;
    }

    private void showRenameDialog() {
        InputBox.Show("Enter new file name", "Rename", new InputBoxListener() {
            public void inputResult(MessageResult result, String text) {
                if (result == MessageResult.OK) {
                    renameFileWithOverwriteHandling(text);
                }
            }
        }, MessageBoxButtons.OKCancel, MessageBoxType.Question, component.getWindow());
    }

    private void renameFileWithOverwriteHandling(String newName) {
        try {
            logger.log(Level.INFO, "Rename: {0}", newName);
            String newFilePath = file.getParent() + File.separator + newName;
            logger.log(Level.INFO, "New file path: {0}", newFilePath);
            final File newFile = new File(newFilePath);
            if (newFile.exists()) {
                MessageBox.Show("The file with this name already exists. Do you want to ovewrite existing file?", "File already exists", new MessageBoxListener() {
                    public void messageResult(MessageResult result) {
                        if (result == MessageResult.OK) {
                            renameFile(newFile);
                        }
                    }

                }, MessageBoxButtons.YesNo, component.getWindow());
            } else {
                renameFile(newFile);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    private void renameFile(File newFile) {
        boolean renameResult = file.renameTo(newFile);
        component.load(newFile.getParent());
        if (!renameResult) {
            MessageBox.Show("Rename failed", "Rename", component.getWindow());
        }
    }

    private void showDeleteDialog() {
        MessageBox.Show("Do you really want to delete " + file.getName() + " ?", "Delete file", new MessageBoxListener() {
            public void messageResult(MessageResult result) {
                if (result == MessageResult.OK) {
                    try {
                        file.delete();
                        component.load(file.getParent());
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Delete failed", e);
                    }
                }
            }
        }, MessageBoxButtons.YesNo, MessageBoxType.Question, component.getWindow());
    }

    public Button getName() {
        return nameLink;
    }

    private NameLink createNameLink() {
        if (name != null) {
            return new NameLink(name);
        }
        if (file != null) {
            return new NameLink(file.getName());
        }
        return null;
    }

    public Long getSize() {
        if (file == null || file.isDirectory()) {
            return null;
        }
        try {
            long length = file.length();
            return length;
        } catch (SecurityException se) {
            return null;
        }
    }

    public String getModificationTime() {
        if (file == null || file.isDirectory()) {
            return null;
        }
        try {
            return SIMPLE_DATE_FORMAT.format(new Date(file.lastModified()));
        } catch (SecurityException se) {
            return null;
        }
    }

    public Embedded getIcon() {
        return icon;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getRenameButton() {
        return renameButton;
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof FileRecord)) {
            return false;
        }
        FileRecord anotherFileRecord = (FileRecord) another;
        if (file == null) {
            if (anotherFileRecord.file != null) {
                return false;
            }
        } else {
            if (!file.equals(anotherFileRecord.file)) {
                return false;
            }
        }
        if (name == null) {
            return anotherFileRecord.name == null;
        }
        return name.equals(anotherFileRecord.name);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        if (file != null) {
            hash += file.hashCode() * 17;
        }
        if (name != null) {
            hash += name.hashCode() * 37;
        }
        return hash;

    }

    private class NameLink extends Button implements Comparable<NameLink> {

        private String name;

        NameLink(String name) {
            super(name, new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {
                    component.load(file == null ? null : file.getAbsolutePath());
                }
            });
            setStyleName(BaseTheme.BUTTON_LINK);
            this.name = name;
        }

        @Override
        public int compareTo(NameLink o) {
            if (o == null) {
                return -1;
            }
            return name.compareTo(o.name);
        }
    }

    public String getNameString() {
        if (name == null) {
            return file == null ? "" : file.getName();
        }
        return name;
    }
}
