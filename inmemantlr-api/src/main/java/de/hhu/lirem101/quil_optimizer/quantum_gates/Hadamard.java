package de.hhu.lirem101.quil_optimizer.quantum_gates;

import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;

import static de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState.*;

public class Hadamard implements QuantumCliffordGate {

    @Override
    public QuantumCliffordState apply(QuantumCliffordState state) {
        switch (state) {
            case X_POSITIVE:
                return Z_POSITIVE;
            case X_NEGATIVE:
                return Z_NEGATIVE;
            case Y_POSITIVE:
                return Y_NEGATIVE;
            case Y_NEGATIVE:
                return Y_POSITIVE;
            case Z_POSITIVE:
                return X_POSITIVE;
            case Z_NEGATIVE:
                return X_NEGATIVE;
            default:
                throw new IllegalArgumentException("Invalid state");
        }
    }
}
