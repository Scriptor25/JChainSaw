package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_CHR;

public class ChrValue extends Value {

    private final char mValue;

    public ChrValue() {
        mValue = '\0';
    }

    public ChrValue(char value) {
        mValue = value;
    }

    public char get() {
        return mValue;
    }

    @Override
    protected String type() {
        return TYPE_CHR;
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
