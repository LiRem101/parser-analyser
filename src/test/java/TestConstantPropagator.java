import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.analysis.BoxedVariableProperties;
import de.hhu.lirem101.quil_optimizer.analysis.ConstantPropagator;
import de.hhu.lirem101.quil_optimizer.quil_variable.*;
import org.apache.commons.numbers.complex.Complex;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestConstantPropagator {

    @Test
    void returnsEmptyNewConstantValuesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ConstantPropagator propagator = new ConstantPropagator(instructions);

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertTrue(result.isEmpty());
    }

    @Test
    void propagatesClassicalConstantsCorrectly() {
        InstructionNode instruction1 = mock(InstructionNode.class);
        InstructionNode instruction2 = mock(InstructionNode.class);
        ClassicalVariable classicalVariableAssignment = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        ClassicalVariable classicalVariableUsage = new ClassicalVariable("c1", ClassicalUsage.USAGE);
        classicalVariableAssignment.setValue(Complex.ofCartesian(1, 0));
        when(instruction1.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariableAssignment)));
        when(instruction2.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariableUsage)));
        ArrayList<InstructionNode> instructions = new ArrayList<>();
        instructions.add(instruction1);
        instructions.add(instruction2);
        ConstantPropagator propagator = new ConstantPropagator(new ArrayList<>(Collections.singletonList(instructions)));

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("c1", result.get(0).get(0).name);
        assertEquals(Complex.ofCartesian(1, 0), result.get(0).get(0).constantValue);
    }

    @Test
    void propagatesQuantumConstantsCorrectlyWithConstantValue() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable = new QuantumVariable("q1", QuantumUsage.SINGLE_GATE);
        quantumVariable.setCliffordStateBeforeGate(QuantumCliffordState.X_POSITIVE);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        ConstantPropagator propagator = new ConstantPropagator(instructions);

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isEmpty());
    }

    @Test
    void propagatesQuantumConstantsCorrectlyWithoutConstantValue() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable = new QuantumVariable("q1", QuantumUsage.SINGLE_GATE);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        ConstantPropagator propagator = new ConstantPropagator(instructions);

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("q1", result.get(0).get(0).name);
        assertEquals(QuantumCliffordState.X_POSITIVE, result.get(0).get(0).constantQuantumState);
    }
}