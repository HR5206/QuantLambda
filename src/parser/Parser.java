package parser;

import ast.*;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    private interface PrefixParselet {
        Expression parse(Token token);
    }

    private interface InfixParselet {
        Expression parse(Expression left, Token token);
    }

    private final Map<TokenType, PrefixParselet> prefixParselets = new HashMap<>();
    private final Map<TokenType, InfixParselet> infixParselets = new HashMap<>();
    private final Map<TokenType, Integer> bindingPowers = new HashMap<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        
        prefixParselets.put(TokenType.NUMBER_LITERAL, this::parseLiteral);
        prefixParselets.put(TokenType.STRING_LITERAL, this::parseLiteral);
        prefixParselets.put(TokenType.TRUE, this::parseLiteral);
        prefixParselets.put(TokenType.FALSE, this::parseLiteral);
        prefixParselets.put(TokenType.IDENTIFIER, this::parseIdentifierOrCall);
        prefixParselets.put(TokenType.MINUS, this::parsePrefix);
        prefixParselets.put(TokenType.BANG, this::parsePrefix);
        prefixParselets.put(TokenType.LEFT_PAREN, this::parseGroup);

        registerInfix(TokenType.PLUS, 4, this::parseInfix);
        registerInfix(TokenType.MINUS, 4, this::parseInfix);
        registerInfix(TokenType.STAR, 5, this::parseInfix);
        registerInfix(TokenType.SLASH, 5, this::parseInfix);
        
        registerInfix(TokenType.EQUAL_EQUAL, 3, this::parseInfix);
        registerInfix(TokenType.BANG_EQUAL, 3, this::parseInfix);
        registerInfix(TokenType.GREATER, 3, this::parseInfix);
        registerInfix(TokenType.LESS, 3, this::parseInfix);
        registerInfix(TokenType.GREATER_EQUAL, 3, this::parseInfix);
        registerInfix(TokenType.LESS_EQUAL, 3, this::parseInfix);
        
        registerInfix(TokenType.AND, 2, this::parseInfix);
        registerInfix(TokenType.OR, 1, this::parseInfix);
    }

    private void registerInfix(TokenType type, int bp, InfixParselet parselet) {
        infixParselets.put(type, parselet);
        bindingPowers.put(type, bp);
    }

    public Program parseProgram() {
        Strategy strategy = parseStrategy();
        return new Program(strategy);
    }

    private Strategy parseStrategy() {
        consume(TokenType.STRATEGY, "Expected 'strategy'");
        Token name = consume(TokenType.IDENTIFIER, "Expected strategy name");
        consume(TokenType.LEFT_BRACE, "Expected '{' after strategy name");
        OnTickStatement onTick = parseOnTickStatement();
        consume(TokenType.RIGHT_BRACE, "Expected '}' after strategy body");
        return new Strategy(name.lexeme, onTick);
    }

    private OnTickStatement parseOnTickStatement() {
        consume(TokenType.ON, "Expected 'on'");
        consume(TokenType.TICK, "Expected 'tick'");
        consume(TokenType.LEFT_PAREN, "Expected '(' after tick");
        Token symbol = consume(TokenType.IDENTIFIER, "Expected symbol identifier");
        consume(TokenType.RIGHT_PAREN, "Expected ')' after symbol");
        BlockStatement body = parseBlockStatement();
        return new OnTickStatement(symbol.lexeme, body);
    }

    private BlockStatement parseBlockStatement() {
        consume(TokenType.LEFT_BRACE, "Expected '{'");
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(parseStatement());
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block");
        return new BlockStatement(statements);
    }

    private Statement parseStatement() {
        if (match(TokenType.LET)) return parseVariableDeclaration();
        if (match(TokenType.IF)) return parseIfStatement();
        if (match(TokenType.LEFT_BRACE)) {
            current--; 
            return parseBlockStatement();
        }
        if (match(TokenType.BUY)) return parseBuyStatement();
        if (match(TokenType.SELL)) return parseSellStatement();
        
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) {
            return parseAssignmentStatement();
        }
        
        return parseExpressionStatement();
    }

    private VariableDeclaration parseVariableDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name");
        String type = null;
        if (match(TokenType.COLON)) {
            type = consumeType();
        }
        consume(TokenType.ASSIGN, "Expected '='");
        Expression initializer = parseExpression(0);
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new VariableDeclaration(name.lexeme, type, initializer);
    }

    private AssignmentStatement parseAssignmentStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name");
        consume(TokenType.ASSIGN, "Expected '='");
        Expression value = parseExpression(0);
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new AssignmentStatement(name.lexeme, value);
    }

    private IfStatement parseIfStatement() {
        Expression condition = parseExpression(0);
        BlockStatement thenBranch = parseBlockStatement();
        BlockStatement elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = parseBlockStatement();
        }
        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private BuyStatement parseBuyStatement() {
        Expression quantity = parseExpression(0);
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new BuyStatement(quantity);
    }

    private SellStatement parseSellStatement() {
        Expression quantity = parseExpression(0);
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new SellStatement(quantity);
    }

    private ExpressionStatement parseExpressionStatement() {
        Expression expr = parseExpression(0);
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new ExpressionStatement(expr);
    }

    private Expression parseExpression(int precedence) {
        Token token = advance();
        PrefixParselet prefix = prefixParselets.get(token.type);
        if (prefix == null) {
            throw new RuntimeException("Could not parse expression starting with " + token.type + " at line " + token.line);
        }
        Expression left = prefix.parse(token);

        while (precedence < getBindingPower()) {
            token = advance();
            InfixParselet infix = infixParselets.get(token.type);
            left = infix.parse(left, token);
        }

        return left;
    }

    private int getBindingPower() {
        Integer bp = bindingPowers.get(peek().type);
        return bp != null ? bp : 0;
    }

    private Expression parseLiteral(Token token) {
        if (token.type == TokenType.TRUE) return new LiteralExpression(true);
        if (token.type == TokenType.FALSE) return new LiteralExpression(false);
        return new LiteralExpression(token.literal);
    }

    private Expression parseIdentifierOrCall(Token token) {
        if (match(TokenType.LEFT_PAREN)) {
            List<Expression> args = new ArrayList<>();
            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    args.add(parseExpression(0));
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments");
            return new CallExpression(token.lexeme, args);
        }
        return new VariableExpression(token.lexeme);
    }

    private Expression parsePrefix(Token token) {
        Expression right = parseExpression(6); 
        return new UnaryExpression(token.lexeme, right);
    }

    private Expression parseInfix(Expression left, Token token) {
        Expression right = parseExpression(bindingPowers.get(token.type));
        return new BinaryExpression(left, token.lexeme, right);
    }

    private Expression parseGroup(Token token) {
        Expression expr = parseExpression(0);
        consume(TokenType.RIGHT_PAREN, "Expected ')'");
        return expr;
    }

    private String consumeType() {
        Token t = advance();
        if (t.type == TokenType.TYPE_INT) return "int";
        if (t.type == TokenType.TYPE_DOUBLE) return "double";
        if (t.type == TokenType.TYPE_BOOLEAN) return "boolean";
        if (t.type == TokenType.TYPE_STRING) return "string";
        if (t.type == TokenType.TYPE_PRICE) return "Price";
        if (t.type == TokenType.TYPE_QUANTITY) return "Quantity";
        if (t.type == TokenType.TYPE_SERIES_PRICE) return "Series<Price>";
        throw new RuntimeException("Expected type but found " + t.lexeme + " at line " + t.line);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message + " found " + peek().lexeme + " at line " + peek().line);
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
}
