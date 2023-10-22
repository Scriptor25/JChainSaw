package io.scriptor.csaw.impl.value;

public class StrValue extends Value {

    private String mValue;

    public StrValue() {
        mValue = "";
    }

    public StrValue(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public String getType() {
        return Value.TYPE_STR;
    }

    @Override
    public boolean asBoolean() {
        return mValue != null && !mValue.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof StrValue))
            return false;
        return mValue.equals(((StrValue) other).mValue);
    }

    @Override
    public String toString() {
        return mValue;
    }
}
