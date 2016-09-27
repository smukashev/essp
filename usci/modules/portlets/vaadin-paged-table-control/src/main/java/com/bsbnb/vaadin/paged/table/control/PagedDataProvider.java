package com.bsbnb.vaadin.paged.table.control;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PagedDataProvider<T> {

    int getCount();

    List<T> getRecords(int firstIndex, int count);

}
