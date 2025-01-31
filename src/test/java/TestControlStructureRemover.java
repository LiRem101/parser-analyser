import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.junit.jupiter.api.Test;

import static de.hhu.lirem101.quil_optimizer.ControlStructureRemover.removeControlStructures;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestControlStructureRemover {

    @Test
    void removesControlStructuresCorrectly() {
        InstructionNode controlInstruction = new InstructionNode(1, LineType.CONTROL_STRUCTURE);
        InstructionNode normalInstruction = new InstructionNode(2, LineType.CLASSICAL);
        ArrayList<InstructionNode> innerInstructions = new ArrayList<>();
        innerInstructions.add(controlInstruction);
        innerInstructions.add(normalInstruction);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(innerInstructions));

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertEquals(1, result.get(0).size());
        assertEquals(normalInstruction, result.get(0).get(0));
    }

    @Test
    void addsEndInstructionToEmptyBlocks() {
        InstructionNode controlInstruction = new InstructionNode(1, LineType.CONTROL_STRUCTURE);
        InstructionNode endInstruction = new InstructionNode(2, LineType.CONTROL_STRUCTURE);
        ArrayList<InstructionNode> innerInstructions = new ArrayList<>();
        innerInstructions.add(controlInstruction);
        innerInstructions.add(endInstruction);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(innerInstructions));

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertEquals(1, result.get(0).size());
        assertEquals(endInstruction, result.get(0).get(0));
    }

    @Test
    void handlesMultipleInstructionBlocksCorrectly() {
        InstructionNode controlInstruction1 = new InstructionNode(0, LineType.CONTROL_STRUCTURE);
        InstructionNode normalInstruction1 = new InstructionNode(1, LineType.CLASSICAL);
        InstructionNode controlInstruction2 = new InstructionNode(2, LineType.CONTROL_STRUCTURE);
        InstructionNode normalInstruction2 = new InstructionNode(3, LineType.CLASSICAL);
        ArrayList<InstructionNode> innerInstructions1 = new ArrayList<>();
        innerInstructions1.add(controlInstruction1);
        innerInstructions1.add(normalInstruction1);
        ArrayList<InstructionNode> innerInstructions2 = new ArrayList<>();
        innerInstructions2.add(controlInstruction2);
        innerInstructions2.add(normalInstruction2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(innerInstructions1);
        instructions.add(innerInstructions2);

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(normalInstruction1, result.get(0).get(0));
        assertEquals(1, result.get(1).size());
        assertEquals(normalInstruction2, result.get(1).get(0));
    }

    @Test
    void handlesEmptyInstructionListsCorrectly() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertTrue(result.isEmpty());
    }
}