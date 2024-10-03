import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_analyser.SplitterQuantumClassical;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestSplitterQuantumClass {

    private Map<Integer, LineType> createQuantumClassicalMap(int start, int end) {
        Map<Integer, LineType> classes = new HashMap<>();
        for(int i = start; i < end; i++) {
            if (i % 2 == 0) {
                classes.put(i, LineType.CLASSICAL);
            } else {
                classes.put(i, LineType.QUANTUM);
            }
        }
        return classes;
    }

    private ControlFlowBlock createBlock(String name, int start, int end) {
        ControlFlowBlock block = new ControlFlowBlock(name);
        for(int i = start; i < end; i++) {
            block.addCodeline(i);
        }
        return block;
    }

    @Test
    public void testGetNewNode() {
        // Arrange
        ControlFlowBlock startBlock = createBlock("start", 0, 10);
        Map<Integer, LineType> classes = createQuantumClassicalMap(0, 10);
        startBlock.addBranch(new ControlFlowBlock("halt"));

        SplitterQuantumClassical splitterQuantumClassical = new SplitterQuantumClassical(startBlock, classes);

        // Act
        ControlFlowBlock result = splitterQuantumClassical.getNewNode();

        // Assert
        assertNotNull(result);
        assertTrue(result.getCodelines().isEmpty());
        assertEquals(2, result.getBranches().size());
        assertEquals(LineType.CONTROL_STRUCTURE, result.getLineType());

        ControlFlowBlock quantumBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.QUANTUM).findFirst().orElse(null);
        assertNotNull(quantumBranch);
        assertEquals(Arrays.asList(1, 3, 5, 7, 9), quantumBranch.getCodelines());
        assertEquals(1, quantumBranch.getBranches().size());

        ControlFlowBlock classicalBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.CLASSICAL).findFirst().orElse(null);
        assertNotNull(classicalBranch);
        assertEquals(Arrays.asList(0, 2, 4, 6, 8), classicalBranch.getCodelines());
        assertEquals(1, classicalBranch.getBranches().size());

        assertEquals(quantumBranch.getBranches().get(0), classicalBranch.getBranches().get(0));
        assertEquals("halt", quantumBranch.getBranches().get(0).getName());
        assertEquals(LineType.CONTROL_STRUCTURE, quantumBranch.getBranches().get(0).getLineType());
    }

    @Test
    public void testTwoNodes() {
        // Arrange
        ControlFlowBlock startBlock = createBlock("start", 0, 5);
        ControlFlowBlock middleBlock = createBlock("middle", 5, 10);
        Map<Integer, LineType> classes = createQuantumClassicalMap(0, 10);
        startBlock.addBranch(middleBlock);
        middleBlock.addBranch(new ControlFlowBlock("halt"));

        SplitterQuantumClassical splitterQuantumClassical = new SplitterQuantumClassical(startBlock, classes);

        // Act
        ControlFlowBlock result = splitterQuantumClassical.getNewNode();

        // Assert
        assertNotNull(result);
        assertTrue(result.getCodelines().isEmpty());
        assertEquals(2, result.getBranches().size());
        assertEquals(LineType.CONTROL_STRUCTURE, result.getLineType());

        ControlFlowBlock quantumBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.QUANTUM).findFirst().orElse(null);
        assertNotNull(quantumBranch);
        assertEquals(Arrays.asList(1, 3), quantumBranch.getCodelines());
        assertEquals(1, quantumBranch.getBranches().size());

        ControlFlowBlock classicalBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.CLASSICAL).findFirst().orElse(null);
        assertNotNull(classicalBranch);
        assertEquals(Arrays.asList(0, 2, 4), classicalBranch.getCodelines());
        assertEquals(1, classicalBranch.getBranches().size());

        assertEquals(quantumBranch.getBranches().get(0), classicalBranch.getBranches().get(0));
        assertEquals("middle", quantumBranch.getBranches().get(0).getName());
        assertEquals(LineType.CONTROL_STRUCTURE, quantumBranch.getBranches().get(0).getLineType());

        result = quantumBranch.getBranches().get(0) ;

        // Assert
        assertNotNull(result);
        assertTrue(result.getCodelines().isEmpty());
        assertEquals(2, result.getBranches().size());
        assertEquals(LineType.CONTROL_STRUCTURE, result.getLineType());

        quantumBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.QUANTUM).findFirst().orElse(null);
        assertNotNull(quantumBranch);
        assertEquals(Arrays.asList(5, 7, 9), quantumBranch.getCodelines());
        assertEquals(1, quantumBranch.getBranches().size());

        classicalBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.CLASSICAL).findFirst().orElse(null);
        assertNotNull(classicalBranch);
        assertEquals(Arrays.asList(6, 8), classicalBranch.getCodelines());
        assertEquals(1, classicalBranch.getBranches().size());

        assertEquals(quantumBranch.getBranches().get(0), classicalBranch.getBranches().get(0));
        assertEquals("halt", quantumBranch.getBranches().get(0).getName());
        assertEquals(LineType.CONTROL_STRUCTURE, quantumBranch.getBranches().get(0).getLineType());
    }

}
