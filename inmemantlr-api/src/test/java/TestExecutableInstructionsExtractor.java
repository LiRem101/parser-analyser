import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;

class TestExectuableInstructionsExtractor {

    @Test
    void returnsEmptyListWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<ArrayList<InstructionNode>> executionQueue = new ArrayList<>();
        ExecutableInstructionsExtractor extractor = new ExecutableInstructionsExtractor(instructions);

        ArrayList<ArrayList<InstructionNode>> result = extractor.getExecutableInstructions(executionQueue);

        assertTrue(result.isEmpty());
    }

    @Test
    void throwsExceptionWhenSizesDoNotMatch() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>());
        ArrayList<ArrayList<InstructionNode>> executionQueue = new ArrayList<>();
        ExecutableInstructionsExtractor extractor = new ExecutableInstructionsExtractor(instructions);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            extractor.getExecutableInstructions(executionQueue);
        });

        assertEquals("The size of the instructions list and the executableInstructions list must be the same.", exception.getMessage());
    }

    @Test
    void returnsExecutableInstructionsWhenAllPreviousInstructionsAreInQueue() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getBranches()).thenReturn(new ArrayList<>());
        when(node2.getBranches()).thenReturn(new ArrayList<InstructionNode>(Collections.singletonList(node1)));

        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<InstructionNode> instructionList1 = new ArrayList<>();
        instructionList1.add(node1);
        instructionList1.add(node2);
        instructions.add(instructionList1);
        ArrayList<ArrayList<InstructionNode>> executionQueue = new ArrayList<>();
        executionQueue.add(new ArrayList<>(Collections.singletonList(node1)));
        ExecutableInstructionsExtractor extractor = new ExecutableInstructionsExtractor(instructions);

        ArrayList<ArrayList<InstructionNode>> result = extractor.getExecutableInstructions(executionQueue);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(node2, result.get(0).get(0));
    }

    @Test
    void handlesMultipleInstructionListsCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        InstructionNode node3 = mock(InstructionNode.class);
        when(node1.getBranches()).thenReturn(new ArrayList<>());
        when(node2.getBranches()).thenReturn(new ArrayList<>(Collections.singletonList(node1)));
        when(node3.getBranches()).thenReturn(new ArrayList<>());

        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<InstructionNode> instructionList1 = new ArrayList<>();
        instructionList1.add(node1);
        instructionList1.add(node2);
        instructions.add(instructionList1);
        instructions.add(new ArrayList<>(Collections.singletonList(node3)));

        ArrayList<ArrayList<InstructionNode>> executionQueue = new ArrayList<>();
        executionQueue.add(new ArrayList<>(Collections.singletonList(node1)));
        executionQueue.add(new ArrayList<>(Collections.singletonList(node3)));

        ExecutableInstructionsExtractor extractor = new ExecutableInstructionsExtractor(instructions);

        ArrayList<ArrayList<InstructionNode>> result = extractor.getExecutableInstructions(executionQueue);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(node2, result.get(0).get(0));
        assertTrue(result.get(1).isEmpty());
    }
}