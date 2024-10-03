import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_analyser.SplitterQuantumClassical;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestSplitterQuantumClass {

    private Map<Integer, LineType> createQuantumClassicalMap() {
        Map<Integer, LineType> classes = new HashMap<>();
        for(int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                classes.put(i, LineType.CLASSICAL);
            } else {
                classes.put(i, LineType.QUANTUM);
            }
        }
        return classes;
    }

    private ControlFlowBlock createStartBlock() {
        ControlFlowBlock block = new ControlFlowBlock("start");
        for(int i = 0; i < 10; i++) {
            block.addCodeline(i);
        }
        return block;
    }

    @Test
    public void testGetNewNode() {
        // Arrange
        ControlFlowBlock startBlock = createStartBlock();
        Map<Integer, LineType> classes = createQuantumClassicalMap();
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

}
