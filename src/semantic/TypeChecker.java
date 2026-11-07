package semantic;

import ast.*;

public class TypeChecker implements Visitor<String> {
    private SymbolTable currentScope;

    public TypeChecker() {
        this.currentScope = new SymbolTable(null);
        // Built-ins
        this.currentScope.define("sma_10", "double");
    }

    public void check(Program program) {
        program.accept(this);
    }

    private void beginScope() {
        currentScope = new SymbolTable(currentScope);
    }

    private void endScope() {
        currentScope = currentScope.parent;
    }

    @Override
    public String visitProgram(Program node) {
        node.strategy.accept(this);
        return null;
    }

    @Override
    public String visitStrategy(Strategy node) {
        node.onTick.accept(this);
        return null;
    }

    @Override
    public String visitOnTickStatement(OnTickStatement node) {
        beginScope();
        currentScope.define(node.symbol, "Price");
        node.body.accept(this);
        endScope();
        return null;
    }

    @Override
    public String visitBlockStatement(BlockStatement node) {
        beginScope();
        for (Statement stmt : node.statements) {
            stmt.accept(this);
        }
        endScope();
        return null;
    }

    @Override
    public String visitIfStatement(IfStatement node) {
        String condType = node.condition.accept(this);
        if (!condType.equals("boolean")) {
            throw new RuntimeException("If condition must be boolean, got " + condType);
        }
        node.thenBranch.accept(this);
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public String visitVariableDeclaration(VariableDeclaration node) {
        String exprType = node.initializer.accept(this);
        String declaredType = node.type;
        if ("Quantity".equals(declaredType) || "Series<Price>".equals(declaredType))
            throw new RuntimeException("This annotation is reserved but not implemented: " + declaredType);
        
        if (declaredType == null) {
            declaredType = exprType; // Simple type inference
        } else if (!declaredType.equals(exprType)) {
            // Very simple type conversion tolerance between int and double
            if (declaredType.equals("double") && exprType.equals("int")) {
                // Allowed
            } else if (declaredType.equals("Price") && (exprType.equals("double") || exprType.equals("int"))) {
                // Allowed
            } else {
                throw new RuntimeException("Type mismatch: expected " + declaredType + " but got " + exprType);
            }
        }
        currentScope.define(node.name, declaredType);
        return null;
    }

    @Override
    public String visitAssignmentStatement(AssignmentStatement node) {
        String varType = currentScope.resolve(node.name);
        String exprType = node.value.accept(this);
        
        if (!varType.equals(exprType)) {
            if (varType.equals("double") && exprType.equals("int")) {
                // Allowed
            } else if (varType.equals("Price") && (exprType.equals("double") || exprType.equals("int"))) {
                // Allowed
            } else {
                throw new RuntimeException("Type mismatch in assignment: expected " + varType + " but got " + exprType);
            }
        }
        return null;
    }

    @Override
    public String visitExpressionStatement(ExpressionStatement node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public String visitBuyStatement(BuyStatement node) {
        String type = node.quantity.accept(this);
        if (!type.equals("int") && !type.equals("double") && !type.equals("Quantity")) {
            throw new RuntimeException("Buy quantity must be numeric, got " + type);
        }
        return null;
    }

    @Override
    public String visitSellStatement(SellStatement node) {
        String type = node.quantity.accept(this);
        if (!type.equals("int") && !type.equals("double") && !type.equals("Quantity")) {
            throw new RuntimeException("Sell quantity must be numeric, got " + type);
        }
        return null;
    }

    @Override
    public String visitLiteralExpression(LiteralExpression node) {
        if (node.value instanceof Integer) return "int";
        if (node.value instanceof Double) return "double";
        if (node.value instanceof Boolean) return "boolean";
        if (node.value instanceof String) return "string";
        return "unknown";
    }

    @Override
    public String visitVariableExpression(VariableExpression node) {
        return currentScope.resolve(node.name);
    }

    @Override
    public String visitUnaryExpression(UnaryExpression node) {
        String rightType = node.right.accept(this);
        if (node.operator.equals("-")) {
            if (!rightType.equals("int") && !rightType.equals("double") && !rightType.equals("Price")) {
                throw new RuntimeException("Unary '-' requires numeric type.");
            }
            return rightType;
        }
        if (node.operator.equals("!")) {
            if (!rightType.equals("boolean")) {
                throw new RuntimeException("Unary '!' requires boolean type.");
            }
            return "boolean";
        }
        return "unknown";
    }

    @Override
    public String visitBinaryExpression(BinaryExpression node) {
        String left = node.left.accept(this);
        String right = node.right.accept(this);
        
        switch (node.operator) {
            case "+": case "-": case "*": case "/":
                if ((left.equals("int") || left.equals("double") || left.equals("Price")) &&
                    (right.equals("int") || right.equals("double") || right.equals("Price"))) {
                    if (left.equals("double") || right.equals("double") || left.equals("Price") || right.equals("Price")) {
                        return "double";
                    }
                    return node.operator.equals("/") ? "double" : "int";
                }
                throw new RuntimeException("Arithmetic operations require numeric types.");
            case ">": case "<": case ">=": case "<=":
                if ((left.equals("int") || left.equals("double") || left.equals("Price")) &&
                    (right.equals("int") || right.equals("double") || right.equals("Price"))) {
                    return "boolean";
                }
                throw new RuntimeException("Relational operations require numeric types.");
            case "==": case "!=":
                return "boolean";
            case "&&": case "||":
                if (left.equals("boolean") && right.equals("boolean")) {
                    return "boolean";
                }
                throw new RuntimeException("Logical operations require boolean types.");
        }
        return "unknown";
    }

    @Override
    public String visitCallExpression(CallExpression node) {
        throw new RuntimeException("Function calls are not implemented: " + node.callee);
    }

    @Override
    public String visitAssignmentExpression(AssignmentExpression node) {
        String varType = currentScope.resolve(node.name);
        String exprType = node.value.accept(this);
        if (!varType.equals(exprType)) {
            if (varType.equals("double") && exprType.equals("int")) {
                // Allowed
            } else if (varType.equals("Price") && (exprType.equals("double") || exprType.equals("int"))) {
                // Allowed
            } else {
                throw new RuntimeException("Type mismatch in assignment expression");
            }
        }
        return varType;
    }
}
