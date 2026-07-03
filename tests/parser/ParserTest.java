package parser;

import ast.Program;
import lexer.Lexer;
import lexer.Token;
import java.util.List;

public class ParserTest {
    public static void main(String[] args) {
        testStrategyParsing();
        System.out.println("All Parser tests passed.");
    }

    private static void testStrategyParsing() {
        String source = 
            "strategy MyStrat { \n" +
            "  on tick(symbol) { \n" +
            "    let x: int = 10 + 5 * 2; \n" +
            "    if x > 15 { buy 100; } else { sell 50; } \n" +
            "  } \n" +
            "}";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        Program program = parser.parseProgram();

        if (!program.strategy.name.equals("MyStrat")) throw new RuntimeException("Failed: Strategy name");
        if (!program.strategy.onTick.symbol.equals("symbol")) throw new RuntimeException("Failed: OnTick symbol");
        if (program.strategy.onTick.body.statements.size() != 2) throw new RuntimeException("Failed: Statements count");
    }
}
