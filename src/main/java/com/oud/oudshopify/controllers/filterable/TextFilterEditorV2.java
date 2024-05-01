package com.oud.oudshopify.controllers.filterable;

import java.util.ArrayList;
import java.util.EnumSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;


/**
 *
 * @author JHS
 */
public class TextFilterEditorV2
        extends AbstractFilterEditorV2<StringOperatorV2>
{
    private String previousText;
    private StringOperatorV2.Type previousType;

    private final TextField textField;
    private final ComboBox<StringOperatorV2.Type> typeBox;

    private final String DEFAULT_TEXT;
    private final StringOperatorV2.Type DEFAULT_TYPE;

    public TextFilterEditorV2(String title)
    {
        this(title, StringOperatorV2.VALID_TYPES);
    }

    public TextFilterEditorV2(String title, EnumSet<StringOperatorV2.Type> types)
    {
        this(title, types.toArray(new StringOperatorV2.Type[0]));
    }

    public TextFilterEditorV2(String title, StringOperatorV2.Type[] types)
    {
        super(title);

        DEFAULT_TEXT = "";
        DEFAULT_TYPE = StringOperatorV2.Type.NONE;

        textField = new TextField();
        typeBox = new ComboBox<>();

        final GridPane box = new GridPane();
        GridPane.setRowIndex(typeBox, 0);
        GridPane.setColumnIndex(typeBox, 0);
        GridPane.setRowIndex(textField, 1);
        GridPane.setColumnIndex(textField, 0);
        GridPane.setMargin(typeBox, new Insets(4, 0, 0, 0));
        GridPane.setMargin(textField, new Insets(4, 0, 0, 0));
        final ColumnConstraints boxConstraint = new ColumnConstraints();
        boxConstraint.setPercentWidth(100);
        box.getColumnConstraints().addAll(boxConstraint);
        box.getChildren().addAll(typeBox, textField);

        setFilterMenuContent(box);

        previousText = DEFAULT_TEXT;
        previousType = DEFAULT_TYPE;

        typeBox.getSelectionModel().select(DEFAULT_TYPE);
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.getItems().addAll(types);
        typeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StringOperatorV2.Type>() {
            @Override
            public void changed(ObservableValue<? extends StringOperatorV2.Type> ov, StringOperatorV2.Type old, StringOperatorV2.Type newVal) {
                textField.setDisable(newVal == StringOperatorV2.Type.NONE);
            }
        });

        textField.setDisable(true);
    }

    @Override
    public StringOperatorV2[] getFilters() throws Exception
    {
        final ArrayList<StringOperatorV2> retList = new ArrayList<>();

        final String text = textField.getText();
        final StringOperatorV2.Type selectedType = typeBox.getSelectionModel().getSelectedItem();
        if (selectedType == StringOperatorV2.Type.NONE)
        {
            retList.add( new StringOperatorV2(selectedType, "") );
        }
        else
        {
            if (text.isEmpty()) {
                throw new Exception("Filter text cannot be empty");
            } else {
                retList.add(new StringOperatorV2(selectedType, text));
            }
        }
        return retList.toArray(new StringOperatorV2[0]);
    }

    @Override
    public void cancel()
    {
        textField.setText(previousText);
        typeBox.getSelectionModel().select(previousType);
    }

    @Override
    public boolean save() throws Exception
    {
        boolean changed = false;

        final StringOperatorV2.Type selectedType = typeBox.getSelectionModel().getSelectedItem();
        if (selectedType == DEFAULT_TYPE)
        {
            changed = clear();
        }
        else
        {
            changed = previousType != typeBox.getSelectionModel().getSelectedItem()
                    || (typeBox.getSelectionModel().getSelectedItem() != StringOperatorV2.Type.NONE
                    && previousText.equals(textField.getText()) == false);

            previousText = textField.getText();
            previousType = typeBox.getSelectionModel().getSelectedItem();
            setFiltered(true);
            //changed = true;
        }

        return changed;
    }

    @Override
    public boolean clear() throws Exception
    {
        boolean changed = false;

        previousText = DEFAULT_TEXT;
        previousType = DEFAULT_TYPE;

        textField.setText(DEFAULT_TEXT);
        typeBox.getSelectionModel().select(DEFAULT_TYPE);

        if (isFiltered())
        {
            setFiltered(false);
            changed = true;
        }

        return changed;
    }

}