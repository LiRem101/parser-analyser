import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.ControlFlowCreator;
import de.hhu.lirem101.quil_analyser.OneLevelCodeBlock;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestControlFlowCreator {

    @Test
    void noCode() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>();
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();
        assertNotNull(cfg);
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals("start", cfg.getName());
        assertTrue(cfg.getCodelines().isEmpty());
        assertEquals(1, branches.size());
        assertEquals(branches.get(0).getName(), "halt");
        assertTrue(branches.get(0).getCodelines().isEmpty());
        assertTrue(branches.get(0).getBranches().isEmpty());
    }

    @Test
    void simpleLinearCode() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();
        assertNotNull(cfg);
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals("start", cfg.getName());
        assertEquals(cfg.getCodelines(), Arrays.asList(0, 1, 2, 3, 4, 5));
        assertEquals(1, branches.size());
        assertEquals(branches.get(0).getName(), "halt");
        assertTrue(branches.get(0).getCodelines().isEmpty());
        assertTrue(branches.get(0).getBranches().isEmpty());
    }

    @Test
    void labelLinearCode() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(3, "label1");
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();
        assertNotNull(cfg);
        assertEquals(cfg.getBranches().size(), 1);
        ControlFlowBlock branch = cfg.getBranches().get(0);
        assertEquals(branch.getBranches().size(), 1);
        ControlFlowBlock halt = branch.getBranches().get(0);
        assertEquals("start", cfg.getName());
        assertEquals(cfg.getCodelines(), Arrays.asList(0, 1, 2));
        assertEquals("label1", branch.getName());
        assertEquals(branch.getCodelines(), Arrays.asList(3, 4, 5));
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void labelJump() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(6, "label1");
        jumpsSameLevel.put(3, "label1");
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();
        assertNotNull(cfg);
        assertEquals(cfg.getBranches().size(), 1);
        ControlFlowBlock branch = cfg.getBranches().get(0);
        assertEquals(branch.getBranches().size(), 1);
        ControlFlowBlock halt = branch.getBranches().get(0);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(0, 1, 2, 3), cfg.getCodelines());
        assertEquals("label1", branch.getName());
        assertEquals(Arrays.asList(6, 7, 8), branch.getCodelines());
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void labelJumpCondWoLabel() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(6, "label1");
        jumpsCondSameLevel.put(3, "label1");
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(2, branches.size());

        ControlFlowBlock branchLabel = branches.stream().filter(b -> b.getName().equals("label1")).findFirst().orElse(null);
        assertNotNull(branchLabel);
        assertEquals(Arrays.asList(6, 7, 8), branchLabel.getCodelines());
        assertEquals(1, branchLabel.getBranches().size());

        ControlFlowBlock branchElse = branches.stream().filter(b -> b.getName().equals("line4")).findFirst().orElse(null);
        assertNotNull(branchElse);
        assertEquals(Arrays.asList(4, 5), branchElse.getCodelines());
        assertEquals(1, branchElse.getBranches().size());

        assertTrue(branchLabel == branchElse.getBranches().get(0));

        ControlFlowBlock halt = branchLabel.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void labelJumpCondWithLabel() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(3, "label1");
        labels.put(7, "label2");
        jumpsCondSameLevel.put(6, "label1");
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(0, 1, 2), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock labelBlock = branches.get(0);

        branches = labelBlock.getBranches();
        assertNotNull(labelBlock);
        assertEquals(Arrays.asList(3, 4, 5, 6), labelBlock.getCodelines());
        assertEquals(2, branches.size());

        ControlFlowBlock branchBack = branches.stream().filter(b -> b.getName().equals("label1")).findFirst().orElse(null);
        assertTrue(branchBack == labelBlock);

        ControlFlowBlock branchElse = branches.stream().filter(b -> b.getName().equals("label2")).findFirst().orElse(null);

        assertNotNull(branchElse);
        assertEquals(Arrays.asList(7, 8), branchElse.getCodelines());
        assertEquals(1, branchElse.getBranches().size());

        ControlFlowBlock halt = branchElse.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void labelJumpCondHalt() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(3, "label1");
        jumpsCondSameLevel.put(6, "label1");
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(0, 1, 2), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock labelBlock = branches.get(0);

        branches = labelBlock.getBranches();
        assertNotNull(labelBlock);
        assertEquals(Arrays.asList(3, 4, 5, 6), labelBlock.getCodelines());
        assertEquals(2, branches.size());

        ControlFlowBlock branchBack = branches.stream().filter(b -> b.getName().equals("label1")).findFirst().orElse(null);
        assertTrue(branchBack == labelBlock);

        ControlFlowBlock halt = branches.stream().filter(b -> b.getName().equals("halt")).findFirst().orElse(null);

        assertNotNull(halt);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void labelJumpCondWithLoop() {
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(3, "label1");
        jumpsCondSameLevel.put(6, "label1");
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(0, 1, 2), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock labelBlock = branches.get(0);

        branches = labelBlock.getBranches();
        assertNotNull(labelBlock);
        assertEquals(Arrays.asList(3, 4, 5, 6), labelBlock.getCodelines());
        assertEquals(2, branches.size());

        ControlFlowBlock branchBack = branches.stream().filter(b -> b.getName().equals("label1")).findFirst().orElse(null);
        assertTrue(branchBack == labelBlock);

        ControlFlowBlock branchElse = branches.stream().filter(b -> b.getName().equals("line7")).findFirst().orElse(null);

        assertNotNull(branchElse);
        assertEquals(Arrays.asList(7, 8), branchElse.getCodelines());
        assertEquals(1, branchElse.getBranches().size());

        ControlFlowBlock halt = branchElse.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void simpleDefCircuitWoLabel() {
        OneLevelCodeBlock innerBlock = mock(OneLevelCodeBlock.class);
        Map<Integer, String> innerLabels = new HashMap<>();
        Map<Integer, String> innerJumpsSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpToCircuits = new HashMap<>();
        Set<Integer> innerValidCodelines = new HashSet<>(Arrays.asList(0, 1, 2));
        Map<Integer, String> innerLinesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> innerCircuitsNextLevel = new HashMap<>();
        when(innerBlock.getLabels()).thenReturn(innerLabels);
        when(innerBlock.getJumpsSameLevel()).thenReturn(innerJumpsSameLevel);
        when(innerBlock.getJumpsCondSameLevel()).thenReturn(innerJumpsCondSameLevel);
        when(innerBlock.getJumpToCircuits()).thenReturn(innerJumpToCircuits);
        when(innerBlock.getValidCodelines()).thenReturn(innerValidCodelines);
        when(innerBlock.getLinesCircuitsNextLevel()).thenReturn(innerLinesCircuitsNextLevel);
        when(innerBlock.getCircuitsNextLevel()).thenReturn(innerCircuitsNextLevel);

        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        jumpToCircuits.put(6, "circuit1");
        linesCircuitsNextLevel.put(0, "circuit1");
        circuitsNextLevel.put("circuit1", innerBlock);
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(3, 4, 5, 6), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock circuitBlock = branches.get(0);

        branches = circuitBlock.getBranches();
        assertNotNull(circuitBlock);
        assertEquals(Arrays.asList(0, 1, 2), circuitBlock.getCodelines());
        assertEquals(1, branches.size());

        ControlFlowBlock nextBlock = branches.get(0);
        assertEquals("line7", nextBlock.getName());
        assertEquals(Arrays.asList(7, 8), nextBlock.getCodelines());
        assertEquals(1, nextBlock.getBranches().size());

        ControlFlowBlock halt = nextBlock.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void simpleDefCircuitWithLabel() {
        OneLevelCodeBlock innerBlock = mock(OneLevelCodeBlock.class);
        Map<Integer, String> innerLabels = new HashMap<>();
        Map<Integer, String> innerJumpsSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpToCircuits = new HashMap<>();
        Set<Integer> innerValidCodelines = new HashSet<>(Arrays.asList(0, 1, 2));
        Map<Integer, String> innerLinesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> innerCircuitsNextLevel = new HashMap<>();
        when(innerBlock.getLabels()).thenReturn(innerLabels);
        when(innerBlock.getJumpsSameLevel()).thenReturn(innerJumpsSameLevel);
        when(innerBlock.getJumpsCondSameLevel()).thenReturn(innerJumpsCondSameLevel);
        when(innerBlock.getJumpToCircuits()).thenReturn(innerJumpToCircuits);
        when(innerBlock.getValidCodelines()).thenReturn(innerValidCodelines);
        when(innerBlock.getLinesCircuitsNextLevel()).thenReturn(innerLinesCircuitsNextLevel);
        when(innerBlock.getCircuitsNextLevel()).thenReturn(innerCircuitsNextLevel);

        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(7, "label1");
        jumpToCircuits.put(6, "circuit1");
        linesCircuitsNextLevel.put(0, "circuit1");
        circuitsNextLevel.put("circuit1", innerBlock);
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(3, 4, 5, 6), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock circuitBlock = branches.get(0);

        branches = circuitBlock.getBranches();
        assertNotNull(circuitBlock);
        assertEquals(Arrays.asList(0, 1, 2), circuitBlock.getCodelines());
        assertEquals(1, branches.size());

        ControlFlowBlock nextBlock = branches.get(0);
        assertEquals("label1", nextBlock.getName());
        assertEquals(Arrays.asList(7, 8), nextBlock.getCodelines());
        assertEquals(1, nextBlock.getBranches().size());

        ControlFlowBlock halt = nextBlock.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void simpleDefCircuitHalt() {
        OneLevelCodeBlock innerBlock = mock(OneLevelCodeBlock.class);
        Map<Integer, String> innerLabels = new HashMap<>();
        Map<Integer, String> innerJumpsSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpToCircuits = new HashMap<>();
        Set<Integer> innerValidCodelines = new HashSet<>(Arrays.asList(0, 1, 2));
        Map<Integer, String> innerLinesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> innerCircuitsNextLevel = new HashMap<>();
        when(innerBlock.getLabels()).thenReturn(innerLabels);
        when(innerBlock.getJumpsSameLevel()).thenReturn(innerJumpsSameLevel);
        when(innerBlock.getJumpsCondSameLevel()).thenReturn(innerJumpsCondSameLevel);
        when(innerBlock.getJumpToCircuits()).thenReturn(innerJumpToCircuits);
        when(innerBlock.getValidCodelines()).thenReturn(innerValidCodelines);
        when(innerBlock.getLinesCircuitsNextLevel()).thenReturn(innerLinesCircuitsNextLevel);
        when(innerBlock.getCircuitsNextLevel()).thenReturn(innerCircuitsNextLevel);

        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(3, 4, 5, 6));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        jumpToCircuits.put(6, "circuit1");
        linesCircuitsNextLevel.put(0, "circuit1");
        circuitsNextLevel.put("circuit1", innerBlock);
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Arrays.asList(3, 4, 5, 6), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock circuitBlock = branches.get(0);

        branches = circuitBlock.getBranches();
        assertNotNull(circuitBlock);
        assertEquals(Arrays.asList(0, 1, 2), circuitBlock.getCodelines());
        assertEquals(1, branches.size());

        ControlFlowBlock halt = branches.get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void simpleDefCircuitJumpInside() {
        OneLevelCodeBlock innerBlock = mock(OneLevelCodeBlock.class);
        Map<Integer, String> innerLabels = new HashMap<>();
        Map<Integer, String> innerJumpsSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpToCircuits = new HashMap<>();
        Set<Integer> innerValidCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Map<Integer, String> innerLinesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> innerCircuitsNextLevel = new HashMap<>();
        innerLabels.put(2, "label1");
        innerJumpsCondSameLevel.put(5, "label1");
        when(innerBlock.getLabels()).thenReturn(innerLabels);
        when(innerBlock.getJumpsSameLevel()).thenReturn(innerJumpsSameLevel);
        when(innerBlock.getJumpsCondSameLevel()).thenReturn(innerJumpsCondSameLevel);
        when(innerBlock.getJumpToCircuits()).thenReturn(innerJumpToCircuits);
        when(innerBlock.getValidCodelines()).thenReturn(innerValidCodelines);
        when(innerBlock.getLinesCircuitsNextLevel()).thenReturn(innerLinesCircuitsNextLevel);
        when(innerBlock.getCircuitsNextLevel()).thenReturn(innerCircuitsNextLevel);

        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        jumpToCircuits.put(6, "circuit1");
        linesCircuitsNextLevel.put(0, "circuit1");
        circuitsNextLevel.put("circuit1", innerBlock);
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Collections.singletonList(6), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock circuitBlock = branches.get(0);

        branches = circuitBlock.getBranches();
        assertNotNull(circuitBlock);
        assertEquals(Arrays.asList(0, 1), circuitBlock.getCodelines());
        assertEquals(1, branches.size());

        ControlFlowBlock nextBlock = branches.get(0);
        assertEquals("label1", nextBlock.getName());
        assertEquals(Arrays.asList(2, 3, 4, 5), nextBlock.getCodelines());
        assertEquals(2, nextBlock.getBranches().size());
        ControlFlowBlock blockLabel = nextBlock.getBranches().stream().filter(b -> b.getName().equals("label1")).findFirst().orElse(null);
        assertTrue(blockLabel == nextBlock);

        nextBlock = nextBlock.getBranches().stream().filter(b -> b.getName().equals("line7")).findFirst().orElse(null);
        assertEquals(Arrays.asList(7, 8), nextBlock.getCodelines());
        assertEquals(1, nextBlock.getBranches().size());

        ControlFlowBlock halt = nextBlock.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void simpleDefCircuitJumpOut() {
        OneLevelCodeBlock innerBlock = mock(OneLevelCodeBlock.class);
        Map<Integer, String> innerLabels = new HashMap<>();
        Map<Integer, String> innerJumpsSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpToCircuits = new HashMap<>();
        Set<Integer> innerValidCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Map<Integer, String> innerLinesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> innerCircuitsNextLevel = new HashMap<>();
        innerJumpsSameLevel.put(4, "label1");
        when(innerBlock.getLabels()).thenReturn(innerLabels);
        when(innerBlock.getJumpsSameLevel()).thenReturn(innerJumpsSameLevel);
        when(innerBlock.getJumpsCondSameLevel()).thenReturn(innerJumpsCondSameLevel);
        when(innerBlock.getJumpToCircuits()).thenReturn(innerJumpToCircuits);
        when(innerBlock.getValidCodelines()).thenReturn(innerValidCodelines);
        when(innerBlock.getLinesCircuitsNextLevel()).thenReturn(innerLinesCircuitsNextLevel);
        when(innerBlock.getCircuitsNextLevel()).thenReturn(innerCircuitsNextLevel);

        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(6, 7, 8));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        labels.put(7, "label1");
        jumpToCircuits.put(6, "circuit1");
        linesCircuitsNextLevel.put(0, "circuit1");
        circuitsNextLevel.put("circuit1", innerBlock);
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Collections.singletonList(6), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock circuitBlock = branches.get(0);

        branches = circuitBlock.getBranches();
        assertNotNull(circuitBlock);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), circuitBlock.getCodelines());
        assertEquals(1, branches.size());

        ControlFlowBlock nextBlock = branches.get(0);
        assertEquals("label1", nextBlock.getName());
        assertEquals(Arrays.asList(7, 8), nextBlock.getCodelines());
        assertEquals(1, nextBlock.getBranches().size());

        ControlFlowBlock halt = nextBlock.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }

    @Test
    void defCircuitTwoAndJump() {
        OneLevelCodeBlock innerBlock = mock(OneLevelCodeBlock.class);
        Map<Integer, String> innerLabels = new HashMap<>();
        Map<Integer, String> innerJumpsSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> innerJumpToCircuits = new HashMap<>();
        Set<Integer> innerValidCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Map<Integer, String> innerLinesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> innerCircuitsNextLevel = new HashMap<>();
        innerLabels.put(3, "label1");
        innerJumpsSameLevel.put(2, "label1");
        when(innerBlock.getLabels()).thenReturn(innerLabels);
        when(innerBlock.getJumpsSameLevel()).thenReturn(innerJumpsSameLevel);
        when(innerBlock.getJumpsCondSameLevel()).thenReturn(innerJumpsCondSameLevel);
        when(innerBlock.getJumpToCircuits()).thenReturn(innerJumpToCircuits);
        when(innerBlock.getValidCodelines()).thenReturn(innerValidCodelines);
        when(innerBlock.getLinesCircuitsNextLevel()).thenReturn(innerLinesCircuitsNextLevel);
        when(innerBlock.getCircuitsNextLevel()).thenReturn(innerCircuitsNextLevel);

        OneLevelCodeBlock block = mock(OneLevelCodeBlock.class);
        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> jumpsSameLevel = new HashMap<>();
        Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
        Map<Integer, String> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(6, 7));
        Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
        Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();
        jumpToCircuits.put(6, "circuit1");
        jumpToCircuits.put(7, "circuit1");
        linesCircuitsNextLevel.put(0, "circuit1");
        circuitsNextLevel.put("circuit1", innerBlock);
        when(block.getLabels()).thenReturn(labels);
        when(block.getJumpsSameLevel()).thenReturn(jumpsSameLevel);
        when(block.getJumpsCondSameLevel()).thenReturn(jumpsCondSameLevel);
        when(block.getJumpToCircuits()).thenReturn(jumpToCircuits);
        when(block.getValidCodelines()).thenReturn(validCodelines);
        when(block.getLinesCircuitsNextLevel()).thenReturn(linesCircuitsNextLevel);
        when(block.getCircuitsNextLevel()).thenReturn(circuitsNextLevel);

        ControlFlowCreator cfc = new ControlFlowCreator(block);
        ControlFlowBlock cfg = cfc.createControlFlowBlock();

        assertNotNull(cfg);
        assertEquals("start", cfg.getName());
        assertEquals(Collections.singletonList(6), cfg.getCodelines());
        ArrayList<ControlFlowBlock> branches = cfg.getBranches();
        assertEquals(1, branches.size());
        ControlFlowBlock circuitBlock = branches.get(0);

        branches = circuitBlock.getBranches();
        assertNotNull(circuitBlock);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), circuitBlock.getCodelines());
        assertEquals(1, branches.size());

        ControlFlowBlock nextBlock = branches.get(0);
        assertEquals("label1", nextBlock.getName());
        assertEquals(Arrays.asList(7, 8), nextBlock.getCodelines());
        assertEquals(1, nextBlock.getBranches().size());

        ControlFlowBlock halt = nextBlock.getBranches().get(0);
        assertEquals("halt", halt.getName());
        assertTrue(halt.getCodelines().isEmpty());
        assertTrue(halt.getBranches().isEmpty());
    }
}
