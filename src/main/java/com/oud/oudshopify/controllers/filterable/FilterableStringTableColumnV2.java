package com.oud.oudshopify.controllers.filterable;

public class FilterableStringTableColumnV2<S, T> extends AbstractFilterableTableColumnV2<S, T, StringOperatorV2, TextFilterEditorV2> {

    public FilterableStringTableColumnV2(String text) {
        super(text, new TextFilterEditorV2(text));
    }
}
