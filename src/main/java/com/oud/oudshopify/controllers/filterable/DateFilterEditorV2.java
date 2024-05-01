/*
 * Copyright (c) 2013, jhsheets@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.oud.oudshopify.controllers.filterable;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


/**
 * @author JHS
 */
public class DateFilterEditorV2
        extends AbstractFilterEditorV2<DateOperatorV2> {
    private final Picker picker1;
    private final Picker picker2;


    public DateFilterEditorV2(String title) {
        this(title, "yyyy-MM-dd HH:mm", DateOperatorV2.VALID_TYPES);
    }

    public DateFilterEditorV2(String title, String dateFormat) {
        this(title, dateFormat, DateOperatorV2.VALID_TYPES);
    }

    public DateFilterEditorV2(String title, DateOperatorV2.Type[] types) {
        this(title, "yyyy-MM-dd HH:mm", types);
    }

    public DateFilterEditorV2(String title, String dateFormat, EnumSet<DateOperatorV2.Type> types) {
        this(title, dateFormat, types.toArray(new DateOperatorV2.Type[0]));
    }

    public DateFilterEditorV2(String title, String dateFormat, DateOperatorV2.Type[] types) {
        super(title);

        final List<DateOperatorV2.Type> set1 = new ArrayList<>(20);
        final List<DateOperatorV2.Type> set2 = new ArrayList<>(20);
        parseTypes(types, set1, set2);

        picker1 = new Picker(dateFormat, set1.toArray(new DateOperatorV2.Type[0]));
        picker2 = new Picker(dateFormat, set2.toArray(new DateOperatorV2.Type[0]));

        final VBox box = new VBox();
        box.getChildren().addAll(picker1.box, picker2.box);
        setFilterMenuContent(box);

        // Disable the 2nd picker if the 1st picker isn't of a range
        picker2.setEnabled(false);
        picker1.typeBox.getSelectionModel().selectedItemProperty()
                .addListener((ov, old, newVal) -> picker2.setEnabled(newVal.equals(DateOperatorV2.Type.BEFORE) || newVal.equals(DateOperatorV2.Type.BEFOREON)
                        || newVal.equals(DateOperatorV2.Type.AFTER) || newVal.equals(DateOperatorV2.Type.AFTERON)));
    }

    private void parseTypes(DateOperatorV2.Type[] types, List<DateOperatorV2.Type> set1, List<DateOperatorV2.Type> set2) {
        set1.add(DateOperatorV2.Type.NONE);
        set2.add(DateOperatorV2.Type.NONE);
        for (DateOperatorV2.Type type : types) {
            // Only these range types should show up in 2nd picker
            if (type.equals(DateOperatorV2.Type.BEFORE) || type.equals(DateOperatorV2.Type.BEFOREON)
                    || type.equals(DateOperatorV2.Type.AFTER) || type.equals(DateOperatorV2.Type.AFTERON)) {
                if (!set2.contains(type)) set2.add(type);
            }

            if (!set1.contains(type)) set1.add(type);
        }
    }

    @Override
    public DateOperatorV2[] getFilters() throws Exception {

        final DateOperatorV2 val1 = picker1.getFilter();
        final DateOperatorV2 val2 = picker2.getFilter();
        final LocalDate d1 = val1.getValue();
        final LocalDate d2 = val2.getValue();

        // Bounds check the dates
        if (d1 != null && d2 != null && (d1.isAfter(d2) || d1.equals(d2))
                && ((DateOperatorV2.Type.AFTER == val1.getType() && DateOperatorV2.Type.BEFORE == val2.getType())
                || (DateOperatorV2.Type.AFTER == val1.getType() && DateOperatorV2.Type.BEFOREON == val2.getType())
                || (DateOperatorV2.Type.AFTERON == val1.getType() && DateOperatorV2.Type.BEFORE == val2.getType())
                || (DateOperatorV2.Type.AFTERON == val1.getType() && DateOperatorV2.Type.BEFOREON == val2.getType())))
            throw new Exception("Second date cannot be before or the same as the first date");

        return new DateOperatorV2[]{val1, val2};
    }

    @Override
    public void cancel() {
        picker1.cancel();
        picker2.cancel();
    }

    @Override
    public boolean save() throws Exception {
        boolean changed = false;

        final DateOperatorV2 do1 = picker1.getFilter();
        final DateOperatorV2 do2 = picker2.getFilter();

        if (do1.getType() == picker1.DEFAULT_TYPE && do2.getType() == picker2.DEFAULT_TYPE) {
            changed = clear();
        } else {
            final boolean changed1 = picker1.save();
            final boolean changed2 = picker2.save();
            setFiltered(true);
            changed = changed1 || changed2;
        }

        return changed;
    }

    @Override
    public boolean clear() throws Exception {
        boolean changed = false;

        picker1.clear();
        picker2.clear();

        if (isFiltered()) {
            setFiltered(false);
            changed = true;
        }

        return changed;
    }


    /**
     * Separate code out so we can reuse it for multiple Date picker groups
     */
    private class Picker {
        private final LocalDate DEFAULT_DATE = null;
        private final DateOperatorV2.Type DEFAULT_TYPE = DateOperatorV2.Type.NONE;

        private LocalDate previousDate = DEFAULT_DATE;
        private DateOperatorV2.Type previousType = DEFAULT_TYPE;

        private final GridPane box = new GridPane();
        private final DatePicker datePicker;
        private final ComboBox<DateOperatorV2.Type> typeBox;

        private Picker(String dateFormat, DateOperatorV2.Type[] choices) {
            datePicker = new DatePicker();
            datePicker.setValue(DEFAULT_DATE);

            typeBox = new ComboBox<>();
            typeBox.setMaxWidth(Double.MAX_VALUE);
            typeBox.getSelectionModel().selectedItemProperty()
                    .addListener(new ChangeListener<DateOperatorV2.Type>() {
                        @Override
                        public void changed(ObservableValue<? extends DateOperatorV2.Type> ov, DateOperatorV2.Type old, DateOperatorV2.Type newVal) {
                            datePicker.setDisable(newVal == DateOperatorV2.Type.NONE);
                        }
                    });
            typeBox.getSelectionModel().select(DEFAULT_TYPE);
            typeBox.getItems().addAll(choices);

            GridPane.setRowIndex(typeBox, 0);
            GridPane.setColumnIndex(typeBox, 0);
            GridPane.setMargin(typeBox, new Insets(4, 0, 0, 0));
            GridPane.setRowIndex(datePicker, 1);
            GridPane.setColumnIndex(datePicker, 0);
            GridPane.setMargin(datePicker, new Insets(4, 0, 0, 0));
            final ColumnConstraints boxConstraint = new ColumnConstraints();
            boxConstraint.setPercentWidth(100);
            box.getColumnConstraints().addAll(boxConstraint);
            box.getChildren().addAll(typeBox, datePicker);

            setFilterMenuContent(box);
        }

        public void setEnabled(boolean enable) {
            typeBox.setDisable(!enable);
            datePicker.setDisable(!enable || typeBox.getSelectionModel()
                    .getSelectedItem() == DateOperatorV2.Type.NONE);
        }

        public void cancel() {
            datePicker.setValue(previousDate);
            typeBox.getSelectionModel().select(previousType);
        }

        public void clear() {
            previousDate = DEFAULT_DATE;
            previousType = DEFAULT_TYPE;

            datePicker.setValue(DEFAULT_DATE);
            typeBox.getSelectionModel().select(DEFAULT_TYPE);
        }

        public boolean save() {
            final boolean changed = previousType != typeBox.getSelectionModel()
                    .getSelectedItem()
                    || (typeBox.getSelectionModel()
                    .getSelectedItem() != DateOperatorV2.Type.NONE
                    && previousDate.equals(datePicker.getValue()) == false);

            previousDate = datePicker.getValue();
            previousType = typeBox.getSelectionModel().getSelectedItem();
            return changed;
        }

        public DateOperatorV2 getFilter() throws Exception {
            final LocalDate date = datePicker.getValue();
            final DateOperatorV2.Type selectedType = typeBox.getSelectionModel()
                    .getSelectedItem();
            if (typeBox.isDisable() || selectedType == DateOperatorV2.Type.NONE) {
                return new DateOperatorV2(DateOperatorV2.Type.NONE, DEFAULT_DATE);
            } else {
                if (date == null && selectedType != IFilterOperatorV2.Type.SHOW_EMPTY) {
                    throw new Exception("Filter text cannot be empty");
                } else {
                    return new DateOperatorV2(selectedType, date);
                }
            }
        }
    }

    ;
}