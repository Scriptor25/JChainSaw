package io.scriptor.stmt;

public class IncStmt extends Stmt {

    public String path;

    @Override
    public String toString() {
        return String.format("inc \"%s\"", path);
    }
}
