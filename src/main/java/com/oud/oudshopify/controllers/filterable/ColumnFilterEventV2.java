package com.oud.oudshopify.controllers.filterable;

import java.util.List;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TableView;

/**
 * An event that is fired when an {@link AbstractFilterableTableColumnV2} has its filter changed
 *
 * @author JHS
 */
public class ColumnFilterEventV2<S, T, R extends IFilterOperatorV2<?>, M extends IFilterEditorV2<R>>
        extends Event {
    /**
     * An event indicating that the filter has changed
     */
    public static final EventType<ColumnFilterEventV2<?, ?, ?, ?>> FILTER_CHANGED_EVENT = new EventType<>(Event.ANY, "FILTER_CHANGED");

    private List<R> filter;

    private AbstractFilterableTableColumnV2<S, T, R, M> sourceColumn;


    public ColumnFilterEventV2(TableView<S> table, AbstractFilterableTableColumnV2<S, T, R, M> sourceColumn, List<R> filter) {
        super(table, Event.NULL_SOURCE_TARGET, ColumnFilterEventV2.FILTER_CHANGED_EVENT);

        if (table == null) {
            throw new NullPointerException("TableView can not be null");
        }

        this.filter = filter;
        this.sourceColumn = sourceColumn;
    }

    /**
     * @return Any and all filters applied to the column
     */
    public List<R> getFilters() {
        return filter;
    }

    /**
     * @return The {@link AbstractFilterableTableColumnV2} which had its filter changed
     */
    public AbstractFilterableTableColumnV2<S, T, R, M> sourceColumn() {
        return sourceColumn;
    }
}