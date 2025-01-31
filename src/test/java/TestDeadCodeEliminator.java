import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.DeadCodeEliminator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestDeadCodeEliminator {

    @Test
    void eliminatesDeadCodeCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        ArrayList<InstructionNode> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(list));
        Set<Integer> deadLinesSet = new HashSet<>();
        deadLinesSet.add(1);
        ArrayList<Set<Integer>> deadLines = new ArrayList<>(Collections.singletonList(deadLinesSet));
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        indizesOfDeadLineBlocks.add(0);
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertEquals(1, instructions.size());
        assertTrue(instructions.get(0).isEmpty());
    }

    @Test
    void handlesEmptyInstructionsCorrectly() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<Set<Integer>> deadLines = new ArrayList<>();
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertTrue(instructions.isEmpty());
    }

    @Test
    void handlesNoDeadLinesCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        ArrayList<InstructionNode> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(list));
        ArrayList<Set<Integer>> deadLines = new ArrayList<>(Collections.singletonList(new HashSet<>()));
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertEquals(1, instructions.size());
        assertEquals(2, instructions.get(0).size());
    }

    @Test
    void handlesNoDeadBlocksCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        ArrayList<InstructionNode> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(list));
        Set<Integer> deadLinesSet = new HashSet<>();
        deadLinesSet.add(1);
        ArrayList<Set<Integer>> deadLines = new ArrayList<>(Collections.singletonList(deadLinesSet));
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertEquals(1, instructions.get(0).size());
        assertEquals(node2, instructions.get(0).get(0));
    }
}