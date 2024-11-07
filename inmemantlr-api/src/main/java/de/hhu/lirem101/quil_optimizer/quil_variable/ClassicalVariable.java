package de.hhu.lirem101.quil_optimizer.quil_variable;

import de.hhu.lirem101.quil_optimizer.quantum_gates.QuantumCliffordGate;

public class ClassicalVariable implements Variable {
    private final String name;
    private boolean shownToBeDead = false;
    private boolean isConstant = false;
    private double value;
    private final ClassicalUsage usage;

    public ClassicalVariable(String name, ClassicalUsage usage) {
        this.name = name;
        this.usage = usage;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isShownToBeDead() {
        return shownToBeDead;
    }

    @Override
    public void setDead() {
        shownToBeDead = true;
    }

    @Override
    public boolean isConstant() {
        return isConstant;
    }

    public void setValue(double value) {
        this.value = value;
        isConstant = true;
    }

    public double getValue() {
        if(isConstant) {
            return value;
        } else {
            throw new RuntimeException("Value not set for variable " + name);
        }
    }
}
