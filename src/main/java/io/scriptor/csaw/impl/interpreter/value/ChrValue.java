package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_CHR;

public class ChrValue extends Value {

    private char mValue;

    public ChrValue() {
        mValue = '\0';
    }

    public ChrValue(char value) {
        mValue = value;
    }

    @Override
    public Character getValue() {
        return mValue;
    }

    @Override
    public String getType() {
        return TYPE_CHR;
    }

    @Override
    public boolean asBoolean() {
        return mValue > 0;
    }

    @Override
    public String toString() {
        return Character.toString(mValue);
    }

}
