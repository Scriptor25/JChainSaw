package io.scriptor.csaw.impl.value;

public class NumValue extends Value {

    private double mValue;

    public NumValue() {
        mValue = 0;
    }

    public NumValue(double value) {
        mValue = value;
    }

    public NumValue(boolean value) {
        mValue = value ? 1 : 0;
    }

    public Double getValue() {
        return mValue;
    }

    @Override
    public String getType() {
        return Value.TYPE_NUM;
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
        return mValue == Math.floor(mValue) ? Long.toString((long) mValue) : Double.toString(mValue);
    }

}
