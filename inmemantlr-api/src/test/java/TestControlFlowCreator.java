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
        BidiMap<String, Integer> labels = new TreeBidiMap<>();
        BidiMap<String, Integer> jumpsSameLevel = new TreeBidiMap<>();
        BidiMap<String, Integer> jumpsCondSameLevel = new TreeBidiMap<>();
        Map<String, Integer> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>();
        Map<String, Integer> linesCircuitsNextLevel = new HashMap<>();
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
        BidiMap<String, Integer> labels = new TreeBidiMap<>();
        BidiMap<String, Integer> jumpsSameLevel = new TreeBidiMap<>();
        BidiMap<String, Integer> jumpsCondSameLevel = new TreeBidiMap<>();
        Map<String, Integer> jumpToCircuits = new HashMap<>();
        Set<Integer> validCodelines = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Map<String, Integer> linesCircuitsNextLevel = new HashMap<>();
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
}
