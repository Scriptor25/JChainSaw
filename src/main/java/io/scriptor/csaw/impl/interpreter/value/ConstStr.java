package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_STR;

public class ConstStr extends Value {

    private final String mValue;

    public ConstStr() {
        mValue = "";
    }

    public ConstStr(String value) {
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
