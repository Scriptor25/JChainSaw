package io.scriptor.csaw.impl.interpreter.value;

import io.scriptor.csaw.impl.Type;

public class ConstNum extends Value {

    private final double mValue;

    public ConstNum() {
        mValue = 0;
    }

    public ConstNum(double value) {
        mValue = value;
    }

    public ConstNum(boolean value) {
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
    protected Type type() {
        return Type.getNum();
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
