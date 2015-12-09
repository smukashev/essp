package com.bsbnb.creditregistry.portlets.serverbrowser;

import com.vaadin.ui.Embedded;

/**
 *
 * @author Aidar.Myrzahanov
 */
class Icon extends Embedded implements Comparable<Icon> {

    private final IconResource iconResource;

    Icon(IconResource resource) {
        super(null, resource);
        this.iconResource = resource;
    }

    @Override
    public int compareTo(Icon anotherIcon) {
        if (anotherIcon == null) {
            return 1;
        }
        return iconResource.getResourceName().compareTo(anotherIcon.iconResource.getResourceName());
    }
}
