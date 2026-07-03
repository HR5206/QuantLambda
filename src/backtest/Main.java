package backtest;

public class Main {
    public static void main(String[] args) {
        String dataPath = "data/market.csv";

        String momentumSource = 
            "strategy Momentum { \n" +
            "  on tick(symbol) { \n" +
            "    if symbol > sma_10 { \n" +
            "      buy 1; \n" +
            "    } else { \n" +
            "      sell 1; \n" +
            "    } \n" +
            "  } \n" +
            "}";

        String meanReversionSource = 
            "strategy MeanReversion { \n" +
            "  on tick(symbol) { \n" +
            "    if symbol < sma_10 { \n" +
            "      buy 1; \n" +
            "    } else { \n" +
            "      sell 1; \n" +
            "    } \n" +
            "  } \n" +
            "}";

        System.out.println("======================================");
        System.out.println("Running Momentum Strategy...");
        BacktestEngine engine1 = new BacktestEngine(momentumSource, dataPath, 100000.0);
        long start1 = System.nanoTime();
        engine1.run();
        long end1 = System.nanoTime();
        System.out.println("VM Execution Time: " + (end1 - start1) / 1_000_000.0 + " ms");
        System.out.println("======================================\n");

        System.out.println("======================================");
        System.out.println("Running Mean Reversion Strategy...");
        BacktestEngine engine2 = new BacktestEngine(meanReversionSource, dataPath, 100000.0);
        long start2 = System.nanoTime();
        engine2.run();
        long end2 = System.nanoTime();
        System.out.println("VM Execution Time: " + (end2 - start2) / 1_000_000.0 + " ms");
        System.out.println("======================================");
    }
}
