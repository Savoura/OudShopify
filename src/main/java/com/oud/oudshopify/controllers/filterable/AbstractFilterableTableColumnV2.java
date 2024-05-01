package com.oud.oudshopify.controllers.filterable;

import com.oud.oudshopify.controllers.filterable.IFilterEditorV2;
import com.oud.oudshopify.controllers.filterable.IFilterableTableColumnV2;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;


public class AbstractFilterableTableColumnV2<S, T, R extends IFilterOperatorV2<?>, M extends IFilterEditorV2<R>>
        extends TableColumn<S, T>
        implements IFilterableTableColumnV2<R, M> {

    private final M filterEditor;
    private final ObservableList<R> filterResults;


    public AbstractFilterableTableColumnV2(String name, final M filterEditor) {
        super(name);

        this.filterEditor = filterEditor;
        this.filterResults = FXCollections.observableArrayList();

        // Keep the popup menu's title sync'd with the column title
        filterEditor.getFilterMenu().titleProperty()
                .bind(AbstractFilterableTableColumnV2.this.textProperty());

        final FilterMenuButtonV2 filterMnuButton = new FilterMenuButtonV2(filterEditor.getFilterMenu());
        filterMnuButton.activeProperty().bind(filterEditor.filteredProperty());
        // Display a button on the column to show the menu
        setGraphic(filterMnuButton);

        filterEditor.getFilterMenu()
                .setResetEvent(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        try {
                            if (filterEditor.clear()) {
                                filterResults.clear();

                                final ColumnFilterEventV2<S, T, R, M> e = new ColumnFilterEventV2<>(
                                        AbstractFilterableTableColumnV2.this.getTableView()
                                        , AbstractFilterableTableColumnV2.this
                                        , getFilters());

                                Event.fireEvent(AbstractFilterableTableColumnV2.this, e);
                            }
                            filterEditor.getFilterMenu().hide();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });

        filterEditor.getFilterMenu()
                .setSaveEvent(t -> {
                    try {
                        if (filterEditor.save()) {
                            filterResults.setAll(filterEditor.getFilters());

                            final ColumnFilterEventV2<S, T, R, M> e = new ColumnFilterEventV2<>(
                                    AbstractFilterableTableColumnV2.this.getTableView()
                                    , AbstractFilterableTableColumnV2.this
                                    , getFilters());

                            Event.fireEvent(AbstractFilterableTableColumnV2.this, e);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    filterEditor.getFilterMenu().hide();

                });
    }

    protected M getFilterEditor() {
        return filterEditor;
    }

    @Override
    public ObservableList<R> getFilters() {
        return filterResults;
    }

    @Override
    public final BooleanProperty filteredProperty() {
        return filterEditor.filteredProperty();
    }

    @Override
    public boolean isFiltered() {
        return filterEditor.isFiltered();
    }

}