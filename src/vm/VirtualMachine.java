package vm;

import bytecode.Chunk;
import bytecode.Opcode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class VirtualMachine {
    private Chunk chunk;
    private int ip;
    private final Stack<Object> stack = new Stack<>();
    private final Map<String, Object> globals = new HashMap<>();

    public double position = 0.0;

    public void init(Chunk chunk) {
        this.chunk = chunk;
        this.ip = 0;
        this.stack.clear();
    }

    public void setGlobal(String name, Object value) {
        globals.put(name, value);
    }

    public void run() {
        while (ip < chunk.getCount()) {
            Opcode instruction = chunk.code.get(ip);
            Object operand = chunk.operands.get(ip);
            ip++;

            switch (instruction) {
                case LOAD_CONST:
                    stack.push(chunk.constants.get((Integer) operand));
                    break;
                case LOAD_VAR:
                    stack.push(globals.get((String) operand));
                    break;
                case STORE_VAR:
                    globals.put((String) operand, stack.pop());
                    break;
                case ADD: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a + b);
                    break;
                }
                case SUB: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a - b);
                    break;
                }
                case MUL: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a * b);
                    break;
                }
                case DIV: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a / b);
                    break;
                }
                case NEGATE: {
                    stack.push(-popDouble());
                    break;
                }
                case GREATER: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a > b);
                    break;
                }
                case LESS: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a < b);
                    break;
                }
                case GREATER_EQUAL: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a >= b);
                    break;
                }
                case LESS_EQUAL: {
                    double b = popDouble();
                    double a = popDouble();
                    stack.push(a <= b);
                    break;
                }
                case EQUAL: {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(a.equals(b));
                    break;
                }
                case NOT_EQUAL: {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(!a.equals(b));
                    break;
                }
                case NOT: {
                    boolean a = (Boolean) stack.pop();
                    stack.push(!a);
                    break;
                }
                case AND: {
                    boolean b = (Boolean) stack.pop();
                    boolean a = (Boolean) stack.pop();
                    stack.push(a && b);
                    break;
                }
                case OR: {
                    boolean b = (Boolean) stack.pop();
                    boolean a = (Boolean) stack.pop();
                    stack.push(a || b);
                    break;
                }
                case JUMP: {
                    ip = (Integer) operand;
                    break;
                }
                case JUMP_IF_FALSE: {
                    boolean condition = (Boolean) stack.pop();
                    if (!condition) {
                        ip = (Integer) operand;
                    }
                    break;
                }
                case CALL: {
                    Object[] op = (Object[]) operand;
                    String callee = (String) op[0];
                    // Very simple dummy call for now
                    stack.push(0.0);
                    break;
                }
                case BUY: {
                    position += popDouble();
                    break;
                }
                case SELL: {
                    position -= popDouble();
                    break;
                }
                case HALT: {
                    return;
                }
                case RETURN: {
                    return;
                }
            }
        }
    }

    private double popDouble() {
        Object val = stack.pop();
        if (val instanceof Integer) {
            return ((Integer) val).doubleValue();
        }
        return (Double) val;
    }
}
