package io.scriptor.csaw.impl.interpreter.value;

import io.scriptor.csaw.impl.interpreter.Type;

public class NamedValue extends Value {

    private final String mName;
    private final Value mValue;

    public NamedValue(String name, Value value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Value get() {
        return mValue;
    }

    @Override
    protected Type type() {
        return mValue.type();
    }

    @Override
    protected Object object() {
        return mValue.object();
    }

    @Override
    protected String string() {
        return mValue.string();
    }

}
