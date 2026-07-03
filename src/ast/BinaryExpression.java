package ast;

public class BinaryExpression implements Expression {
    public final Expression left;
    public final String operator;
    public final Expression right;

    public BinaryExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}
