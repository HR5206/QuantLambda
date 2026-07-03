package vm;

import ast.Program;
import bytecode.Chunk;
import bytecode.Compiler;
import lexer.Lexer;
import parser.Parser;

public class VMTest {
    public static void main(String[] args) {
        testVMExecution();
        System.out.println("All VM tests passed.");
    }

    private static void testVMExecution() {
        String source = 
            "strategy MyStrat { \n" +
            "  on tick(symbol) { \n" +
            "    let x: int = 10; \n" +
            "    if symbol > x { buy 5; } else { sell 5; } \n" +
            "  } \n" +
            "}";
        
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.scanTokens());
        Program program = parser.parseProgram();

        Compiler compiler = new Compiler();
        Chunk chunk = compiler.compile(program);

        VirtualMachine vm = new VirtualMachine();
        vm.init(chunk);
        vm.setGlobal("symbol", 15.0);
        vm.run();
        
        if (vm.position != 5.0) {
            throw new RuntimeException("VM failed, expected position 5.0 but got " + vm.position);
        }

        vm.init(chunk);
        vm.setGlobal("symbol", 5.0);
        vm.run();

        if (vm.position != 0.0) {
            throw new RuntimeException("VM failed, expected position 0.0 but got " + vm.position);
        }
    }
}
