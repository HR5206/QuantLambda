package bytecode;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public final List<Opcode> code = new ArrayList<>();
    public final List<Object> operands = new ArrayList<>();
    public final List<Object> constants = new ArrayList<>();

    public int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }

    public void write(Opcode opcode) {
        write(opcode, null);
    }

    public void write(Opcode opcode, Object operand) {
        code.add(opcode);
        operands.add(operand);
    }

    public void patchOperand(int offset, Object operand) {
        operands.set(offset, operand);
    }

    public int getCount() {
        return code.size();
    }
}
