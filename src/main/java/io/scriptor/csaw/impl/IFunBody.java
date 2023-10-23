package io.scriptor.csaw.impl;

import io.scriptor.csaw.impl.value.Value;

@FunctionalInterface
public interface IFunBody {

    public Value invoke(Value member, Value... args) ;
}
