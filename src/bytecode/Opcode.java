package bytecode;

public enum Opcode {
    LOAD_CONST,
    LOAD_VAR,
    STORE_VAR,
    ADD, SUB, MUL, DIV,
    NEGATE,
    GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, EQUAL, NOT_EQUAL,
    NOT, AND, OR,
    JUMP,
    JUMP_IF_FALSE,
    CALL,
    BUY,
    SELL,
    RETURN,
    HALT
}
