package ast;

public class IfStatement implements Statement {
    public final Expression condition;
    public final BlockStatement thenBranch;
    public final BlockStatement elseBranch;

    public IfStatement(Expression condition, BlockStatement thenBranch, BlockStatement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitIfStatement(this);
    }
}
