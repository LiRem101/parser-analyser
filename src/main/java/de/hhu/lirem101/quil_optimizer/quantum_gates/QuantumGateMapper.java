package de.hhu.lirem101.quil_optimizer.quantum_gates;

import java.util.HashMap;
import java.util.Map;

public class QuantumGateMapper {

    public static Map<String, QuantumCliffordGate> getQuantumGateMap() {
        Map<String, QuantumCliffordGate> quantumGateMap = new HashMap<>();
        quantumGateMap.put("I", new Identity());
        quantumGateMap.put("X", new PauliX());
        quantumGateMap.put("Y", new PauliY());
        quantumGateMap.put("Z", new PauliZ());
        quantumGateMap.put("H", new Hadamard());
        quantumGateMap.put("S", new SGate());
        return quantumGateMap;
    }

}
