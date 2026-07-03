package ast;

public class BuyStatement implements Statement {
    public final Expression quantity;

    public BuyStatement(Expression quantity) {
        this.quantity = quantity;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBuyStatement(this);
    }
}
