package com.oud.oudshopify.controllers.filterable;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;

/**
 * @author jhsheets@gmail.com
 */
public interface IFilterableTableColumnV2<R extends IFilterOperatorV2<?>, M extends IFilterEditorV2<R>> {
    ObservableList<R> getFilters();

    /**
     * @return Property indicating if this column has filters applied
     */
    BooleanProperty filteredProperty();

    /**
     * @return If this column has filters applied
     */
    boolean isFiltered();
}