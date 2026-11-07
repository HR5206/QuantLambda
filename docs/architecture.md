# Execution path

`BacktestEngine` runs Lexer → Pratt Parser → TypeChecker → Compiler once, then resets the VM instruction pointer for each CSV candle. The engine supplies the tick variable and rolling average, computes the change in VM position, and applies that net change to the simulated portfolio.

`ASTInterpreter` is an unfinished comparison experiment, not part of this path. The implementation makes no speedup claim. Replay timing includes printing and is not a VM benchmark.

The VM has flat named slots. The checker rejects shadowing to keep static bindings consistent with runtime storage. A future lexical-slot compiler should be covered by nested-scope tests before lifting this restriction.
