package io.scriptor.value;

public class StrValue extends Value {

    private String mValue;

    public StrValue(String value) {
        mValue = value;
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
