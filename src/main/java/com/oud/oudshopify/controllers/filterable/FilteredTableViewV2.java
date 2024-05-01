package com.oud.oudshopify.controllers.filterable;

;

import com.oud.oudshopify.controllers.filterable.AbstractFilterableTableColumnV2;
import com.oud.oudshopify.controllers.filterable.ColumnFilterEventV2;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class FilteredTableViewV2<S> extends TableView<S> {

    /**
     * List of filterable columns with a filter applied
     */
    private ObservableList<AbstractFilterableTableColumnV2<?, ?, ?, ?>> filteredColumns;


    public FilteredTableViewV2(ObservableList<S> ol) {
        this();
        super.setItems(ol);
    }

    public FilteredTableViewV2() {
        super();

        filteredColumns = FXCollections.observableArrayList();

        // Execute the filteringChanged runnable
        // And, if a column has a filter on it, make sure that column is in our filteredColumns list
        final EventHandler<ColumnFilterEventV2<?, ?, ?, ?>> columnFilteredEventHandler = new EventHandler<ColumnFilterEventV2<?, ?, ?, ?>>() {
            @Override
            public void handle(ColumnFilterEventV2<?, ?, ?, ?> event) {
                // Keep track of which TableColumn's are currently filtered
                final AbstractFilterableTableColumnV2<?, ?, ?, ?> col = event.sourceColumn();

                if (col.isFiltered() == true && filteredColumns.contains(col) == false) {
                    filteredColumns.add(col);
                } else if (col.isFiltered() == false && filteredColumns.contains(col) == true) {
                    filteredColumns.remove(col);
                }

                // Forward event
                fireEvent(event);
            }
        };

        // Make sure any filterable columns on this table have the ColumnFilterEventV2Handler
        getColumns().addListener(new ListChangeListener<TableColumn<?, ?>>() {
            @Override
            public void onChanged(Change<? extends TableColumn<?, ?>> change) {
                change.next();// must advance to next change, for whatever reason...
                // Drag-n-dropping a column fires a remove and an add.
                if (change.wasRemoved()) {
                    for (final TableColumn<?, ?> col : change.getAddedSubList()) {
                        if (col instanceof AbstractFilterableTableColumnV2) {
                            final AbstractFilterableTableColumnV2<?, ?, ?, ?> fcol = (AbstractFilterableTableColumnV2<?, ?, ?, ?>) col;
                            fcol.removeEventHandler(ColumnFilterEventV2.FILTER_CHANGED_EVENT, columnFilteredEventHandler);
                        }
                    }
                }
                if (change.wasAdded()) {
                    for (final TableColumn<?, ?> col : change.getAddedSubList()) {
                        if (col instanceof AbstractFilterableTableColumnV2) {
                            final AbstractFilterableTableColumnV2<?, ?, ?, ?> fcol = (AbstractFilterableTableColumnV2<?, ?, ?, ?>) col;
                            fcol.addEventHandler(ColumnFilterEventV2.FILTER_CHANGED_EVENT, columnFilteredEventHandler);
                        }
                    }
                }
            }
        });
    }

    /**
     * @return Observable list containing any {@link AbstractFilterableTableColumnV2}'s that have a filter applied
     */
    public ObservableList<AbstractFilterableTableColumnV2<?, ?, ?, ?>> getFilteredColumns() {
        return filteredColumns;
    }

}