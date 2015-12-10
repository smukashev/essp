package com.bsbnb.creditregistry.portlets.serverbrowser;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar.Myrzahanov
 */
class IconResource extends ExternalResource {

    public static final IconResource FOLDER_ICON = new IconResource("folder_icon.png");
    public static final IconResource FILE_ICON = new IconResource("file_icon.png");
    public static final IconResource DOWNLOAD_ICON = new IconResource("download_icon.png");
    public static final IconResource DELETE_ICON = new IconResource("delete_icon.png");
    public static final IconResource RENAME_ICON = new IconResource("rename_icon.png");
    public static final IconResource DRIVE_ICON = new IconResource("drive_icon.png");
    public static final IconResource STAR_ICON = new IconResource("star_icon.png");
    public static final IconResource OPEN_ICON = new IconResource("open_icon.png");
    public static final IconResource EMPTY_STAR_ICON = new IconResource("empty_star_icon.png");
    public static final IconResource NEW_FOLDER_ICON = new IconResource("new_folder.png");
    public static final IconResource BREADCRUMB_ICON = new IconResource("breadcrumb.png");
    private static final String ICONS_LOCATION = "/static-usci/icons/";
    private final String resourceName;

    private IconResource(String name) {
        super( ICONS_LOCATION + name);
        resourceName = name;
    }

    public String getResourceName() {
        return resourceName;
    }
}
