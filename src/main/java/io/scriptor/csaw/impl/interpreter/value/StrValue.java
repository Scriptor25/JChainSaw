package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_STR;

public class StrValue extends Value {

    private final String mValue;

    public StrValue() {
        mValue = "";
    }

    public StrValue(String value) {
        mValue = value;
    }

    public String get() {
        return mValue;
    }

    @Override
    protected String type() {
        return TYPE_STR;
    }

    @Override
    protected Object object() {
        return mValue;
    }

    @Override
    protected String string() {
        return mValue;
    }

}
