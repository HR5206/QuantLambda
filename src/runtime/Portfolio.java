package runtime;

public class Portfolio {
    public double cash;
    public double position;

    public Portfolio(double initialCash) {
        this.cash = initialCash;
        this.position = 0;
    }

    public void executeTrade(double quantity, double price) {
        position += quantity;
        cash -= quantity * price;
    }

    public double getPortfolioValue(double currentPrice) {
        return cash + (position * currentPrice);
    }
}
