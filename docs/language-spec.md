# QUANT-λ Language Specification

## Overview
QUANT-λ is a statically typed domain-specific language (DSL) designed for algorithmic trading. It is built to express trading strategies clearly and concisely. The language features static typing, basic control flow, arithmetic, and domain-specific operations for interacting with market data.

## Features
- **Statically Typed**: Variables are bound to types at compile time.
- **Domain Specific Types**: Native support for `Price`, `Quantity`, and `Series<Price>`.
- **Tick-based Execution**: Strategies execute inside an `on tick` event loop triggered by a trading runtime.
- **Built-in Indicators**: Native support for `sma()` and `ema()` calculations.
- **Order Execution**: Built-in `buy` and `sell` statements for interacting with the portfolio.

## Primitive Types
- `int`: 32-bit signed integer.
- `double`: 64-bit floating point.
- `boolean`: `true` or `false`.
- `string`: Sequence of characters.

## Trading Types
- `Price`: Represents a market price.
- `Quantity`: Represents an asset quantity.
- `Series<Price>`: A time-series collection of prices used for indicator calculations.

## Variables
Variables are declared using the `let` keyword followed by the identifier, an optional type annotation, and an initializer.
```
let fastLength: int = 10;
let isTrend: boolean = true;
```

## Control Flow
The language supports `if` and `else` for conditional execution.
```
if isTrend {
    buy 10;
} else {
    sell 10;
}
```

## Trading Operations
Trading is handled via native statements:
- `buy <expression>;`
- `sell <expression>;`

## Built-in Functions
- `sma(series, period)`: Simple Moving Average.
- `ema(series, period)`: Exponential Moving Average.

## Operators
- **Arithmetic**: `+`, `-`, `*`, `/`
- **Comparison**: `==`, `!=`, `>`, `<`, `>=`, `<=`
- **Logical**: `&&`, `||`, `!`
- **Assignment**: `=`

## Strategy Structure
A script defines a `strategy` block that contains an `on tick` block. The `on tick` block is executed once for every market tick (or candle).
```
strategy Momentum {
    on tick(symbol) {
        let currentPrice = symbol;
        if currentPrice > 100 {
            buy 1;
        }
    }
}
```
