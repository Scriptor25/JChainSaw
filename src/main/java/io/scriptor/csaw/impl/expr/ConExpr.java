package io.scriptor.csaw.impl.expr;

public class ConExpr extends Expr {

    public final Expr condition;
    public final Expr thenExpr;
    public final Expr elseExpr;

    public ConExpr(Expr condition, Expr thenExpr, Expr elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    @Override
    public boolean isConstant() {
        if (!condition.isConstant())
            return false;
        final var value = new ConstExpr(condition);
        if (value.value.asNum().getBool())
            return thenExpr.isConstant();
        return elseExpr.isConstant();
    }

    @Override
    public Expr makeConstant() {
        if (isConstant())
            return new ConstExpr(this);
        return ConstExpr.make(new ConExpr(condition.makeConstant(), thenExpr.makeConstant(), elseExpr.makeConstant()));
    }

    @Override
    public String toString() {
        return String.format("%s ? %s : %s", condition, thenExpr, elseExpr);
    }
}
