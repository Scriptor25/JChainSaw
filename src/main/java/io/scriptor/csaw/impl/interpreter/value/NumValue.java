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

    public boolean getBool() {
        return mValue != 0;
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
    protected String type() {
        return TYPE_NUM;
    }

    @Override
    protected Object object() {
        if (mValue == getLong())
            return getLong();
        return mValue;
    }

    @Override
    protected String string() {
        return object().toString();
    }

}
