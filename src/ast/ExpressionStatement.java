package ast;

public class ExpressionStatement implements Statement {
    public final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitExpressionStatement(this);
    }
}
