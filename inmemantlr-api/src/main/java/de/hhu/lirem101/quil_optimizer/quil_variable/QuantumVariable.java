package de.hhu.lirem101.quil_optimizer.quil_variable;

import de.hhu.lirem101.quil_optimizer.quantum_gates.QuantumCliffordGate;

public class QuantumVariable implements Variable {
    private final String name;
    private boolean shownToBeDead = false;
    private QuantumCliffordState cliffordState = null;
    private final QuantumUsage usage;

    public QuantumVariable(String name, QuantumUsage usage) {
        this.name = name;
        this.usage = usage;
    }

    public QuantumCliffordState applyGate(QuantumCliffordGate gate) {
        return gate.apply(cliffordState);
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
        return cliffordState != null;
    }

    public void setCliffordState(QuantumCliffordState cliffordState) {
        this.cliffordState = cliffordState;
    }

    public QuantumCliffordState getCliffordState() {
        return cliffordState;
    }
}
