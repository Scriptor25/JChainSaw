package io.scriptor.csaw.impl.interpreter.value;

import io.scriptor.csaw.impl.interpreter.Type;

public class ConstNull extends Value {

    public final String why;

    public ConstNull(String why) {
        this.why = why;
    }

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
