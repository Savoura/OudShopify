package com.oud.oudshopify.controllers.filterable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.WindowEvent;

public abstract class AbstractFilterEditorV2<R extends IFilterOperatorV2<?>> implements IFilterEditorV2<R> {
    private FilterMenuPopupV2 menu;
    private SimpleBooleanProperty filtered;

    public AbstractFilterEditorV2(String title) {
        menu = new FilterMenuPopupV2(title);
        filtered = new SimpleBooleanProperty(false);

        menu.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                AbstractFilterEditorV2.this.cancel();
            }
        });
    }

    @Override
    public FilterMenuPopupV2 getFilterMenu() {
        return menu;
    }

    /**
     * Sets the content to display in the filter menu
     *
     * @param node
     */
    public void setFilterMenuContent(Node node) {
        menu.setContentNode(node);
    }

    @Override
    public BooleanProperty filteredProperty() {
        return filtered;
    }

    @Override
    public boolean isFiltered() {
        return filtered.get();
    }

    /**
     * @param isFiltered If there are any non-default filters applied
     */
    protected void setFiltered(boolean isFiltered) {
        filtered.set(isFiltered);
    }

}
