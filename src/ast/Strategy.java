package ast;

public class Strategy implements Node {
    public final String name;
    public final OnTickStatement onTick;

    public Strategy(String name, OnTickStatement onTick) {
        this.name = name;
        this.onTick = onTick;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitStrategy(this);
    }
}
