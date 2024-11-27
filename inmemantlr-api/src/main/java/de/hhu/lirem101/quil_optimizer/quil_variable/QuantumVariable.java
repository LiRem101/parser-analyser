package de.hhu.lirem101.quil_optimizer.quil_variable;

import de.hhu.lirem101.quil_optimizer.quantum_gates.QuantumCliffordGate;

public class QuantumVariable implements Variable {
    private final String name;
    private boolean shownToBeDead = false;
    private QuantumCliffordState cliffordStateBeforeGate = null;
    private QuantumCliffordState cliffordStateAfterGate = null;
    private final QuantumUsage usage;

    public QuantumVariable(String name, QuantumUsage usage) {
        this.name = name;
        this.usage = usage;
    }

    public QuantumCliffordState applyGate(QuantumCliffordGate gate) {
        if(cliffordStateAfterGate != null) {
            return cliffordStateAfterGate;
        }
        cliffordStateAfterGate = gate.apply(cliffordStateBeforeGate);
        return cliffordStateAfterGate;
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
        return cliffordStateBeforeGate != null;
    }

    public QuantumUsage getUsage() {
        return usage;
    }

    public void setCliffordStateBeforeGate(QuantumCliffordState cliffordStateBeforeGate) {
        this.cliffordStateBeforeGate = cliffordStateBeforeGate;
    }

    public QuantumCliffordState getCliffordStateBeforeGate() {
        return cliffordStateBeforeGate;
    }

    public QuantumCliffordState getCliffordStateAfterGate() {
        return cliffordStateAfterGate;
    }

    public QuantumVariable copyQV() {
        QuantumVariable qv = new QuantumVariable(name, usage);
        qv.cliffordStateBeforeGate = cliffordStateBeforeGate;
        qv.cliffordStateAfterGate = cliffordStateAfterGate;
        return qv;
    }
}
