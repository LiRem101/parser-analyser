package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import org.apache.commons.numbers.complex.Complex;

public class BoxedVariableProperties {
    public boolean isQuantum;
    public int line;
    public String name;
    public QuantumCliffordState constantQuantumState;
    public Complex constantValue;

    public BoxedVariableProperties(String name, int line, boolean isQuantum) {
        this.isQuantum = isQuantum;
        this.line = line;
        this.name = name;
    }

    public BoxedVariableProperties(String name, int line, QuantumCliffordState qcs) {
        this.name = name;
        this.line = line;
        this.constantQuantumState = qcs;
        this.isQuantum = true;
    }

    public BoxedVariableProperties(String name, int line, double constantValue) {
        this.name = name;
        this.line = line;
        this.constantValue = Complex.ofCartesian(constantValue, 0);
        this.isQuantum = false;
    }

    public BoxedVariableProperties(String name, int line, Complex constantValue) {
        this.name = name;
        this.line = line;
        this.constantValue = constantValue;
        this.isQuantum = false;
    }
}
