package io.scriptor.csaw.impl.value;

import java.util.HashMap;
import java.util.Map;

import io.scriptor.csaw.impl.Environment;
import io.scriptor.csaw.impl.Pair;

public class ObjValue extends Value {

    private final String mType;
    private final Map<String, Pair<String, Value>> mFields = new HashMap<>();

    public ObjValue(Environment env, String type) throws Exception {
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
    public Object getValue() {
        return mFields;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s %s", mType, mFields);
    }

}
