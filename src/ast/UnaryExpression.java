package ast;

public class UnaryExpression implements Expression {
    public final String operator;
    public final Expression right;

    public UnaryExpression(String operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}
