import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.analysis.FindHybridDependencies;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

class TestFindHybridDependencies {

    @Test
    void returnsEmptyHybridDependenciesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        FindHybridDependencies finder = new FindHybridDependencies(instructions);

        ArrayList<Map<Integer, Set<Integer>>> result = finder.getHybridDependencies();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesHybridDependenciesCorrectly() {
        InstructionNode hybridNode = mock(InstructionNode.class);
        when(hybridNode.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(hybridNode.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(1)));
        when(hybridNode.getDependencies()).thenReturn(new HashSet<>());
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(hybridNode))));
        FindHybridDependencies finder = new FindHybridDependencies(instructions);

        ArrayList<Map<Integer, Set<Integer>>> result = finder.getHybridDependencies();

        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey(1));
        assertTrue(result.get(0).get(1).isEmpty());
    }

    @Test
    void handlesMultipleHybridNodesCorrectly() {
        InstructionNode hybridNode1 = mock(InstructionNode.class);
        when(hybridNode1.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(hybridNode1.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(1)));
        when(hybridNode1.getDependencies()).thenReturn(new HashSet<>());
        InstructionNode hybridNode2 = mock(InstructionNode.class);
        when(hybridNode2.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(hybridNode2.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(2)));
        when(hybridNode2.getDependencies()).thenReturn(new HashSet<>());
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(hybridNode1);
        instructionList.add(hybridNode2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(instructionList));
        FindHybridDependencies finder = new FindHybridDependencies(instructions);

        ArrayList<Map<Integer, Set<Integer>>> result = finder.getHybridDependencies();

        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey(1));
        assertTrue(result.get(0).containsKey(2));
        assertTrue(result.get(0).get(1).isEmpty());
        assertTrue(result.get(0).get(2).isEmpty());
    }

    @Test
    void handlesDependenciesCorrectly() {
        InstructionNode classicalNode1 = mock(InstructionNode.class);
        when(classicalNode1.getLineType()).thenReturn(LineType.CLASSICAL);
        when(classicalNode1.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(1)));
        when(classicalNode1.getDependencies()).thenReturn(new HashSet<>());
        InstructionNode classicalNode2 = mock(InstructionNode.class);
        when(classicalNode2.getLineType()).thenReturn(LineType.CLASSICAL);
        when(classicalNode2.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(2)));
        when(classicalNode2.getDependencies()).thenReturn(new HashSet<>());
        InstructionNode hybridNode1 = mock(InstructionNode.class);
        when(hybridNode1.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(hybridNode1.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(3)));
        when(hybridNode1.getDependencies()).thenReturn(new HashSet<>(Collections.singletonList(classicalNode1)));
        InstructionNode hybridNode2 = mock(InstructionNode.class);
        when(hybridNode2.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(hybridNode2.getCodelines()).thenReturn(new ArrayList<>(Collections.singletonList(4)));
        Set<InstructionNode> dependencies = new HashSet<>(Collections.singletonList(classicalNode1));
        dependencies.add(classicalNode2);
        when(hybridNode2.getDependencies()).thenReturn(dependencies);

        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(classicalNode1);
        instructionList.add(classicalNode2);
        instructionList.add(hybridNode1);
        instructionList.add(hybridNode2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(instructionList));
        FindHybridDependencies finder = new FindHybridDependencies(instructions);

        ArrayList<Map<Integer, Set<Integer>>> result = finder.getHybridDependencies();

        assertEquals(1, result.size());

        Map<Integer, Set<Integer>> hybridDependencies = result.get(0);
        assertTrue(hybridDependencies.containsKey(3));
        assertTrue(hybridDependencies.containsKey(4));
        assertEquals(1, hybridDependencies.get(3).size());
        assertEquals(1, hybridDependencies.get(4).size());
        assertTrue(hybridDependencies.get(3).contains(1));
        assertTrue(hybridDependencies.get(4).contains(2));
    }
}