package semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public final SymbolTable parent;
    private final Map<String, String> symbols = new HashMap<>();

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public void define(String name, String type) {
        if (symbols.containsKey(name)) {
            throw new RuntimeException("Variable '" + name + "' already defined in this scope.");
        }
        symbols.put(name, type);
    }

    public String resolve(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }
}
