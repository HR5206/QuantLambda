package ast;

import java.util.List;

public class CallExpression implements Expression {
    public final String callee;
    public final List<Expression> arguments;

    public CallExpression(String callee, List<Expression> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitCallExpression(this);
    }
}
