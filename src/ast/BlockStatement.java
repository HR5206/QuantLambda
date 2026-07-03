package ast;

import java.util.List;

public class BlockStatement implements Statement {
    public final List<Statement> statements;

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBlockStatement(this);
    }
}
