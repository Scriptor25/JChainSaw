package io.scriptor.csaw.impl.interpreter.value;

import io.scriptor.csaw.impl.interpreter.Type;

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
    protected Type type() {
        return Type.getStr();
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
