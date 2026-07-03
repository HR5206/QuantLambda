package backtest;

import bytecode.Chunk;
import bytecode.Compiler;
import lexer.Lexer;
import parser.Parser;
import runtime.CSVLoader;
import runtime.OHLCV;
import runtime.Portfolio;
import vm.VirtualMachine;
import ast.Program;

import java.util.List;

public class BacktestEngine {
    private final VirtualMachine vm;
    private final Portfolio portfolio;
    private final List<OHLCV> data;
    private final Chunk chunk;

    public BacktestEngine(String strategySource, String dataPath, double initialCash) {
        Lexer lexer = new Lexer(strategySource);
        Parser parser = new Parser(lexer.scanTokens());
        Program program = parser.parseProgram();
        
        Compiler compiler = new Compiler();
        this.chunk = compiler.compile(program);
        
        this.vm = new VirtualMachine();
        this.vm.init(this.chunk);
        this.portfolio = new Portfolio(initialCash);
        this.data = CSVLoader.load(dataPath);
    }

    public void run() {
        int trades = 0;
        double sumPrices = 0;
        int count = 0;
        
        for (OHLCV candle : data) {
            double currentPrice = candle.close;
            sumPrices += currentPrice;
            count++;
            
            // Simple builtin simulations
            vm.setGlobal("symbol", currentPrice);
            vm.setGlobal("sma_10", sumPrices / count);
            
            double oldPosition = vm.position;
            
            vm.init(chunk); 
            vm.run();
            
            double positionChange = vm.position - oldPosition;
            if (positionChange != 0) {
                portfolio.executeTrade(positionChange, currentPrice);
                trades++;
            }
        }
        
        double finalPrice = data.get(data.size() - 1).close;
        double finalValue = portfolio.getPortfolioValue(finalPrice);
        System.out.println("Final Portfolio Value: $" + String.format("%.2f", finalValue));
        System.out.println("Total Trades: " + trades);
        System.out.println("Profit and Loss: $" + String.format("%.2f", finalValue - 100000.0));
    }
}
