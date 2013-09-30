package com.bsbnb.vaadin.filterableselector;

/**
 *
 * @author Aidar.Myrzahanov
 * Интерфейс Selectable необходим для предоставления информации об объекте 
 * фильтруемого выбора
 */
public interface Selector<T> {
    String getCaption(T item) ;
    Object getValue(T item);
    String getType(T item);
}
