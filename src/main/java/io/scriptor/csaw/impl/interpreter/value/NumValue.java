package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_NUM;

public class NumValue extends Value {

    private final double mValue;

    public NumValue() {
        mValue = 0;
    }

    public NumValue(double value) {
        mValue = value;
    }

    public NumValue(boolean value) {
        mValue = value ? 1 : 0;
    }

    public int getInt() {
        return (int) mValue;
    }

    public long getLong() {
        return (long) mValue;
    }

    public double get() {
        return mValue;
    }

    @Override
    protected Number value() {
        if (mValue == (long) mValue)
            return (long) mValue;
        return mValue;
    }

    @Override
    protected String type() {
        return TYPE_NUM;
    }

    @Override
    protected boolean bool() {
        return mValue != 0;
    }

    @Override
    protected String string() {
        return mValue == Math.floor(mValue) ? Long.toString((long) mValue) : Double.toString(mValue);
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

}
