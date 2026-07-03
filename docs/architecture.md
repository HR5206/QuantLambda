# QUANT-λ Architecture

## Overview
QUANT-λ is a Domain-Specific Language (DSL) tailored for quantitative algorithmic trading. 
It follows a standard multi-pass compiler architecture culminating in a stack-based virtual machine designed to be embedded in a historical event-driven backtesting engine.

## Compiler Pipeline

1. **Source Code**: Written in `.qlx` containing `strategy` declarations.
2. **Lexer**: A handwritten scanner converting the raw string into a stream of `Token`s. It tracks line and column numbers for error reporting.
3. **Parser**: A Pratt (Top-Down Operator Precedence) Parser parsing tokens into an Abstract Syntax Tree (AST). It uses recursive descent for statements and Pratt parsing for expressions to easily handle operator precedence.
4. **Type Checker**: A static semantic analysis phase using a `SymbolTable`. It enforces lexical scoping, resolves identifiers, and performs type checking across numeric, boolean, and trading-specific primitives (`Price`, `Quantity`).
5. **Bytecode Compiler**: Walks the validated AST and emits instructions for a custom stack-based VM. Emits instances of `Chunk` containing `Opcode` instructions, inline operands, and a constant pool.
6. **Virtual Machine**: A stack-based execution engine (`VirtualMachine`). It maps identifiers to a global environment state and executes `Chunk` instructions sequentially. It handles core arithmetic, branching (`JUMP_IF_FALSE`), and native domain operations (`BUY`, `SELL`).
7. **Trading Runtime**: Supplies market data via `OHLCV` candle objects parsed from CSV by `CSVLoader`. It injects global variables (e.g. `symbol`, `sma_10`) into the VM's environment.
8. **Backtest Engine**: Iterates over market data, triggers the VM execution (`on tick`), and manages the virtual `Portfolio` to track positions, cash, and PnL.

## Design Decisions
- **Stack-based VM vs AST Interpreter**: The project implements both an AST Interpreter (as a baseline) and a Bytecode VM. The VM avoids tree-walking overhead, packing operations sequentially, which provides a significant speedup for repetitive tick-by-tick evaluations.
- **Pratt Parsing**: Simplifies expression parsing significantly over traditional recursive descent by associating parsing functions directly with token types.
- **Custom Lexer**: Built from scratch to maintain complete control over the emitted tokens and avoid external dependencies (e.g., ANTLR).

## Future Optimizations
- **Tracing JIT**: The current dispatch loop uses a large switch statement in Java. For a production system, a JIT compiler could compile hot execution paths directly to native machine code.
- **Garbage Collection**: Currently relies on the JVM. A dedicated implementation might use an arena allocator optimized for short-lived per-tick variables.
- **Inline Caching**: To accelerate global variable lookups in the VM's environment map.
