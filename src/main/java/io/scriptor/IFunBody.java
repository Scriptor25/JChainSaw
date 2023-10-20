package io.scriptor;

import io.scriptor.value.Value;

@FunctionalInterface
public interface IFunBody {

    public Value invoke(Value member, Environment env, Value... args);
}
