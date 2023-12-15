package io.scriptor.csaw.impl.stmt;

public class IncStmt extends Stmt {

    public final String path;

    public IncStmt(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("inc \"%s\"", path);
    }
}
