/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_analyser.SplitterQuantumClassical;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
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
        assertEquals(2, quantumBranch.getBranches().size());

        ControlFlowBlock classicalBranch = result.getBranches().stream().filter(b -> b.getLineType() == LineType.CLASSICAL).findFirst().orElse(null);
        assertNotNull(classicalBranch);
        assertEquals(Arrays.asList(0, 2, 4), classicalBranch.getCodelines());
        assertEquals(2, classicalBranch.getBranches().size());

        assertEquals(quantumBranch.getBranches(), classicalBranch.getBranches());

        quantumBranch = quantumBranch.getBranches().stream().filter(b -> b.getLineType() == LineType.QUANTUM).findFirst().orElse(null);
        assertNotNull(quantumBranch);
        assertEquals(Arrays.asList(5, 7, 9), quantumBranch.getCodelines());
        assertEquals(1, quantumBranch.getBranches().size());

        classicalBranch = classicalBranch.getBranches().stream().filter(b -> b.getLineType() == LineType.CLASSICAL).findFirst().orElse(null);
        assertNotNull(classicalBranch);
        assertEquals(Arrays.asList(6, 8), classicalBranch.getCodelines());
        assertEquals(1, classicalBranch.getBranches().size());

        assertEquals(quantumBranch.getBranches().get(0), classicalBranch.getBranches().get(0));
        assertEquals("halt", quantumBranch.getBranches().get(0).getName());
        assertEquals(LineType.CONTROL_STRUCTURE, quantumBranch.getBranches().get(0).getLineType());
    }

    @Test
    public void testWithControlStructures() {
        Map<Integer, LineType> classes = createQuantumClassicalMap(0, 10);
        classes.put(10, LineType.CONTROL_STRUCTURE);
        classes.put(11, LineType.CONTROL_STRUCTURE);
        classes.put(12, LineType.CONTROL_STRUCTURE);
        ControlFlowBlock startBlock = createBlock("start", 0, 11);
        ControlFlowBlock middleBlock = createBlock("middle", 11, 13);

        startBlock.addBranch(middleBlock);
        middleBlock.addBranch(new ControlFlowBlock("halt"));

        SplitterQuantumClassical splitterQuantumClassical = new SplitterQuantumClassical(startBlock, classes);

        ControlFlowBlock result = splitterQuantumClassical.getNewNode();

        ControlFlowBlock branch1 = result.getBranches().get(0);
        ControlFlowBlock branch2 = result.getBranches().get(1);

        assertEquals(1, branch1.getBranches().size());
        assertEquals(1, branch2.getBranches().size());

        assertEquals(branch1.getBranches().get(0), branch2.getBranches().get(0));

        ControlFlowBlock endOfStart = branch1.getBranches().get(0);
        assertNotNull(endOfStart);
        assertEquals(LineType.CONTROL_STRUCTURE, endOfStart.getLineType());
        assertEquals(Collections.singletonList(10), endOfStart.getCodelines());

        assertEquals(1, endOfStart.getBranches().size());
        ControlFlowBlock middle = endOfStart.getBranches().get(0);
        assertNotNull(middle);
        assertEquals(LineType.CONTROL_STRUCTURE, middle.getLineType());
        assertEquals(Arrays.asList(11, 12), middle.getCodelines());

        assertEquals(1, middle.getBranches().size());
        assertEquals("halt", middle.getBranches().get(0).getName());
    }

    @Test
    public void testOnlyQuantum() {
        // Arrange
        ControlFlowBlock startBlock = createBlock("start", 0, 10);
        Map<Integer, LineType> classes = new HashMap<>();
        for(int i = 0; i < 10; i++) {
            classes.put(i, LineType.QUANTUM);
        }
        startBlock.addBranch(new ControlFlowBlock("halt"));

        SplitterQuantumClassical splitterQuantumClassical = new SplitterQuantumClassical(startBlock, classes);

        // Act
        ControlFlowBlock result = splitterQuantumClassical.getNewNode();

        // Assert
        assertNotNull(result);
        assertTrue(result.getCodelines().isEmpty());
        assertEquals(1, result.getBranches().size());
        assertEquals(LineType.CONTROL_STRUCTURE, result.getLineType());

        ControlFlowBlock quantumBranch = result.getBranches().get(0);
        assertNotNull(quantumBranch);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), quantumBranch.getCodelines());
        assertEquals(LineType.QUANTUM, quantumBranch.getLineType());
        assertEquals(1, quantumBranch.getBranches().size());

        assertEquals("halt", quantumBranch.getBranches().get(0).getName());
        assertEquals(LineType.CONTROL_STRUCTURE, quantumBranch.getBranches().get(0).getLineType());
    }

    @Test
    public void testOnlyClassical() {
        // Arrange
        ControlFlowBlock startBlock = createBlock("start", 0, 10);
        Map<Integer, LineType> classes = new HashMap<>();
        for(int i = 0; i < 10; i++) {
            classes.put(i, LineType.CLASSICAL);
        }
        startBlock.addBranch(new ControlFlowBlock("halt"));

        SplitterQuantumClassical splitterQuantumClassical = new SplitterQuantumClassical(startBlock, classes);

        // Act
        ControlFlowBlock result = splitterQuantumClassical.getNewNode();

        // Assert
        assertNotNull(result);
        assertTrue(result.getCodelines().isEmpty());
        assertEquals(1, result.getBranches().size());
        assertEquals(LineType.CONTROL_STRUCTURE, result.getLineType());

        ControlFlowBlock quantumBranch = result.getBranches().get(0);
        assertNotNull(quantumBranch);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), quantumBranch.getCodelines());
        assertEquals(LineType.CLASSICAL, quantumBranch.getLineType());
        assertEquals(1, quantumBranch.getBranches().size());

        assertEquals("halt", quantumBranch.getBranches().get(0).getName());
        assertEquals(LineType.CONTROL_STRUCTURE, quantumBranch.getBranches().get(0).getLineType());
    }

    @Test
    public void testMiddleNodeWithOnlyQuantum() {
        ControlFlowBlock startBlock = createBlock("start", 0, 5);
        ControlFlowBlock middleBlock = createBlock("middle", 5, 7);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(0, LineType.CLASSICAL);
        for(int i = 1; i < 5; i++) {
            classes.put(i, LineType.CONTROL_STRUCTURE);
        }
        for(int i = 5; i < 7; i++) {
            classes.put(i, LineType.QUANTUM);
        }
        startBlock.addBranch(middleBlock);
        middleBlock.addBranch(new ControlFlowBlock("halt"));

        SplitterQuantumClassical splitterQuantumClassical = new SplitterQuantumClassical(startBlock, classes);

        ControlFlowBlock result = splitterQuantumClassical.getNewNode();

        assertNotNull(result);
        assertTrue(result.getCodelines().isEmpty());
        assertEquals(1, result.getBranches().size());
        assertEquals(LineType.CONTROL_STRUCTURE, result.getLineType());

        ControlFlowBlock classicalBranch = result.getBranches().get(0);
        assertNotNull(classicalBranch);
        assertEquals(Collections.singletonList(0), classicalBranch.getCodelines());
        assertEquals(LineType.CLASSICAL, classicalBranch.getLineType());
        assertEquals(1, classicalBranch.getBranches().size());

        ControlFlowBlock controlBranch = classicalBranch.getBranches().get(0);
        assertNotNull(controlBranch);
        assertEquals(Arrays.asList(1, 2, 3, 4), controlBranch.getCodelines());
        assertEquals(LineType.CONTROL_STRUCTURE, controlBranch.getLineType());
        assertEquals(1, controlBranch.getBranches().size());

        ControlFlowBlock middleBranch = controlBranch.getBranches().get(0);
        assertNotNull(middleBranch);
        assertEquals(Arrays.asList(5, 6), middleBranch.getCodelines());
        assertEquals(LineType.QUANTUM, middleBranch.getLineType());
        assertEquals(1, middleBranch.getBranches().size());

        assertEquals("halt", middleBranch.getBranches().get(0).getName());
    }

}
