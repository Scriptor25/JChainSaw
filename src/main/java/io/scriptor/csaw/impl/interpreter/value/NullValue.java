package io.scriptor.csaw.impl.interpreter.value;

public class NullValue extends Value {

    private final String mType;

    public NullValue(String type) {
        mType = type;
    }

    @Override
    protected Object value() {
        return null;
    }

    @Override
    protected String type() {
        return mType;
    }

    @Override
    protected boolean bool() {
        return false;
    }

    @Override
    protected String string() {
        return String.format("null [%s]", mType);
    }

}
