package io.scriptor.csaw.impl.interpreter.value;

import io.scriptor.csaw.impl.Type;

public class ConstChr extends Value {

    private final char mValue;

    public ConstChr() {
        mValue = '\0';
    }

    public ConstChr(char value) {
        mValue = value;
    }

    public char get() {
        return mValue;
    }

    @Override
    protected Type type() {
        return Type.getChr();
    }

    @Override
    protected Object object() {
        return mValue;
    }

    @Override
    protected String string() {
        return Character.toString(mValue);
    }

}
