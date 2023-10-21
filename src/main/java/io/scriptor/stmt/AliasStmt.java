package io.scriptor.stmt;

public class AliasStmt extends Stmt {

    public String alias;
    public String origin;

    @Override
    public String toString() {
        return String.format("alias %s : %s;", alias, origin);
    }

}
