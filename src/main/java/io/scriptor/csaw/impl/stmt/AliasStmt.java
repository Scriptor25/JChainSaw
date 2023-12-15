package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.Type;

public class AliasStmt extends Stmt {

    public final String alias;
    public final Type origin;

    public AliasStmt(String alias, Type origin) {
        this.alias = alias;
        this.origin = origin;
    }

    @Override
    public String toString() {
        return String.format("alias %s : %s", alias, origin);
    }

}
