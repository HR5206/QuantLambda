package vm;
import lexer.Lexer;
import parser.Parser;
import ast.Program;
import semantic.TypeChecker;
import bytecode.Compiler;
import backtest.BacktestEngine;
import java.nio.file.*;
import java.io.*;

public class RegressionTest {
    static Program parse(String body) { return new Parser(new Lexer("strategy T { on tick(price) { " + body + " } }").scanTokens()).parseProgram(); }
    static double run(String body) {
        Program p = parse(body); new TypeChecker().check(p);
        VirtualMachine vm = new VirtualMachine(); vm.init(new Compiler().compile(p)); vm.setGlobal("price", 10.0); vm.run(); return vm.position;
    }
    static void rejects(Runnable f) { try { f.run(); } catch (RuntimeException e) { return; } throw new AssertionError("Expected rejection"); }
    public static void main(String[] args) throws Exception {
        if (run("if 1 == 1.0 { buy 2; }") != 2) throw new AssertionError("Numeric equality");
        if (run("if false && (1 / 0 > 1) { buy 2; }") != 0) throw new AssertionError("AND");
        if (run("if true || (1 / 0 > 1) { buy 2; }") != 2) throw new AssertionError("OR");
        if (run("let x: double = 5 / 2; if x == 2.5 { buy 1; }") != 1) throw new AssertionError("Division");
        rejects(() -> run("buy sma(price, 10);"));
        rejects(() -> run("let x = 1; { let x = 2; }"));
        rejects(() -> run("buy -1;"));
        rejects(() -> new Parser(new Lexer("strategy T { on tick(p) {} } buy 1;").scanTokens()).parseProgram());
        Path csv = Files.createTempFile("quant-regression", ".csv");
        try {
            String data = "timestamp,open,high,low,close,volume\n";
            for (int i = 1; i <= 11; i++) data += i + "," + i + "," + i + "," + i + "," + i + ",1\n";
            Files.writeString(csv, data);
            String source = "strategy T { on tick(price) { if price == 11 && sma_10 == 6.5 { buy 1; } } }";
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(); PrintStream old = System.out;
            try { System.setOut(new PrintStream(bytes)); new BacktestEngine(source, csv.toString(), 100).run(); }
            finally { System.setOut(old); }
            if (!bytes.toString().contains("Total Trades: 1") || !bytes.toString().contains("Profit and Loss: $0.00")) throw new AssertionError(bytes.toString());
            rejects(() -> new BacktestEngine("strategy T { on tick(p) { let x: int = true; } }", csv.toString(),100));
            Files.writeString(csv, "timestamp,open,high,low,close,volume\n");
            rejects(() -> new BacktestEngine(source,csv.toString(),100));
            Files.writeString(csv, "header\ninvalid,row\n");
            rejects(() -> new BacktestEngine(source,csv.toString(),100));
        } finally { Files.delete(csv); }
        System.out.println("QuantLambda regression checks passed");
    }
}
