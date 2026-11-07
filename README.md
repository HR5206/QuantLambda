# QuantLambda

I use QuantLambda to study a compiler end to end: source text becomes tokens, an AST, checked expressions, bytecode, and a small stack VM. The example language expresses trading rules over a CSV replay. This is a compiler/backtesting exercise, not a trading recommendation or broker integration.

## Run and test

Use JDK 21, from the repository root (Linux/macOS or Git Bash):

```sh
mkdir -p out
find src tests -name '*.java' > sources.txt
javac -d out @sources.txt
java -cp out lexer.LexerTest
java -cp out parser.ParserTest
java -cp out semantic.SemanticTest
java -cp out vm.VMTest
java -cp out vm.RegressionTest
java -cp out backtest.Main
```

There are no external libraries. The supplied CSV is a ten-row demonstration fixture, not a documented market dataset. The two bundled rules finish at 99980.00 and 99964.00 from 100000.00 initial cash. Runtime timings include replay/output; I make no claim that the VM beats an interpreter.

```text
strategy Momentum {
  on tick(price) {
    if price > sma_10 { buy 1; } else { sell 1; }
  }
}
```

## Implemented behavior

The backtester invokes the type checker before compiling. The tick parameter's name is respected. `sma_10` is the mean of the latest ten closes including the current close; during warm-up it uses all closes seen so far.

The language supports declarations, assignment statements, nested blocks, `if`/`else`, arithmetic, comparisons, strings/booleans, and buy/sell quantities. Numeric equality compares values across integer/double literals. Division returns a floating-point value and rejects zero divisors. Logical operators short-circuit. Shadowing an existing name is rejected because the VM uses named slots rather than separate lexical frames. See [the language notes](docs/language-spec.md).

Function calls (`sma()` and `ema()` included) and `Quantity`/`Series<Price>` annotations are reserved syntax, **not implemented features**. They now fail explicitly instead of returning dummy zeroes.

## Replay assumptions

Orders are netted within each candle and filled at its closing price, after the rule reads that same close. There are no fees, slippage, latency, margin rules, or cash/short-position constraints. “Total Trades” counts candles with a nonzero net position change, not individual buy/sell statements. These simplifications prevent interpreting the example P&L as evidence of a profitable strategy.

An engine instance is single-use. CSV input must have six fields per nonblank row, finite positive OHLC prices, and nonnegative volume. Missing or empty data fails rather than producing an empty replay.

## Code and evidence

Start at `src/backtest/BacktestEngine.java`, then follow `lexer`, `parser`, `semantic`, `bytecode`, and `vm`. The original four runners test individual stages. The added regression runner exercises numeric equality, short circuiting, rolling-average behavior beyond ten rows, custom initial cash, parameter names, rejected syntax/types, and invalid data.

`ASTInterpreter` is an unfinished experimental baseline; it is not used by the backtester and is not a supported equivalent of the VM. I would establish parity and a repeatable benchmark before reporting speed comparisons. A recorded local run is more useful here than a permanently hosted backend.
