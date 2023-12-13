package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_CHR;

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
