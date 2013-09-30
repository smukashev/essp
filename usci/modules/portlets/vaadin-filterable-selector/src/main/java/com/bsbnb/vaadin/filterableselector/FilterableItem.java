package com.bsbnb.vaadin.filterableselector;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class FilterableItem<T> {
    private T item;
    private Selector<T> selector;

    public FilterableItem(T item, Selector<T> selector) {
        this.item = item;
        this.selector = selector;
    }

    /*
     * Возвращает текстовое представление объекта
     */
    public String getCaption() {
        return selector.getCaption(item);
    }
    /*
     * Возвращает идентификатор объекта
     */

    public Object getValue() {
        return selector.getValue(item);
    }
    /*
     * Возвращает тип объекта для группировки
     */

    public String getType() {
        return selector.getType(item);
    }

    T getItem() {
        return item;
    }
    
}
