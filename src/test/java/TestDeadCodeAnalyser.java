import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.analysis.DeadCodeAnalyser;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestDeadCodeAnalyser {

    @Test
    void returnsEmptyIndizesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        Set<Integer> result = analyser.getIndizesOfDeadLines();

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyDeadLinesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        ArrayList<Set<Integer>> result = analyser.getDeadLines();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesDeadLinesCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        when(instruction.getLine()).thenReturn(1);
        when(instruction.getShownToBeDead()).thenReturn(true);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>(Collections.singletonList(new HashSet<>()));
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        ArrayList<Set<Integer>> result = analyser.getDeadLines();

        assertEquals(1, result.size());
        assertTrue(result.get(0).contains(1));
    }

    @Test
    void calculatesIndizesOfDeadLinesCorrectlyWithDeadBlock() {
        InstructionNode instruction1 = mock(InstructionNode.class);
        InstructionNode instrcution2 = mock(InstructionNode.class);
        when(instruction1.getLine()).thenReturn(1);
        when(instruction1.getShownToBeDead()).thenReturn(false);
        when(instrcution2.getLine()).thenReturn(2);
        when(instrcution2.getShownToBeDead()).thenReturn(false);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Collections.singletonList(instruction1)));
        instructions.add(new ArrayList<>(Collections.singletonList(instrcution2)));
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>(Collections.singletonList(new HashSet<>()));
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        Set<Integer> result = analyser.getIndizesOfDeadLines();

        assertEquals(1, result.size());
        assertTrue(result.contains(1));
    }

    @Test
    void calculatesIndizesOfDeadLinesCorrectlyWithoutDeadBlock() {
        InstructionNode instruction1 = mock(InstructionNode.class);
        InstructionNode instrcution2 = mock(InstructionNode.class);
        when(instruction1.getLine()).thenReturn(1);
        when(instruction1.getShownToBeDead()).thenReturn(false);
        when(instrcution2.getLine()).thenReturn(2);
        when(instrcution2.getShownToBeDead()).thenReturn(false);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Collections.singletonList(instruction1)));
        instructions.add(new ArrayList<>(Collections.singletonList(instrcution2)));
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>(Collections.singletonList(new HashSet<>(Collections.singletonList(1))));
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        Set<Integer> result = analyser.getIndizesOfDeadLines();

        assertTrue(result.isEmpty());
    }
}