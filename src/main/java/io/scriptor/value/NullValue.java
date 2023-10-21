package io.scriptor.value;

public class NullValue extends Value {

    private final String mType;

    public NullValue(String type) {
        mType = type;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("null [%s]", mType);
    }

}
