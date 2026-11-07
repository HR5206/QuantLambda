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
    private final double initialCash;
    private final String tickVariable;
    private boolean ran;

    public BacktestEngine(String strategySource, String dataPath, double initialCash) {
        Lexer lexer = new Lexer(strategySource);
        Parser parser = new Parser(lexer.scanTokens());
        Program program = parser.parseProgram();
        new semantic.TypeChecker().check(program);
        this.tickVariable = program.strategy.onTick.symbol;
        if (!Double.isFinite(initialCash) || initialCash < 0) throw new IllegalArgumentException("Invalid initial cash");
        this.initialCash = initialCash;
        
        Compiler compiler = new Compiler();
        this.chunk = compiler.compile(program);
        
        this.vm = new VirtualMachine();
        this.vm.init(this.chunk);
        this.portfolio = new Portfolio(initialCash);
        this.data = CSVLoader.load(dataPath);
        if (data.isEmpty()) throw new IllegalArgumentException("CSV has no candles");
    }

    public void run() {
        if (ran) throw new IllegalStateException("Create a new engine for each replay");
        ran = true;
        int trades = 0;
        java.util.ArrayDeque<Double> window = new java.util.ArrayDeque<>();
        double sumPrices = 0;
        int count = 0;
        
        for (OHLCV candle : data) {
            double currentPrice = candle.close;
            sumPrices += currentPrice;
            window.addLast(currentPrice);
            if (window.size() > 10) sumPrices -= window.removeFirst();
            count = window.size();
            
            // Simple builtin simulations
            vm.setGlobal(tickVariable, currentPrice);
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
        System.out.println("Profit and Loss: $" + String.format("%.2f", finalValue - initialCash));
    }
}
