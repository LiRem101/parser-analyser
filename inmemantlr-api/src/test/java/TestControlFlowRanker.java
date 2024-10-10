import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.ControlFlowRanker;
import org.junit.jupiter.api.Test;

import javax.naming.ldap.Control;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestControlFlowRanker {
    @Test
    void oneBlock() {
        ControlFlowBlock block = new ControlFlowBlock("start");

        ControlFlowRanker cfr = new ControlFlowRanker(block);
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(1, ranking.size());
        assertEquals(block, ranking.get(0));
    }

    @Test
    void simpleLinearCode() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle = new ControlFlowBlock("middle");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(middle);
        middle.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root);
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(3, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(middle, ranking.get(1));
        assertEquals(halt, ranking.get(2));
    }

    @Test
    void branchingCode1() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle = new ControlFlowBlock("middle");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(halt);
        root.addBranch(middle);
        middle.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root);
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(3, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(middle, ranking.get(1));
        assertEquals(halt, ranking.get(2));
    }

    @Test
    void branchingCode2() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle1 = new ControlFlowBlock("middle1");
        ControlFlowBlock middle2 = new ControlFlowBlock("middle2");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(halt);
        root.addBranch(middle1);
        root.addBranch(middle2);
        middle1.addBranch(halt);
        middle2.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root);
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(4, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(halt, ranking.get(3));
    }
}
