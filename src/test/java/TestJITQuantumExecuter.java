import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.JITQuantumExecuter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestJITQuantumExecuter {

    @Test
    void reordersInstructionsCorrectlyWhenHybridDependenciesExist() {
        InstructionNode node1 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node1.getLineType()).thenReturn(LineType.CLASSICAL);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node2.getLine()).thenReturn(2);
        when(node2.getLineType()).thenReturn(LineType.QUANTUM);
        InstructionNode node3 = mock(InstructionNode.class);
        when(node3.getLine()).thenReturn(3);
        when(node3.getLineType()).thenReturn(LineType.CLASSICAL);
        InstructionNode node4 = mock(InstructionNode.class);
        when(node4.getLine()).thenReturn(4);
        when(node4.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(node4.getDependencies()).thenReturn(new HashSet<>(Collections.singletonList(node2)));

        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        hybridDependencies.put(4, new HashSet<>(Collections.singletonList(2)));
        ArrayList<InstructionNode> instructions = new ArrayList<>();
        instructions.add(node1);
        instructions.add(node2);
        instructions.add(node3);
        instructions.add(node4);
        JITQuantumExecuter executer = new JITQuantumExecuter(hybridDependencies, instructions);

        ArrayList<InstructionNode> result = executer.reorderInstructions();

        assertEquals(4, result.size());
        assertEquals(node1, result.get(0));
        assertEquals(node3, result.get(1));
        assertEquals(node2, result.get(2));
        assertEquals(node4, result.get(3));
    }

    @Test
    void returnsEmptyListWhenNoHybridDependencies() {
        InstructionNode node1 = new InstructionNode(1, LineType.CLASSICAL);
        InstructionNode node2 = new InstructionNode(2, LineType.QUANTUM);
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        ArrayList<InstructionNode> instructions = new ArrayList<>();
        instructions.add(node1);
        instructions.add(node2);
        JITQuantumExecuter executer = new JITQuantumExecuter(hybridDependencies, instructions);

        ArrayList<InstructionNode> result = executer.reorderInstructions();

        assertTrue(result.isEmpty());
    }

    @Test
    void doesNotReorderWhenAlreadyCalculated() {
        InstructionNode node1 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node1.getLineType()).thenReturn(LineType.CLASSICAL);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node2.getLine()).thenReturn(2);
        when(node2.getLineType()).thenReturn(LineType.CLASSICAL);
        InstructionNode node3 = mock(InstructionNode.class);
        when(node3.getLine()).thenReturn(3);
        when(node3.getLineType()).thenReturn(LineType.QUANTUM);
        InstructionNode node4 = mock(InstructionNode.class);
        when(node4.getLine()).thenReturn(4);
        when(node4.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(node4.getDependencies()).thenReturn(new HashSet<>(Collections.singletonList(node3)));
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        hybridDependencies.put(4, new HashSet<>(Collections.singletonList(3)));
        ArrayList<InstructionNode> instructions = new ArrayList<>();
        instructions.add(node1);
        instructions.add(node2);
        instructions.add(node3);
        instructions.add(node4);
        JITQuantumExecuter executer = new JITQuantumExecuter(hybridDependencies, instructions);

        ArrayList<InstructionNode> result = executer.reorderInstructions();

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesEmptyInstructionsList() {
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        ArrayList<InstructionNode> instructions = new ArrayList<>();
        JITQuantumExecuter executer = new JITQuantumExecuter(hybridDependencies, instructions);

        ArrayList<InstructionNode> result = executer.reorderInstructions();

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesSingleInstruction() {
        InstructionNode node = mock(InstructionNode.class);
        when(node.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        hybridDependencies.put(0, new HashSet<>());
        ArrayList<InstructionNode> instructions = new ArrayList<>(Collections.singletonList(node));
        JITQuantumExecuter executer = new JITQuantumExecuter(hybridDependencies, instructions);

        ArrayList<InstructionNode> result = executer.reorderInstructions();

        assertTrue(result.isEmpty());
    }
}