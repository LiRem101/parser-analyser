import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.analysis.BoxedVariableProperties;
import de.hhu.lirem101.quil_optimizer.analysis.LiveVariableAnalyser;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestLiveVariableAnalyser {

    @Test
    void returnsEmptyVariablesSetToDeadWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesDeadVariablesCorrectlyForSingleInstruction() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVariable = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("c1", result.get(0).get(0).name);
    }

    @Test
    void handlesReadoutVariablesCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVariable = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>(Collections.singletonList("c1"));
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertTrue(result.get(0).isEmpty());
    }

    @Test
    void calculatesDeadQuantumVariablesCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable = new QuantumVariable("q1", QuantumUsage.SINGLE_GATE);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("q1", result.get(0).get(0).name);
    }

    @Test
    void handlesMultiQubitGatesCorrectlyDeadQubits() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable1 = new QuantumVariable("q1", QuantumUsage.MULTI_GATE);
        QuantumVariable quantumVariable2 = new QuantumVariable("q2", QuantumUsage.MULTI_GATE);
        ArrayList<QuantumVariable> quantumVariables = new ArrayList<>();
        quantumVariables.add(quantumVariable1);
        quantumVariables.add(quantumVariable2);
        when(instruction.getQuantumParameters()).thenReturn(quantumVariables);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).size());
        ArrayList<String> names = new ArrayList<>(Arrays.asList(result.get(0).get(0).name, result.get(0).get(1).name));
        assertTrue(names.contains("q1"));
        assertTrue(names.contains("q2"));
    }

    @Test
    void handlesMultiQubitGatesCorrectly() {
        InstructionNode multiGateInstruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable1 = new QuantumVariable("q1", QuantumUsage.MULTI_GATE);
        QuantumVariable quantumVariable2 = new QuantumVariable("q2", QuantumUsage.MULTI_GATE);
        ArrayList<QuantumVariable> quantumVariables = new ArrayList<>();
        quantumVariables.add(quantumVariable1);
        quantumVariables.add(quantumVariable2);
        when(multiGateInstruction.getQuantumParameters()).thenReturn(quantumVariables);

        InstructionNode measureInstruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable3 = new QuantumVariable("q1", QuantumUsage.MEASURE);
        ClassicalVariable classicalVariable = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        when(measureInstruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable3)));
        when(measureInstruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariable)));

        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Arrays.asList(multiGateInstruction, measureInstruction))));
        Set<String> readoutVariables = new HashSet<>(Collections.singletonList("c1"));
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertTrue(result.get(0).isEmpty());
    }
}