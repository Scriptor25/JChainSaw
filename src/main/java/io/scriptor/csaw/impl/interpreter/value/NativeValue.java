package io.scriptor.csaw.impl.interpreter.value;

public class NativeValue extends Value {

    private final Object mValue;

    public NativeValue(Object value) {
        mValue = value;
    }

    @Override
    public Object getValue() {
        return mValue;
    }

    @Override
    public String getType() {
        return mValue.getClass().getName();
    }

    @Override
    public boolean asBoolean() {
        return mValue != null;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }

}
