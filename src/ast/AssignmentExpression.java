package ast;

public class AssignmentExpression implements Expression {
    public final String name;
    public final Expression value;

    public AssignmentExpression(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignmentExpression(this);
    }
}
