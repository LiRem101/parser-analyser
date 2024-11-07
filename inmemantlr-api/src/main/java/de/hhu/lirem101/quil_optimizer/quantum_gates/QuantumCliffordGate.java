package de.hhu.lirem101.quil_optimizer.quantum_gates;

import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;

public interface QuantumCliffordGate {

    public QuantumCliffordState apply(QuantumCliffordState state);

}
