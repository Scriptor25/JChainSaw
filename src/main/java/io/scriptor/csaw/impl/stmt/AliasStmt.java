package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.Type;

public class AliasStmt extends Stmt {

    public String alias;
    public Type origin;

    @Override
    public String toString() {
        return String.format("alias %s : %s", alias, origin);
    }

}
