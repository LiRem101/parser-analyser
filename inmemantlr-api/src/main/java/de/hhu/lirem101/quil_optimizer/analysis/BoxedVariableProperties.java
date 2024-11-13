package de.hhu.lirem101.quil_optimizer.analysis;

public class BoxedVariableProperties {
    public boolean isQuantum;
    public int line;
    public String name;

    public BoxedVariableProperties(String name, int line, boolean isQuantum) {
        this.isQuantum = isQuantum;
        this.line = line;
        this.name = name;
    }
}
