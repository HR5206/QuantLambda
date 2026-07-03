package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int startColumn = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("strategy", TokenType.STRATEGY);
        keywords.put("on", TokenType.ON);
        keywords.put("tick", TokenType.TICK);
        keywords.put("let", TokenType.LET);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("buy", TokenType.BUY);
        keywords.put("sell", TokenType.SELL);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("int", TokenType.TYPE_INT);
        keywords.put("double", TokenType.TYPE_DOUBLE);
        keywords.put("boolean", TokenType.TYPE_BOOLEAN);
        keywords.put("string", TokenType.TYPE_STRING);
        keywords.put("Price", TokenType.TYPE_PRICE);
        keywords.put("Quantity", TokenType.TYPE_QUANTITY);
        keywords.put("Series<Price>", TokenType.TYPE_SERIES_PRICE);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            startColumn = column;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ':': addToken(TokenType.COLON); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.ASSIGN);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    addErrorToken("Unexpected character '&'");
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    addErrorToken("Unexpected character '|'");
                }
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                column = 1;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    addErrorToken("Unexpected character '" + c + "'");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()) || (peek() == '<' || peek() == '>')) {
            if (peek() == '<' || peek() == '>') {
                advance();
            } else {
                advance();
            }
        }

        String text = source.substring(start, current);
        if (text.equals("Series<Price>")) {
            addToken(TokenType.TYPE_SERIES_PRICE);
        } else {
            while (text.endsWith("<") || text.endsWith(">")) {
                current--;
                column--;
                text = source.substring(start, current);
            }
            TokenType type = keywords.get(text);
            if (type == null) type = TokenType.IDENTIFIER;
            addToken(type);
        }
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
            addToken(TokenType.NUMBER_LITERAL, Double.parseDouble(source.substring(start, current)));
        } else {
            addToken(TokenType.NUMBER_LITERAL, Integer.parseInt(source.substring(start, current)));
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            advance();
        }

        if (isAtEnd()) {
            addErrorToken("Unterminated string.");
            return;
        }

        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING_LITERAL, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        column++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char advance() {
        column++;
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, startColumn));
    }

    private void addErrorToken(String message) {
        tokens.add(new Token(TokenType.ERROR, source.substring(start, current), message, line, startColumn));
    }
}
