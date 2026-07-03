package ast;

public class SellStatement implements Statement {
    public final Expression quantity;

    public SellStatement(Expression quantity) {
        this.quantity = quantity;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSellStatement(this);
    }
}
