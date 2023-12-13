package io.scriptor.csaw.impl.interpreter.value;

import java.util.HashMap;
import java.util.Map;

import io.scriptor.csaw.impl.Pair;
import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.interpreter.Environment;

public class ConstThing extends Value {

    private final String mType;
    private final Map<String, Pair<Type, Value>> mFields = new HashMap<>();

    public ConstThing(Environment env, String type) {
        mType = type;
        for (final var field : Environment.getType(type))
            mFields.put(field.name, new Pair<>(field.type, Value.makeValue(env, field.type, true, false)));
    }

    public Value getField(String field) {
        return mFields.get(field).second;
    }

    public <V extends Value> V setField(String field, V value) {
        mFields.get(field).second = value;
        return value;
    }

    @Override
    protected Type type() {
        return Type.get(mType);
    }

    @Override
    protected Object object() {
        return null;
    }

    @Override
    protected String string() {
        return String.format("%s %s", mType, mFields);
    }

}
