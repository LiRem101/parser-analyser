import com.kitfox.svg.A;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.NodeSorter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestNodeSorter {

    @Test
    void sortsNodesWithDependenciesCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getDependencies()).thenReturn(new HashSet<>());
        when(node2.getDependencies()).thenReturn(new HashSet<>(Collections.singletonList(node1)));
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node1);
        instructionList.add(node2);

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertEquals(2, sortedList.size());
        assertEquals(node1, sortedList.get(0));
        assertEquals(node2, sortedList.get(1));
    }

    @Test
    void handlesEmptyInstructionListCorrectly() {
        ArrayList<InstructionNode> instructionList = new ArrayList<>();

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertTrue(sortedList.isEmpty());
    }

    @Test
    void handlesSingleNodeListCorrectly() {
        InstructionNode node = mock(InstructionNode.class);
        when(node.getDependencies()).thenReturn(new HashSet<>());
        ArrayList<InstructionNode> instructionList = new ArrayList<>(Collections.singletonList(node));

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertEquals(1, sortedList.size());
        assertEquals(node, sortedList.get(0));
    }

    @Test
    void handlesMultipleIndependentNodesCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getDependencies()).thenReturn(new HashSet<>());
        when(node2.getDependencies()).thenReturn(new HashSet<>());
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node1);
        instructionList.add(node2);

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertEquals(2, sortedList.size());
        assertTrue(sortedList.contains(node1));
        assertTrue(sortedList.contains(node2));
    }
}