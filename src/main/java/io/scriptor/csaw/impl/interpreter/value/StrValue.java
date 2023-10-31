package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_STR;

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
        return TYPE_STR;
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
