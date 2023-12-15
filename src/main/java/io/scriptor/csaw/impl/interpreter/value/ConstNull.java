package io.scriptor.csaw.impl.interpreter.value;

import io.scriptor.csaw.impl.Type;

public class ConstNull extends Value {

    @Override
    protected Type type() {
        return Type.getNull();
    }

    @Override
    protected Object object() {
        return null;
    }

    @Override
    protected String string() {
        return "{}";
    }

}
