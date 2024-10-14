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
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

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

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(1, result.size());
        assertEquals(lineParam, result.get(0));
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

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block1);
        blocks.add(block2);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(2, result.size());
        assertTrue(result.contains(lineParam1));
        assertTrue(result.contains(lineParam2));
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

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(2, result.size());
        assertTrue(result.contains(lineParam1));
        assertTrue(result.contains(lineParam2));
        assertTrue(lineParam2.getExecuteBeforeLines().contains(lineParam1));
        assertEquals(new HashSet<>(), lineParam1.getExecuteBeforeLines());
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

        ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
        blocks.add(block);
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(lineParam1);
        lines.add(lineParam2);
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(2, result.size());
        assertTrue(result.contains(lineParam1));
        assertTrue(result.contains(lineParam2));
        assertEquals(new HashSet<>(), lineParam1.getExecuteBeforeLines());
        assertEquals(new HashSet<>(), lineParam2.getExecuteBeforeLines());
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
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(4, result.size());
        assertTrue(result.contains(lineParam1));
        assertTrue(result.contains(lineParam2));
        assertTrue(result.contains(lineParam3));
        assertTrue(result.contains(lineParam4));
        assertEquals(new HashSet<>(), lineParam1.getExecuteBeforeLines());
        assertTrue(lineParam2.getExecuteBeforeLines().contains(lineParam1));
        assertTrue(lineParam3.getExecuteBeforeLines().contains(lineParam1));
        assertTrue(lineParam4.getExecuteBeforeLines().contains(lineParam2));
        assertTrue(lineParam4.getExecuteBeforeLines().contains(lineParam3));
        assertFalse(lineParam2.getExecuteBeforeLines().contains(lineParam3));
        assertFalse(lineParam3.getExecuteBeforeLines().contains(lineParam2));
        assertFalse(lineParam4.getExecuteBeforeLines().contains(lineParam1));
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
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(4, result.size());
        assertTrue(result.contains(lineParam1));
        assertTrue(result.contains(lineParam2));
        assertTrue(result.contains(lineParam3));
        assertTrue(result.contains(lineParam4));
        assertEquals(new HashSet<>(), lineParam1.getExecuteBeforeLines());
        assertTrue(lineParam2.getExecuteBeforeLines().contains(lineParam1));
        assertTrue(lineParam4.getExecuteBeforeLines().contains(lineParam2));
        assertFalse(lineParam2.getExecuteBeforeLines().contains(lineParam3));
        assertFalse(lineParam3.getExecuteBeforeLines().contains(lineParam2));
        assertFalse(lineParam3.getExecuteBeforeLines().contains(lineParam1));
        assertFalse(lineParam4.getExecuteBeforeLines().contains(lineParam1));
        assertFalse(lineParam4.getExecuteBeforeLines().contains(lineParam3));
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
        DataDependencyGraphCreator creator = new DataDependencyGraphCreator(blocks, lines);

        ArrayList<LineParameter> result = creator.getDataDependencyGraph();

        assertEquals(4, result.size());
        assertTrue(result.contains(lineParam1));
        assertTrue(result.contains(lineParam2));
        assertTrue(result.contains(lineParam3));
        assertTrue(result.contains(lineParam4));
        assertEquals(new HashSet<>(), lineParam1.getExecuteBeforeLines());
        assertTrue(lineParam4.getExecuteBeforeLines().contains(lineParam1));
        assertFalse(lineParam2.getExecuteBeforeLines().contains(lineParam1));
        assertFalse(lineParam2.getExecuteBeforeLines().contains(lineParam3));
        assertFalse(lineParam3.getExecuteBeforeLines().contains(lineParam2));
        assertFalse(lineParam3.getExecuteBeforeLines().contains(lineParam1));
        assertFalse(lineParam4.getExecuteBeforeLines().contains(lineParam2));
        assertFalse(lineParam4.getExecuteBeforeLines().contains(lineParam3));
    }
}