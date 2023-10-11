package io.scriptor;

import io.scriptor.stmt.Stmt;
import io.scriptor.value.Value;

public class FunDef<T extends Value> {

    public final Class<T> type;
    public FunParam<?>[] parameters;
    public Stmt[] implementation;

    public FunDef(Class<T> type) {
        this.type = type;
    }
}
