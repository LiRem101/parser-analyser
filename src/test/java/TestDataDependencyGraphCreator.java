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
import de.hhu.lirem101.quil_analyser.DataDependencyGraphCreator;
import de.hhu.lirem101.quil_analyser.LineParameter;
import de.hhu.lirem101.quil_analyser.LineType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestDataDependencyGraphCreator {

    @Test
    void returnsEmptyGraphWhenNoBlocks() {
        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        ArrayList<LineParameter> lines = new ArrayList<>();
        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesGraphWithSingleBlock() {
        ControlFlowBlock block = new ControlFlowBlock("block");
        block.addCodeline(1);
        block.setRank(0);

        LineParameter lineParam = new LineParameter(1, LineType.QUANTUM);
        lineParam.addQuantumParameter("param1");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(1, result.size());
    }

    @Test
    void calculatesGraphWithMultipleBlocks() {
        ControlFlowBlock block1 = new ControlFlowBlock("block1");
        block1.addCodeline(1);
        block1.setRank(0);
        ControlFlowBlock block2 = new ControlFlowBlock("block2");
        block2.addCodeline(2);
        block2.setRank(1);

        LineParameter lineParam1 = new LineParameter(1, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        LineParameter lineParam2 = new LineParameter(2, LineType.CLASSICAL);
        lineParam2.addQuantumParameter("param2");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block1);
        blocks.add(block2);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(2, result.size());
        assertEquals(new HashSet<>(), lineParam1.getExecuteBeforeLines());
        assertEquals(new HashSet<>(), lineParam2.getExecuteBeforeLines());
    }

    @Test
    void handlesParameterDependenciesCorrectlyOfOneBlock() {
        ControlFlowBlock block = new ControlFlowBlock("block");
        block.addCodeline(0);
        block.addCodeline(1);
        block.setRank(0);

        LineParameter lineParam1 = new LineParameter(0, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        LineParameter lineParam2 = new LineParameter(1, LineType.QUANTUM);
        lineParam2.addQuantumParameter("param1");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();
        result.sort(Comparator.comparingInt(LineParameter::getLineNumber));

        assertEquals(2, result.size());
        assertTrue(result.get(1).getExecuteBeforeLines().contains(result.get(0)));
        assertEquals(new HashSet<>(), result.get(0).getExecuteBeforeLines());
    }

    @Test
    void handlesNoParameterDependenciesInOneBlock() {
        ControlFlowBlock block = new ControlFlowBlock("block");
        block.addCodeline(1);
        block.addCodeline(2);
        block.setRank(0);

        LineParameter lineParam1 = new LineParameter(1, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        LineParameter lineParam2 = new LineParameter(2, LineType.CLASSICAL);
        lineParam2.addQuantumParameter("param2");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(2, result.size());
        assertEquals(new HashSet<>(), result.get(0).getExecuteBeforeLines());
        assertEquals(new HashSet<>(), result.get(1).getExecuteBeforeLines());
    }

    @Test
    void handlesParameterDependenciesInMultipleBlocks1() {
        ControlFlowBlock block1 = new ControlFlowBlock("block1");
        block1.addCodeline(1);
        block1.setRank(0);
        ControlFlowBlock block2 = new ControlFlowBlock("block2");
        block2.addCodeline(2);
        block2.setRank(1);
        block2.setNewDominatingBlock(block1);
        ControlFlowBlock block3 = new ControlFlowBlock("block3");
        block3.addCodeline(3);
        block3.setRank(2);
        block3.setNewDominatingBlock(block1);
        ControlFlowBlock block4 = new ControlFlowBlock("block4");
        block4.addCodeline(4);
        block4.setRank(3);
        block4.setNewDominatingBlock(block2);
        block4.setNewDominatingBlock(block3);
        block1.addBranch(block2);
        block1.addBranch(block3);
        block2.addBranch(block4);
        block3.addBranch(block4);

        LineParameter lineParam1 = new LineParameter(1, LineType.QUANTUM);
        LineParameter lineParam2 = new LineParameter(2, LineType.QUANTUM);
        LineParameter lineParam3 = new LineParameter(3, LineType.QUANTUM);
        LineParameter lineParam4 = new LineParameter(4, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        lineParam2.addQuantumParameter("param1");
        lineParam3.addQuantumParameter("param1");
        lineParam4.addQuantumParameter("param1");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block1);
        blocks.add(block2);
        blocks.add(block3);
        blocks.add(block4);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        lines.add(lineParam3);
        lines.add(lineParam4);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();
        result.sort(Comparator.comparingInt(LineParameter::getLineNumber));

        assertEquals(4, result.size());
        assertEquals(new HashSet<>(), result.get(0).getExecuteBeforeLines());
        assertTrue(result.get(1).getExecuteBeforeLines().contains(result.get(0)));
        assertTrue(result.get(2).getExecuteBeforeLines().contains(result.get(0)));
        assertTrue(result.get(3).getExecuteBeforeLines().contains(result.get(1)));
        assertTrue(result.get(3).getExecuteBeforeLines().contains(result.get(2)));
        assertFalse(result.get(1).getExecuteBeforeLines().contains(result.get(2)));
        assertFalse(result.get(2).getExecuteBeforeLines().contains(result.get(1)));
        assertFalse(result.get(3).getExecuteBeforeLines().contains(result.get(0)));
    }

    @Test
    void handlesParameterDependenciesInMultipleBlocks2() {
        ControlFlowBlock block1 = new ControlFlowBlock("block1");
        block1.addCodeline(1);
        block1.setRank(0);
        ControlFlowBlock block2 = new ControlFlowBlock("block2");
        block2.addCodeline(2);
        block2.setRank(1);
        block2.setNewDominatingBlock(block1);
        ControlFlowBlock block3 = new ControlFlowBlock("block3");
        block3.addCodeline(3);
        block3.setRank(2);
        block3.setNewDominatingBlock(block1);
        ControlFlowBlock block4 = new ControlFlowBlock("block4");
        block4.addCodeline(4);
        block4.setRank(3);
        block4.setNewDominatingBlock(block2);
        block4.setNewDominatingBlock(block3);
        block1.addBranch(block2);
        block1.addBranch(block3);
        block2.addBranch(block4);
        block3.addBranch(block4);

        LineParameter lineParam1 = new LineParameter(1, LineType.QUANTUM);
        LineParameter lineParam2 = new LineParameter(2, LineType.QUANTUM);
        LineParameter lineParam3 = new LineParameter(3, LineType.QUANTUM);
        LineParameter lineParam4 = new LineParameter(4, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        lineParam2.addQuantumParameter("param1");
        lineParam4.addQuantumParameter("param1");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block1);
        blocks.add(block2);
        blocks.add(block3);
        blocks.add(block4);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        lines.add(lineParam3);
        lines.add(lineParam4);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(4, result.size());
        assertEquals(new HashSet<>(), result.get(0).getExecuteBeforeLines());
        assertTrue(result.get(1).getExecuteBeforeLines().contains(result.get(0)));
        assertTrue(result.get(3).getExecuteBeforeLines().contains(result.get(1)));
        assertFalse(result.get(1).getExecuteBeforeLines().contains(result.get(2)));
        assertFalse(result.get(2).getExecuteBeforeLines().contains(result.get(1)));
        assertFalse(result.get(2).getExecuteBeforeLines().contains(result.get(0)));
        assertFalse(result.get(3).getExecuteBeforeLines().contains(result.get(0)));
        assertFalse(result.get(3).getExecuteBeforeLines().contains(result.get(2)));
    }

    @Test
    void handlesParameterDependenciesInMultipleBlocks3() {
        ControlFlowBlock block1 = new ControlFlowBlock("block1");
        block1.addCodeline(1);
        block1.setRank(0);
        ControlFlowBlock block2 = new ControlFlowBlock("block2");
        block2.addCodeline(2);
        block2.setRank(1);
        block2.setNewDominatingBlock(block1);
        ControlFlowBlock block3 = new ControlFlowBlock("block3");
        block3.addCodeline(3);
        block3.setRank(2);
        block3.setNewDominatingBlock(block1);
        ControlFlowBlock block4 = new ControlFlowBlock("block4");
        block4.addCodeline(4);
        block4.setRank(3);
        block4.setNewDominatingBlock(block2);
        block4.setNewDominatingBlock(block3);
        block1.addBranch(block2);
        block1.addBranch(block3);
        block2.addBranch(block4);
        block3.addBranch(block4);

        LineParameter lineParam1 = new LineParameter(1, LineType.QUANTUM);
        LineParameter lineParam2 = new LineParameter(2, LineType.QUANTUM);
        LineParameter lineParam3 = new LineParameter(3, LineType.QUANTUM);
        LineParameter lineParam4 = new LineParameter(4, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        lineParam4.addQuantumParameter("param1");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block1);
        blocks.add(block2);
        blocks.add(block3);
        blocks.add(block4);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        lines.add(lineParam3);
        lines.add(lineParam4);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(4, result.size());


        assertEquals(new HashSet<>(), result.get(0).getExecuteBeforeLines());
        assertTrue(result.get(3).getExecuteBeforeLines().contains(result.get(0)));
        assertFalse(result.get(1).getExecuteBeforeLines().contains(result.get(0)));
        assertFalse(result.get(1).getExecuteBeforeLines().contains(result.get(2)));
        assertFalse(result.get(2).getExecuteBeforeLines().contains(result.get(1)));
        assertFalse(result.get(2).getExecuteBeforeLines().contains(result.get(0)));
        assertFalse(result.get(3).getExecuteBeforeLines().contains(result.get(1)));
        assertFalse(result.get(3).getExecuteBeforeLines().contains(result.get(2)));
    }

    @Test
    void calculatesGraphWithMultipleLinesPerBlock() {
        ControlFlowBlock block1 = new ControlFlowBlock("block1");
        block1.addCodeline(1);
        block1.addCodeline(2);
        block1.setRank(0);
        ControlFlowBlock block2 = new ControlFlowBlock("block2");
        block2.addCodeline(3);
        block2.setRank(1);
        block2.setNewDominatingBlock(block1);
        block1.addBranch(block2);

        LineParameter lineParam1 = new LineParameter(1, LineType.QUANTUM);
        lineParam1.addQuantumParameter("param1");
        LineParameter lineParam2 = new LineParameter(2, LineType.QUANTUM);
        lineParam2.addQuantumParameter("param1");
        LineParameter lineParam3 = new LineParameter(3, LineType.QUANTUM);
        lineParam3.addQuantumParameter("param1");

        ArrayList<Integer> startBlockIndizes = new ArrayList<>();
        startBlockIndizes.add(0);

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block1);
        blocks.add(block2);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        lines.add(lineParam3);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, startBlockIndizes, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(3, result.size());
        assertEquals(new HashSet<>(), result.get(0).getExecuteBeforeLines());
        assertTrue(result.get(1).getExecuteBeforeLines().contains(result.get(0)));
        assertTrue(result.get(2).getExecuteBeforeLines().contains(result.get(1)));
        assertFalse(result.get(2).getExecuteBeforeLines().contains(result.get(0)));
    }
}