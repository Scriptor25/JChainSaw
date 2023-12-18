package io.scriptor.csaw.impl.interpreter;

import io.scriptor.csaw.impl.interpreter.value.Value;

@FunctionalInterface
public interface IFunBody {

    public Value invoke(Value member, Value... args);
}
