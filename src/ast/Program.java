package ast;

public class Program implements Node {
    public final Strategy strategy;

    public Program(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitProgram(this);
    }
}
