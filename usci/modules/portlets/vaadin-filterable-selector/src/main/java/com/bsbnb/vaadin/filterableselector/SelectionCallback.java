package com.bsbnb.vaadin.filterableselector;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface SelectionCallback<U> {

    public void selected(List<U> selectedItems);
    
}
