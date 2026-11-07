# Current language scope

The supported entry point is the checked bytecode pipeline in `BacktestEngine`.

Supported annotations are `int`, `double`, `boolean`, `string`, and `Price`. Numeric arithmetic uses VM double values; integer annotations are static categories, not overflow-checked integer storage. Division has type double. String concatenation is not supported. Equality compares numeric values across literal types; other values use value equality.

Declarations initialize on every tick. Assignment is a statement, not a general expression. Blocks provide static scope; redeclaring an existing visible name is rejected because runtime slots are flat. `&&` and `||` short-circuit. Functions, loops, series values, and `Quantity` annotations are not implemented. The lexer/parser retain some reserved syntax, which semantic checking rejects.

`on tick(name)` binds `name` to the current close. `sma_10` contains a rolling mean of up to ten closes including this candle. `buy` and `sell` require finite nonnegative quantities. Orders net within a candle and change an unrestricted simulated cash/position balance at the same close. There is no persistent user-defined state across ticks promised by this language.

See README.md for runnable examples and replay assumptions. The AST interpreter is experimental and not a supported execution path.
