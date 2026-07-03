package ast;

public class LiteralExpression implements Expression {
    public final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteralExpression(this);
    }
}
