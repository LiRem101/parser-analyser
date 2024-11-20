import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.ReOrdererForHybridExecution;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestReOrdererForHybridExecution {

    @Test
    void reOrdersInstructionsCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        when(node1.getLineType()).thenReturn(LineType.CLASSICAL);
        when(node2.getLineType()).thenReturn(LineType.QUANTUM_INFLUENCES_CLASSICAL);
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node2);
        instructionList.add(node1);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(instructionList));
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        hybridDependencies.put(2, new HashSet<>(Collections.singletonList(1)));
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependenciesList = new ArrayList<>(Collections.singletonList(hybridDependencies));
        ReOrdererForHybridExecution reOrderer = new ReOrdererForHybridExecution(instructions, hybridDependenciesList);

        ArrayList<ArrayList<Integer>> result = reOrderer.reOrderInstructions();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get(0));
        assertEquals(2, result.get(0).get(1));
    }

    @Test
    void handlesEmptyInstructionsList() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencies = new ArrayList<>();
        ReOrdererForHybridExecution reOrderer = new ReOrdererForHybridExecution(instructions, hybridDependencies);

        ArrayList<ArrayList<Integer>> result = reOrderer.reOrderInstructions();

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesNoHybridDependencies() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        when(node1.getLineType()).thenReturn(LineType.QUANTUM);
        when(node2.getLineType()).thenReturn(LineType.QUANTUM);
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node1);
        instructionList.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(instructionList));
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependenciesList = new ArrayList<>(Collections.singletonList(hybridDependencies));
        ReOrdererForHybridExecution reOrderer = new ReOrdererForHybridExecution(instructions, hybridDependenciesList);

        ArrayList<ArrayList<Integer>> result = reOrderer.reOrderInstructions();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(1, result.get(0).get(0));
        assertEquals(2, result.get(0).get(1));
    }

    @Test
    void handlesSingleInstructionList() {
        InstructionNode node = mock(InstructionNode.class);
        when(node.getLine()).thenReturn(1);
        when(node.getLineType()).thenReturn(LineType.CLASSICAL);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(
                new ArrayList<>(Collections.singletonList(node))
        ));
        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        hybridDependencies.put(1, Collections.emptySet());
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependenciesList = new ArrayList<>(Collections.singletonList(hybridDependencies));
        ReOrdererForHybridExecution reOrderer = new ReOrdererForHybridExecution(instructions, hybridDependenciesList);

        ArrayList<ArrayList<Integer>> result = reOrderer.reOrderInstructions();

        assertEquals(1, result.size());
        assertEquals(Collections.singletonList(1), result.get(0));
    }

    @Test
    void handlesMultipleHybridDependencies() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        InstructionNode node3 = mock(InstructionNode.class);
        InstructionNode node4 = mock(InstructionNode.class);
        InstructionNode node5 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        when(node3.getLine()).thenReturn(3);
        when(node4.getLine()).thenReturn(4);
        when(node5.getLine()).thenReturn(5);
        when(node1.getDependencies()).thenReturn(Collections.emptySet());
        when(node2.getDependencies()).thenReturn(Collections.emptySet());
        Set<InstructionNode> node3Dependencies = new HashSet<>();
        node3Dependencies.add(node1);
        node3Dependencies.add(node2);
        when(node3.getDependencies()).thenReturn(node3Dependencies);
        when(node4.getDependencies()).thenReturn(Collections.emptySet());
        when(node5.getDependencies()).thenReturn(new HashSet<>(Collections.singletonList(node4)));
        when(node1.getLineType()).thenReturn(LineType.CLASSICAL);
        when(node2.getLineType()).thenReturn(LineType.CLASSICAL);
        when(node3.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(node4.getLineType()).thenReturn(LineType.QUANTUM);
        when(node5.getLineType()).thenReturn(LineType.QUANTUM_INFLUENCES_CLASSICAL);
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node1);
        instructionList.add(node2);
        instructionList.add(node3);
        instructionList.add(node4);
        instructionList.add(node5);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(instructionList));

        LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
        Set<Integer> dependenciesOf3 = new HashSet<>();
        dependenciesOf3.add(1);
        dependenciesOf3.add(2);
        hybridDependencies.put(3, dependenciesOf3);
        Set<Integer> dependenciesOf5 = new HashSet<>();
        dependenciesOf5.add(4);
        hybridDependencies.put(5, dependenciesOf5);
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependenciesList = new ArrayList<>(Collections.singletonList(hybridDependencies));
        ReOrdererForHybridExecution reOrderer = new ReOrdererForHybridExecution(instructions, hybridDependenciesList);

        ArrayList<ArrayList<Integer>> result = reOrderer.reOrderInstructions();

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).size());
        assertEquals(1, result.get(0).get(0));
        assertEquals(2, result.get(0).get(1));
        assertEquals(4, result.get(0).get(2));
        assertEquals(3, result.get(0).get(3));
        assertEquals(5, result.get(0).get(4));
    }
}