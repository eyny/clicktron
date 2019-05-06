package com.clicktron;

import java.util.regex.Pattern;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;

public class NumberField extends TextField
{
    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // New property named minNumber which is the minimum number can be entered
    private SimpleIntegerProperty minNumber =
            new SimpleIntegerProperty(NumberField.this, "minNumber", Integer.MIN_VALUE);
    public SimpleIntegerProperty minNumberProperty() { return minNumber; }
    public int getMinNumber() { return minNumber.get(); }
    public void setMinNumber(int number) { minNumber.set(number); }

    // New property named maxNumber which is the maximum number can be entered
    private SimpleIntegerProperty maxNumber =
            new SimpleIntegerProperty(NumberField.this, "maxNumber", Integer.MAX_VALUE);
    public SimpleIntegerProperty maxValueProperty() { return maxNumber; }
    public int getMaxNumber() { return maxNumber.get(); }
    public void setMaxNumber(int number) { maxNumber.set(number); }

    // New property named canBeEmpty which sets whether the number field can be left blank by user or not
    private SimpleBooleanProperty canBeEmpty =
            new SimpleBooleanProperty(NumberField.this,"canBeEmpty", false);
    public SimpleBooleanProperty canBeEmptyProperty() { return canBeEmpty; }
    public boolean getCanBeEmpty() { return canBeEmpty.get(); }
    public void setCanBeEmpty(boolean bool) { canBeEmpty.set(bool); }

    /***************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/

    // Compile the regex pattern only once
    private static Pattern numberPattern = Pattern.compile("[0-9]*");
    // Override replaceTest so only numbers are taken as input
    @Override
    public void replaceText(int start, int end, String text)
    {
        if (numberPattern.matcher(text).matches())
        {
            super.replaceText(start, end, text);

            //If empty but cannot be empty
            if (!getCanBeEmpty() && getText().isEmpty()) {
                setNumber(getMinNumber());
            }
            //If not empty but value is smaller than min limit
            else if (!getText().isEmpty() && getNumber() < getMinNumber()) {
                setNumber(getMinNumber());
            }
            //If value is bigger than max limit
            else if (getMaxNumber() != 0 && getNumber() > getMaxNumber()) {
                setNumber(getMaxNumber());
            }
        }
    }

    // Get and set number and returns -1 if number field is empty
    public int getNumber() {
        return super.getText().equals("") ? -1 : Integer.parseInt(super.getText());
    }

    public void setNumber(int number) {
        super.setText(number == -1 ? "" : Integer.toString(number));
    }
}
