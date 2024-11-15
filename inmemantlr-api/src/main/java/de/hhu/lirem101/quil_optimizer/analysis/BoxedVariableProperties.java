package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import org.apache.commons.numbers.complex.Complex;

public class BoxedVariableProperties {
    public boolean isQuantum;
    public int line;
    public String name;
    public QuantumCliffordState constantQuantumState;
    public Complex constantValue;
    public boolean assignment;

    public BoxedVariableProperties(String name, int line, boolean isQuantum, boolean assignment) {
        this.isQuantum = isQuantum;
        this.line = line;
        this.name = name;
        this.assignment = assignment;
    }

    public BoxedVariableProperties(String name, int line, QuantumCliffordState qcs) {
        this.name = name;
        this.line = line;
        this.constantQuantumState = qcs;
        this.isQuantum = true;
        this.assignment = true;
    }

    public BoxedVariableProperties(String name, int line, double constantValue, boolean assignment) {
        this.name = name;
        this.line = line;
        this.constantValue = Complex.ofCartesian(constantValue, 0);
        this.isQuantum = false;
        this.assignment = assignment;
    }

    public BoxedVariableProperties(String name, int line, Complex constantValue, boolean assignment) {
        this.name = name;
        this.line = line;
        this.constantValue = constantValue;
        this.isQuantum = false;
        this.assignment = assignment;
    }
}
