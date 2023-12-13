package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.interpreter.Environment.isAssignable;

import java.util.Arrays;

import io.scriptor.csaw.impl.CSawException;

public class ValueRef extends Value {

    private final Value[] mValues;
    private final String mType;

    public ValueRef(int size, String type) {
        mValues = new Value[size];
        mType = type;
    }

    public ValueRef(Value value) {
        mValues = new Value[] { value };
        mType = value.getType();
    }

    public int size() {
        return mValues.length;
    }

    public Value get(int i) {
        return mValues[i];
    }

    public Value set(int i, Value value) {
        if (!isAssignable(value.getType(), mType))
            throw new CSawException("cannot set type '%s' in reference of type '%s'", value.getType(), mType);
        return mValues[i] = value;
    }

    @Override
    protected String type() {
        return "ref__" + mType;
    }

    @Override
    protected Object object() {
        return mValues;
    }

    @Override
    protected String string() {
        return Arrays.toString(mValues);
    }

}
