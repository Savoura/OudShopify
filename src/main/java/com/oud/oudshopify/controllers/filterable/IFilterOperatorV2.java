package com.oud.oudshopify.controllers.filterable;

/**
 * @author JHS
 */
public interface IFilterOperatorV2<T> {
    /**
     * Probably should turn this into a normal class, so I can create true subsets of these type in IFilterOperator subclasses
     */
    public static enum Type {
        NONE("No Filter"), NOTSET("Not Set"), EQUALS("Equals"),
        NOTEQUALS("Not Equals"), GREATERTHAN("Greater Than"), GREATERTHANEQUALS("Equals/Greater Than"),
        LESSTHAN("Less Than"), LESSTHANEQUALS("Equals/Less Than"), CONTAINS("Contains"),
        STARTSWITH("Starts With"), ENDSWITH("Ends With"),
        BEFORE("Before"), BEFOREON("Before Or On"), AFTER("After"), SHOW_EMPTY("Show Empty"),
        AFTERON("After Or On"), TRUE("True"), FALSE("False");

        private final String display;

        Type(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    ;

    public T getValue();

    public Type getType();
}