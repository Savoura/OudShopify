package com.oud.oudshopify.controllers.filterable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class FilterMenuPopupV2 extends PopupControl {
    /**
     * Use context-menu's CSS settings as a base, and override with our filter-popup-menu settings
     */
    private static final String[] DEFAULT_STYLE_CLASS = { "filter-popup-menu", "context-menu" };

    private static FilterMenuPopupV2 currentlyVisibleMenu;

    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Button> saveButton;
    private final SimpleObjectProperty<Button> resetButton;
    private final SimpleObjectProperty<Button> cancelButton;
    private final SimpleStringProperty title;

    /**
     * Popup constructor
     */
    public FilterMenuPopupV2(String title)
    {
        setHideOnEscape(true);
        setAutoHide(true);

        // Listen for ESC key events; hide/cancel if one's caught
        final EventHandler<KeyEvent> cancelEvent = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    hide();
                }
            }
        };

        this.title = new SimpleStringProperty(title);

        final Button sButton = new Button("Save");
        saveButton = new SimpleObjectProperty<>(sButton);
        sButton.getStyleClass().add("save-button");
        sButton.addEventFilter(KeyEvent.KEY_PRESSED, cancelEvent);
        sButton.setDefaultButton(true);

        final Button rButton = new Button("Reset");
        resetButton = new SimpleObjectProperty<>(rButton);
        rButton.getStyleClass().add("reset-button");
        rButton.addEventFilter(KeyEvent.KEY_PRESSED, cancelEvent);

        final Button cButton = new Button("Cancel");
        cancelButton = new SimpleObjectProperty<>(cButton);
        cButton.getStyleClass().add("cancel-button");
        cButton.addEventFilter(KeyEvent.KEY_PRESSED, cancelEvent);
        cButton.setCancelButton(true);
        cButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hide();
            }
        });

        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        setSkin(new FilterMenuPopupSkin2());
    }

    public ObjectProperty<Node> contentNodeProperty()
    {
        return contentNode;
    }

    /**
     * Set the content to display in the filter menu
     * @param value
     */
    public final void setContentNode(Node value)
    {
        contentNodeProperty().set(value);
    }

    public final Node getContentNode()
    {
        return contentNode.get();
    }

    public SimpleStringProperty titleProperty()
    {
        return title;
    }

    public String getTitle()
    {
        return title.get();
    }

    public void setTitle(String title)
    {
        this.title.set(title);
    }

    public SimpleObjectProperty<Button> saveButtonProperty()
    {
        return saveButton;
    }

    public Button getSaveButton()
    {
        return saveButton.get();
    }

    public SimpleObjectProperty<Button> cancelButtonProperty()
    {
        return saveButton;
    }

    public Button getCancelButton()
    {
        return cancelButton.get();
    }

    public SimpleObjectProperty<Button> resetButtonProperty()
    {
        return saveButton;
    }

    public Button getResetButton()
    {
        return resetButton.get();
    }

    /**
     * Set the event to fire when the save button is pressed
     * @param event
     */
    public void setSaveEvent(EventHandler<ActionEvent> event)
    {
        saveButton.get().setOnAction(event);
    }

    /**
     * Set the event to fire when the reset button is pressed
     * @param event
     */
    public void setResetEvent(EventHandler<ActionEvent> event)
    {
        resetButton.get().setOnAction(event);
    }

    @Override
    protected void show()
    {
        highlander();
        super.show();
    }

    @Override
    public void show(Window window)
    {
        highlander();
        super.show(window);
    }

    @Override
    public void show(Window window, double d, double d1)
    {
        highlander();
        super.show(window, d, d1);
    }

    @Override
    public void show(Node node, double d, double d1)
    {
        highlander();
        super.show(node, d, d1);
    }

    @Override
    public void hide()
    {
        if (currentlyVisibleMenu == this)
            currentlyVisibleMenu = null;
        super.hide();
    }

    /**
     * There can be only one... visible FilterMenuPopup
     */
    private void highlander()
    {
        if (currentlyVisibleMenu != null && currentlyVisibleMenu != this)
        {
            currentlyVisibleMenu.hide();
        }
        currentlyVisibleMenu = this;
    }

    // XXX: I'm not sure how to set the skin properly for PopupControl in JavaFX 8; it somehow changed.
    // Just making the skin a private class so I can use setSkin() is easier than trying to figure it out
    // (I think you have to call -fx-skin on the CSSBridge somehow)
    private class FilterMenuPopupSkin2 extends StackPane implements Skin<FilterMenuPopupV2>
    {
        public FilterMenuPopupSkin2()
        {
            final ContentStack contentStack = new ContentStack();
            getChildren().add(contentStack);

            idProperty().bind( FilterMenuPopupV2.this.idProperty() );
            styleProperty().bind( FilterMenuPopupV2.this.styleProperty() );
            getStyleClass().setAll( FilterMenuPopupV2.this.getStyleClass() );
        }

        @Override
        public FilterMenuPopupV2 getSkinnable()
        {
            return FilterMenuPopupV2.this;
        }

        @Override
        public Node getNode()
        {
            return this;
        }

        @Override
        public void dispose()
        {
            getChildren().clear();
        }

        class ContentStack extends BorderPane
        {
            public ContentStack()
            {
                getStyleClass().add("content");

                final Label titleLabel = new Label();
                titleLabel.textProperty().bind(FilterMenuPopupV2.this.titleProperty());

                final StackPane topPane = new StackPane();
                topPane.getChildren().addAll(new Separator(), titleLabel);
                topPane.getStyleClass().add("top");
                setTop(topPane);

                FilterMenuPopupV2.this.contentNodeProperty().addListener(new ChangeListener<Node>() {
                    @Override
                    public void changed(ObservableValue<? extends Node> paramObservableValue,Node paramT1, Node paramT2) {
                        paramT2.getStyleClass().add("center");
                        setCenter(paramT2);
                    }
                });

                if (FilterMenuPopupV2.this.getContentNode() != null)
                    FilterMenuPopupV2.this.getContentNode().getStyleClass().add("center");
                setCenter(FilterMenuPopupV2.this.getContentNode());

                final HBox buttons = new HBox();
                buttons.getStyleClass().add("buttons");
                buttons.setPrefWidth(USE_COMPUTED_SIZE);
                buttons.setPrefHeight(USE_COMPUTED_SIZE);
                buttons.setSpacing(4);
                buttons.getChildren().addAll(FilterMenuPopupV2.this.getSaveButton(), FilterMenuPopupV2.this.getResetButton(), FilterMenuPopupV2.this.getCancelButton());

                final VBox bottom = new VBox();
                bottom.getStyleClass().add("bottom");
                bottom.getChildren().addAll(new Separator(), buttons);
                setBottom(bottom);
            }
        }
    }
}
