package com.oud.oudshopify.controllers.filterable;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

/**
 * A button that controls displaying the filter menu when clicked
 *
 * @author jhs
 */
public class FilterMenuButtonV2
        extends Button {

    private final SimpleBooleanProperty active = new SimpleBooleanProperty();
    private final FontIcon ON_ICON = new FontIcon(Material2AL.FILTER_ALT);
    private final FontIcon OFF_ICON = new FontIcon(Material2OutlinedAL.FILTER_ALT);

    public FilterMenuButtonV2(final FilterMenuPopupV2 popup) {
        setGraphic(OFF_ICON);

        // When the active property is true, append an active class to this button
        active.addListener((ov, oldVal, newVal) -> {
            if (newVal == Boolean.TRUE) {
                setGraphic(ON_ICON);
            } else {
                setGraphic(OFF_ICON);
            }
        });

        // Toggle popup display when clicked
        setOnAction(event -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                final Control c = (Control) event.getSource();
                final Bounds b = c.localToScene(c.getLayoutBounds());
                final PopupControl menu = popup;

                final Scene scene = c.getScene();
                final Window window = scene.getWindow();
                menu.show(c, window.getX() + scene.getX() + b.getMinX(), window.getY() + scene.getY() + b.getMaxY());
            }
        });

    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean b) {
        active.set(b);
    }

    public boolean isActive() {
        return active.get();
    }
}