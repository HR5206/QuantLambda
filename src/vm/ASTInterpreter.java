package vm;

import ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ASTInterpreter implements Visitor<Object> {
    private final Map<String, Object> environment = new HashMap<>();
    private Strategy strategy;
    
    public double totalPnL = 0.0;
    public double position = 0.0;

    public void interpret(Program program) {
        program.accept(this);
    }

    public void tick(double price) {
        if (strategy == null) return;
        environment.put(strategy.onTick.symbol, price);
        strategy.onTick.body.accept(this);
    }

    @Override
    public Object visitProgram(Program node) {
        return node.strategy.accept(this);
    }

    @Override
    public Object visitStrategy(Strategy node) {
        this.strategy = node;
        return null;
    }

    @Override
    public Object visitOnTickStatement(OnTickStatement node) {
        return node.body.accept(this);
    }

    @Override
    public Object visitBlockStatement(BlockStatement node) {
        for (Statement stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Object visitIfStatement(IfStatement node) {
        Object condition = node.condition.accept(this);
        if (condition instanceof Boolean && (Boolean) condition) {
            node.thenBranch.accept(this);
        } else if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Object visitVariableDeclaration(VariableDeclaration node) {
        Object value = node.initializer.accept(this);
        environment.put(node.name, value);
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement node) {
        Object value = node.value.accept(this);
        environment.put(node.name, value);
        return null;
    }

    @Override
    public Object visitExpressionStatement(ExpressionStatement node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public Object visitBuyStatement(BuyStatement node) {
        Object qty = node.quantity.accept(this);
        double quantity = 0;
        if (qty instanceof Number) quantity = ((Number) qty).doubleValue();
        position += quantity;
        return null;
    }

    @Override
    public Object visitSellStatement(SellStatement node) {
        Object qty = node.quantity.accept(this);
        double quantity = 0;
        if (qty instanceof Number) quantity = ((Number) qty).doubleValue();
        position -= quantity;
        return null;
    }

    @Override
    public Object visitLiteralExpression(LiteralExpression node) {
        return node.value;
    }

    @Override
    public Object visitVariableExpression(VariableExpression node) {
        return environment.get(node.name);
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression node) {
        Object right = node.right.accept(this);
        if (node.operator.equals("-")) {
            if (right instanceof Double) return -(Double) right;
            if (right instanceof Integer) return -(Integer) right;
        }
        if (node.operator.equals("!")) {
            if (right instanceof Boolean) return !(Boolean) right;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression node) {
        Object left = node.left.accept(this);
        Object right = node.right.accept(this);

        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            switch (node.operator) {
                case "+": return l + r;
                case "-": return l - r;
                case "*": return l * r;
                case "/": return l / r;
                case ">": return l > r;
                case "<": return l < r;
                case ">=": return l >= r;
                case "<=": return l <= r;
                case "==": return l == r;
                case "!=": return l != r;
            }
        }
        if (left instanceof Boolean && right instanceof Boolean) {
            boolean l = (Boolean) left;
            boolean r = (Boolean) right;
            switch (node.operator) {
                case "&&": return l && r;
                case "||": return l || r;
                case "==": return l == r;
                case "!=": return l != r;
            }
        }
        return null;
    }

    @Override
    public Object visitCallExpression(CallExpression node) {
        // Built-ins not fully simulated in minimal baseline
        return 0.0;
    }

    @Override
    public Object visitAssignmentExpression(AssignmentExpression node) {
        Object value = node.value.accept(this);
        environment.put(node.name, value);
        return value;
    }
}
