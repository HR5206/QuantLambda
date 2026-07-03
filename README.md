# QUANT-λ

QUANT-λ is a statically typed Domain-Specific Language (DSL) and execution engine built entirely from scratch in Java to express and simulate quantitative trading strategies. Designed as a functional MVP for a Goldman Sachs Summer Analyst application, this project models a complete compiler and runtime pipeline. It solves the problem of writing algorithmic trading logic in a readable, specialized language while safely translating it into fast, tick-by-tick execution bytecode for historical backtesting.

## Motivation

Domain-Specific Languages are useful in algorithmic trading because they provide targeted abstractions—such as native price types, built-in indicators, and simplified order execution—reducing boilerplate and cognitive load for quant researchers. Briefly, implementing a compiler pipeline instead of a simple interpreter allows strategies to be statically checked for type safety ahead of time, preventing costly runtime errors. Furthermore, compiling to custom bytecode enables a stack-based virtual machine to execute tick-by-tick market simulations much faster than repeatedly walking an Abstract Syntax Tree.

## Architecture

```text
       Source (.qlx)
            ↓
          Lexer
            ↓
          Tokens
            ↓
       Pratt Parser
            ↓
           AST
            ↓
   Static Type Checker
            ↓
    Bytecode Compiler
            ↓
Stack-Based Virtual Machine
            ↓
      Trading Runtime
            ↓
    Backtesting Engine
```

## Compiler Pipeline

* **Lexer**: A handwritten scanner that reads raw source code character by character, emitting a sequence of valid language tokens while tracking line and column numbers.
* **Parser**: A Pratt (Top-Down Operator Precedence) parser that transforms the flat token stream into a hierarchical Abstract Syntax Tree (AST), gracefully handling arithmetic operator precedence.
* **AST**: A collection of node classes representing the structural syntax of the language, connected via the Visitor pattern.
* **Semantic Analysis**: A static type checker that traverses the AST using a scoped Symbol Table to enforce type constraints (e.g., ensuring boolean conditions for `if` statements).
* **Bytecode Compiler**: Walks the validated AST and emits a flat array of custom instructions (opcodes) and constants, preparing the strategy for the VM.
* **Virtual Machine**: A stack-based execution engine that sequentially evaluates the compiled opcodes against a runtime environment containing global variables and an operand stack.
* **Runtime**: Simulates the market environment by injecting current asset prices and indicator values into the VM on every simulated market tick.
* **Backtester**: Iterates through historical OHLCV data, triggers the VM execution per candle, and updates a virtual portfolio to calculate final Profit and Loss (PnL).

## Language Features

* **Static Typing**: Primitive types (`int`, `double`, `boolean`, `string`) and specialized trading types (`Price`, `Quantity`, `Series<Price>`).
* **Control Flow**: `if` and `else` blocks.
* **Trading Instructions**: Built-in `buy` and `sell` statements.
* **Indicators**: Access to globally injected indicators like `sma_10`.
* **Strategy Definition**: `strategy` blocks containing an `on tick` execution loop.

### Sample Strategy

```
strategy Momentum { 
  on tick(symbol) {
    let x: int = 10;
    if symbol > sma_10 { 
      buy 1; 
    } else { 
      sell 1; 
    } 
  } 
}
```

## Example Compilation Flow

**Source**
```text
let x: int = 10;
```

**Tokens**
```text
[LET, IDENTIFIER("x"), COLON, TYPE_INT("int"), ASSIGN, NUMBER_LITERAL(10), SEMICOLON]
```

**AST**
```text
VariableDeclaration
  Name: "x"
  Type: "int"
  Initializer: LiteralExpression(10)
```

**Bytecode**
```text
LOAD_CONST 0  // Loads 10
STORE_VAR "x"
```

**Execution**
The VM pushes `10` to the stack, then pops `10` and stores it in the global environment map under the key `"x"`.

## Bytecode Example

For the expression `5 + 3`:

```text
LOAD_CONST 0  // 5
LOAD_CONST 1  // 3
ADD
```

## Runtime

The trading runtime drives the execution of the Virtual Machine using historical data.
* **Event-Driven Execution**: Strategies execute solely within the `on tick(symbol)` block, simulating an event-driven architecture.
* **CSV Replay**: Market data is parsed from static CSV files.
* **OHLCV Processing**: Parses timestamps, open, high, low, close, and volume, forwarding the close price to the VM.
* **Portfolio**: Tracks virtual cash and current asset position.
* **Order Execution**: Processed at the end of each market tick by evaluating the accumulated net position changes tracked by the VM.
* **PnL Calculation**: Final portfolio value is calculated based on remaining cash plus open positions evaluated at the final market price.

## Backtesting

The backtester integrates the runtime, data loading, and the VM to simulate historical trading.
* **Historical Data Replay**: Loads a dataset of historical market ticks and replays them sequentially.
* **Momentum Strategy**: Implemented in QUANT-λ to buy when the price is above the moving average and sell when below.
* **Mean Reversion Strategy**: Implemented to buy when the price drops below the moving average and sell when it rises above.
* **Execution Flow**: For each historical tick, the engine updates global indicator values (e.g., `sma_10`), resets the VM instruction pointer, evaluates the strategy, tracks any emitted orders, and updates the portfolio.

## Project Structure

```text
quant-lambda/
├── data/           # Historical market data (CSV)
├── docs/           # Grammar, language spec, architecture documentation
├── src/
│   ├── ast/        # AST node definitions and Visitor interface
│   ├── backtest/   # Backtesting engine and benchmarking main class
│   ├── bytecode/   # Opcodes, Chunk, and Compiler
│   ├── lexer/      # Handwritten scanner, Token, and TokenType
│   ├── parser/     # Pratt parser implementation
│   ├── runtime/    # Portfolio, CSVLoader, and OHLCV models
│   ├── semantic/   # SymbolTable and TypeChecker
│   └── vm/         # Stack-based VM and AST baseline interpreter
├── tests/          # Unit tests for lexical, parsing, semantic, and VM stages
└── README.md
```

## Build

### Prerequisites
* Java 21 or higher. No external dependencies are required.

### Compile Command
```bash
javac -d out src/lexer/*.java src/ast/*.java src/parser/*.java src/semantic/*.java src/bytecode/*.java src/vm/*.java src/runtime/*.java src/backtest/*.java
```

### Run Command
```bash
java -cp out backtest.Main
```

## Example Output

```text
======================================
Running Momentum Strategy...
Final Portfolio Value: $99980.00
Total Trades: 10
Profit and Loss: $-20.00
VM Execution Time: 37.8521 ms
======================================

======================================
Running Mean Reversion Strategy...
Final Portfolio Value: $99964.00
Total Trades: 10
Profit and Loss: $-36.00
VM Execution Time: 0.3528 ms
======================================
```
*(Note: Execution times may vary slightly based on hardware and JVM warm-up.)*

## Learning Outcomes

This project demonstrates core fundamentals in systems and financial engineering:
* Compiler Construction
* Pratt Parsing
* Abstract Syntax Trees
* Static Semantic Analysis
* Stack-Based Virtual Machines
* Domain-Specific Languages
* Event-Driven Systems
* Trading System Design

## Future Improvements

* User-defined functions
* Loop support
* Additional indicators
* More order types

## Design Philosophy

This project intentionally focuses on a Minimum Viable Product (MVP) rather than attempting to implement an exhaustive compiler. The goal was to build a complete, end-to-end understanding of the compiler pipeline—from tokenizing raw source code to evaluating custom bytecode against a historical financial simulation. Features were kept small and modular to ensure that every class remains highly readable, strictly object-oriented, and straightforward to explain during a technical review.
