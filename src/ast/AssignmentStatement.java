package ast;

public class AssignmentStatement implements Statement {
    public final String name;
    public final Expression value;

    public AssignmentStatement(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignmentStatement(this);
    }
}
