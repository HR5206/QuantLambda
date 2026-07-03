package ast;

public class VariableDeclaration implements Statement {
    public final String name;
    public final String type;
    public final Expression initializer;

    public VariableDeclaration(String name, String type, Expression initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVariableDeclaration(this);
    }
}
