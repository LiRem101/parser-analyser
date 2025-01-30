package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import org.apache.commons.numbers.complex.Complex;

/**
 * This class is used to store properties of a boxed variable.
 * A boxed variable is a variable that is used in a Quil program at a defined line.
 * The properties of a boxed variable are the name of the variable, the line number where the variable is used,
 * if the variable is a quantum variable, if the variable is assigned a value and the value of the variable.
 */
public class BoxedVariableProperties {
    public boolean isQuantum;
    public int line;
    public String name;
    public QuantumCliffordState constantQuantumState;
    public Complex constantValue;
    public boolean assignment;

    /**
     * Constructor for a boxed variable at a defined line.
     * @param name The name of the variable.
     * @param line The line number where the variable is used.
     * @param isQuantum True if the variable is a quantum variable.
     * @param assignment True if the variable is assigned a value and not only used.
     */
    public BoxedVariableProperties(String name, int line, boolean isQuantum, boolean assignment) {
        this.isQuantum = isQuantum;
        this.line = line;
        this.name = name;
        this.assignment = assignment;
    }

    /**
     * Constructor for a boxed quantum variable at a defined line with a defined quantum state.
     * @param name The name of the variable.
     * @param line The line number where the variable is used.
     * @param qcs The quantum state of the variable.
     */
    public BoxedVariableProperties(String name, int line, QuantumCliffordState qcs) {
        this.name = name;
        this.line = line;
        this.constantQuantumState = qcs;
        this.isQuantum = true;
        this.assignment = true;
    }

    /**
     * Constructor for a boxed variable at a defined line with a real value.
     * @param name The name of the variable.
     * @param line The line number where the variable is used.
     * @param constantValue The real value of the variable.
     * @param assignment True if the variable is assigned a value and not only used.
     */
    public BoxedVariableProperties(String name, int line, double constantValue, boolean assignment) {
        this.name = name;
        this.line = line;
        this.constantValue = Complex.ofCartesian(constantValue, 0);
        this.isQuantum = false;
        this.assignment = assignment;
    }

    /**
     * Constructor for a boxed variable at a defined line with a complex value.
     * @param name The name of the variable.
     * @param line The line number where the variable is used.
     * @param constantValue The complex value of the variable.
     * @param assignment True if the variable is assigned a value and not only used.
     */
    public BoxedVariableProperties(String name, int line, Complex constantValue, boolean assignment) {
        this.name = name;
        this.line = line;
        this.constantValue = constantValue;
        this.isQuantum = false;
        this.assignment = assignment;
    }
}
