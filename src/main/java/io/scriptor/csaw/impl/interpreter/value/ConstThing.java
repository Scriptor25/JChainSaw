package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.interpreter.Environment.isAssignable;

import java.util.HashMap;
import java.util.Map;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.Type;
import io.scriptor.csaw.impl.interpreter.Variable;

public class ConstThing extends Value {

    private final String mType;
    private final Map<String, Variable> mFields = new HashMap<>();

    public ConstThing(Environment env, String type) {
        mType = type;
        for (final var field : Environment.getThing(type))
            mFields.put(field.name, new Variable(field.type, Value.makeValue(env, field.type, true, false)));
    }

    public Value getField(String field) {
        return mFields.get(field).value;
    }

    public <V extends Value> V setField(String field, V value) {
        if (!mFields.containsKey(field))
            throw new CSawException("undefined field '%s'", field);
        if (!isAssignable(value.getType(), mFields.get(field).type))
            throw new CSawException("cannot assign value of type '%s' to '%s' with type '%s'", value.getType(), field,
                    mFields.get(field).type);
        mFields.get(field).value = value;
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
