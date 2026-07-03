package semantic;

import ast.Program;
import lexer.Lexer;
import parser.Parser;

public class SemanticTest {
    public static void main(String[] args) {
        testValidTyping();
        testInvalidTyping();
        System.out.println("All Semantic tests passed.");
    }

    private static void testValidTyping() {
        String source = 
            "strategy MyStrat { \n" +
            "  on tick(symbol) { \n" +
            "    let x: int = 10; \n" +
            "    let price: Price = 100.5; \n" +
            "    if price > x { buy 10; } \n" +
            "  } \n" +
            "}";
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.scanTokens());
        Program program = parser.parseProgram();
        
        TypeChecker checker = new TypeChecker();
        checker.check(program);
    }

    private static void testInvalidTyping() {
        String source = 
            "strategy MyStrat { \n" +
            "  on tick(symbol) { \n" +
            "    let x: int = true; \n" +
            "  } \n" +
            "}";
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.scanTokens());
        Program program = parser.parseProgram();
        
        TypeChecker checker = new TypeChecker();
        boolean failed = false;
        try {
            checker.check(program);
        } catch (RuntimeException e) {
            failed = true;
        }
        if (!failed) throw new RuntimeException("Expected type error");
    }
}
