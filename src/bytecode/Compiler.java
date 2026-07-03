package bytecode;

import ast.*;

public class Compiler implements Visitor<Void> {
    private final Chunk chunk = new Chunk();

    public Chunk compile(Program program) {
        program.accept(this);
        chunk.write(Opcode.HALT);
        return chunk;
    }

    @Override
    public Void visitProgram(Program node) {
        node.strategy.accept(this);
        return null;
    }

    @Override
    public Void visitStrategy(Strategy node) {
        node.onTick.accept(this);
        return null;
    }

    @Override
    public Void visitOnTickStatement(OnTickStatement node) {
        node.body.accept(this);
        return null;
    }

    @Override
    public Void visitBlockStatement(BlockStatement node) {
        for (Statement stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement node) {
        node.condition.accept(this);
        
        int jumpIfFalseOffset = chunk.getCount();
        chunk.write(Opcode.JUMP_IF_FALSE, -1);
        
        node.thenBranch.accept(this);
        
        int jumpEndOffset = chunk.getCount();
        chunk.write(Opcode.JUMP, -1);
        
        chunk.patchOperand(jumpIfFalseOffset, chunk.getCount());
        
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        
        chunk.patchOperand(jumpEndOffset, chunk.getCount());
        
        return null;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration node) {
        node.initializer.accept(this);
        chunk.write(Opcode.STORE_VAR, node.name);
        return null;
    }

    @Override
    public Void visitAssignmentStatement(AssignmentStatement node) {
        node.value.accept(this);
        chunk.write(Opcode.STORE_VAR, node.name);
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatement node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public Void visitBuyStatement(BuyStatement node) {
        node.quantity.accept(this);
        chunk.write(Opcode.BUY);
        return null;
    }

    @Override
    public Void visitSellStatement(SellStatement node) {
        node.quantity.accept(this);
        chunk.write(Opcode.SELL);
        return null;
    }

    @Override
    public Void visitLiteralExpression(LiteralExpression node) {
        int index = chunk.addConstant(node.value);
        chunk.write(Opcode.LOAD_CONST, index);
        return null;
    }

    @Override
    public Void visitVariableExpression(VariableExpression node) {
        chunk.write(Opcode.LOAD_VAR, node.name);
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression node) {
        node.right.accept(this);
        if (node.operator.equals("-")) chunk.write(Opcode.NEGATE);
        if (node.operator.equals("!")) chunk.write(Opcode.NOT);
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression node) {
        node.left.accept(this);
        node.right.accept(this);
        switch (node.operator) {
            case "+": chunk.write(Opcode.ADD); break;
            case "-": chunk.write(Opcode.SUB); break;
            case "*": chunk.write(Opcode.MUL); break;
            case "/": chunk.write(Opcode.DIV); break;
            case ">": chunk.write(Opcode.GREATER); break;
            case "<": chunk.write(Opcode.LESS); break;
            case ">=": chunk.write(Opcode.GREATER_EQUAL); break;
            case "<=": chunk.write(Opcode.LESS_EQUAL); break;
            case "==": chunk.write(Opcode.EQUAL); break;
            case "!=": chunk.write(Opcode.NOT_EQUAL); break;
            case "&&": chunk.write(Opcode.AND); break;
            case "||": chunk.write(Opcode.OR); break;
        }
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression node) {
        for (Expression arg : node.arguments) {
            arg.accept(this);
        }
        chunk.write(Opcode.CALL, new Object[]{node.callee, node.arguments.size()});
        return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression node) {
        node.value.accept(this);
        chunk.write(Opcode.STORE_VAR, node.name);
        chunk.write(Opcode.LOAD_VAR, node.name);
        return null;
    }
}
