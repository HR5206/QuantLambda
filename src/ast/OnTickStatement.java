package ast;

public class OnTickStatement implements Statement {
    public final String symbol;
    public final BlockStatement body;

    public OnTickStatement(String symbol, BlockStatement body) {
        this.symbol = symbol;
        this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitOnTickStatement(this);
    }
}
