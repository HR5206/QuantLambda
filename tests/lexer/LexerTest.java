package lexer;

import java.util.List;

public class LexerTest {
    public static void main(String[] args) {
        testKeywords();
        testOperators();
        testStrategyAndTick();
        System.out.println("All Lexer tests passed.");
    }

    private static void testKeywords() {
        Lexer lexer = new Lexer("let x = 10; if (x > 5) { buy x; } else { sell x; }");
        List<Token> tokens = lexer.scanTokens();
        assertToken(tokens.get(0), TokenType.LET, "let");
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "x");
        assertToken(tokens.get(2), TokenType.ASSIGN, "=");
        assertToken(tokens.get(3), TokenType.NUMBER_LITERAL, "10");
        assertToken(tokens.get(4), TokenType.SEMICOLON, ";");
    }

    private static void testOperators() {
        Lexer lexer = new Lexer("== != >= <= && || !");
        List<Token> tokens = lexer.scanTokens();
        assertToken(tokens.get(0), TokenType.EQUAL_EQUAL, "==");
        assertToken(tokens.get(1), TokenType.BANG_EQUAL, "!=");
        assertToken(tokens.get(2), TokenType.GREATER_EQUAL, ">=");
        assertToken(tokens.get(3), TokenType.LESS_EQUAL, "<=");
        assertToken(tokens.get(4), TokenType.AND, "&&");
        assertToken(tokens.get(5), TokenType.OR, "||");
        assertToken(tokens.get(6), TokenType.BANG, "!");
    }

    private static void testStrategyAndTick() {
        Lexer lexer = new Lexer("strategy MyStrat { on tick(symbol) { let p: Price = 100.0; } }");
        List<Token> tokens = lexer.scanTokens();
        assertToken(tokens.get(0), TokenType.STRATEGY, "strategy");
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "MyStrat");
        assertToken(tokens.get(2), TokenType.LEFT_BRACE, "{");
        assertToken(tokens.get(3), TokenType.ON, "on");
        assertToken(tokens.get(4), TokenType.TICK, "tick");
        assertToken(tokens.get(5), TokenType.LEFT_PAREN, "(");
        assertToken(tokens.get(6), TokenType.IDENTIFIER, "symbol");
        assertToken(tokens.get(7), TokenType.RIGHT_PAREN, ")");
        assertToken(tokens.get(8), TokenType.LEFT_BRACE, "{");
        assertToken(tokens.get(9), TokenType.LET, "let");
        assertToken(tokens.get(10), TokenType.IDENTIFIER, "p");
        assertToken(tokens.get(11), TokenType.COLON, ":");
        assertToken(tokens.get(12), TokenType.TYPE_PRICE, "Price");
        assertToken(tokens.get(13), TokenType.ASSIGN, "=");
        assertToken(tokens.get(14), TokenType.NUMBER_LITERAL, "100.0");
        assertToken(tokens.get(15), TokenType.SEMICOLON, ";");
    }

    private static void assertToken(Token t, TokenType expectedType, String expectedLexeme) {
        if (t.type != expectedType || !t.lexeme.equals(expectedLexeme)) {
            throw new RuntimeException("Assertion failed! Expected " + expectedType + " '" + expectedLexeme + "', but got " + t.type + " '" + t.lexeme + "'");
        }
    }
}
