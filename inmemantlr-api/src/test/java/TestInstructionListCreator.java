import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionListCreator;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestInstructionListCreator {

    @Test
    void returnsEmptyInstructionsWhenNoCodelines() {
        ControlFlowBlock block = new ControlFlowBlock("test");
        Map<Integer, LineType> classes = new HashMap<>();
        InstructionListCreator creator = new InstructionListCreator(block, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesInstructionsForSingleBlock() {
        ControlFlowBlock block = new ControlFlowBlock("test");
        block.addCodeline(1);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CLASSICAL);
        InstructionListCreator creator = new InstructionListCreator(block, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(1, result.get(0).get(0).getLine());
    }

    @Test
    void calculatesInstructionsForMultipleBlocks() {
        ControlFlowBlock blockControl1 = new ControlFlowBlock("testControl1");
        blockControl1.addCodeline(1);
        ControlFlowBlock blockQuantum = new ControlFlowBlock("testQuantum");
        blockQuantum.addCodeline(2);
        ControlFlowBlock blockClassical = new ControlFlowBlock("testClassical");
        blockClassical.addCodeline(3);
        blockControl1.addBranch(blockClassical);
        blockControl1.addBranch(blockQuantum);
        ControlFlowBlock blockControl2 = new ControlFlowBlock("testControl2");
        blockControl1.addCodeline(4);
        blockQuantum.addBranch(blockControl2);
        blockClassical.addBranch(blockControl2);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CONTROL_STRUCTURE);
        classes.put(2, LineType.QUANTUM);
        classes.put(3, LineType.CLASSICAL);
        classes.put(4, LineType.CONTROL_STRUCTURE);

        InstructionListCreator creator = new InstructionListCreator(blockControl1, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(1, result.size());
        ArrayList<InstructionNode> instructions = result.get(0);
        assertEquals(2, instructions.size());
        assertEquals(2, instructions.get(0).getLine());
        assertEquals(3, instructions.get(1).getLine());
        assertEquals(LineType.QUANTUM, instructions.get(0).getLineType());
        assertEquals(LineType.CLASSICAL, instructions.get(1).getLineType());
    }

    @Test
    void handlesConditionalJumpsCorrectly() {
        ControlFlowBlock blockControl = new ControlFlowBlock("testControl");
        blockControl.addCodeline(1);
        ControlFlowBlock blockQuantum = new ControlFlowBlock("testQuantum");
        blockQuantum.addCodeline(2);
        ControlFlowBlock blockClassical = new ControlFlowBlock("testClassical");
        blockClassical.addCodeline(3);
        blockControl.addBranch(blockQuantum);
        blockControl.addBranch(blockClassical);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL);
        classes.put(2, LineType.QUANTUM);
        classes.put(3, LineType.CLASSICAL);
        InstructionListCreator creator = new InstructionListCreator(blockControl, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(3, result.size());
        ArrayList<InstructionNode> instructionsControl = result.get(0);
        ArrayList<InstructionNode> instructionsQuantum = result.get(1);
        ArrayList<InstructionNode> instructionsClassical = result.get(2);

        assertEquals(1, instructionsControl.get(0).getLine());
        assertEquals(LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL, instructionsControl.get(0).getLineType());
        assertEquals(2, instructionsQuantum.get(0).getLine());
        assertEquals(LineType.QUANTUM, instructionsQuantum.get(0).getLineType());
        assertEquals(3, instructionsClassical.get(0).getLine());
        assertEquals(LineType.CLASSICAL, instructionsClassical.get(0).getLineType());
    }
}