package de.hhu.lirem101.quil_optimizer.quantum_gates;

import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;

import static de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState.*;

public class PauliY implements QuantumCliffordGate {

    @Override
    public QuantumCliffordState apply(QuantumCliffordState state) {
        switch (state) {
            case X_POSITIVE:
                return X_NEGATIVE;
            case X_NEGATIVE:
                return X_POSITIVE;
            case Y_POSITIVE:
                return Y_POSITIVE;
            case Y_NEGATIVE:
                return Y_NEGATIVE;
            case Z_POSITIVE:
                return Z_NEGATIVE;
            case Z_NEGATIVE:
                return Z_POSITIVE;
            default:
                throw new IllegalArgumentException("Invalid state");
        }
    }
}
