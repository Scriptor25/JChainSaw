package io.scriptor.value;

public class NumValue extends Value {

    private double mValue;

    public NumValue(double value) {
        mValue = value;
    }

    public NumValue(boolean value) {
        mValue = value ? 1 : 0;
    }

    public double getValue() {
        return mValue;
    }

    @Override
    public boolean asBoolean() {
        return mValue != 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof NumValue))
            return false;
        return mValue == ((NumValue) other).mValue;
    }

    @Override
    public String toString() {
        return Double.toString(mValue);
    }

}
