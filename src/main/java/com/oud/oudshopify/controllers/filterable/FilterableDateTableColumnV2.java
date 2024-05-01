package com.oud.oudshopify.controllers.filterable;

public class FilterableDateTableColumnV2<S, T> extends AbstractFilterableTableColumnV2<S, T, DateOperatorV2, DateFilterEditorV2> {
    public FilterableDateTableColumnV2() {
        this("");
    }

    public FilterableDateTableColumnV2(String text) {
        super(text, new DateFilterEditorV2(text));
    }

    public FilterableDateTableColumnV2(String text, String dateFormat) {
        super(text, new DateFilterEditorV2(text, dateFormat));
    }
}
