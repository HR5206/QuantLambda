package ast;

public interface Visitor<R> {
    R visitProgram(Program node);
    R visitStrategy(Strategy node);
    R visitOnTickStatement(OnTickStatement node);
    R visitBlockStatement(BlockStatement node);
    R visitIfStatement(IfStatement node);
    R visitVariableDeclaration(VariableDeclaration node);
    R visitAssignmentStatement(AssignmentStatement node);
    R visitExpressionStatement(ExpressionStatement node);
    R visitBuyStatement(BuyStatement node);
    R visitSellStatement(SellStatement node);
    R visitLiteralExpression(LiteralExpression node);
    R visitVariableExpression(VariableExpression node);
    R visitUnaryExpression(UnaryExpression node);
    R visitBinaryExpression(BinaryExpression node);
    R visitCallExpression(CallExpression node);
    R visitAssignmentExpression(AssignmentExpression node);
}
