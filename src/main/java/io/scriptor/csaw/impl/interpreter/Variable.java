package io.scriptor.csaw.impl.interpreter;

import io.scriptor.csaw.impl.interpreter.value.Value;

public class Variable {

    public Type type;
    public Value value;

    public Variable(Type type, Value value) {
        this.type = type;
        this.value = value;
    }
}
