import de.hhu.lirem101.quil_optimizer.SortingNodesToLines;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestSortingNodesToLines {

    @Test
    void returnsEmptyMapWhenNoNodes() {
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        when(rootNode.getChildren()).thenReturn(Collections.emptyList());
        SortingNodesToLines sorter = new SortingNodesToLines(rootNode);

        HashMap<Integer, ParseTreeNode> result = sorter.getSortedNodes();

        assertTrue(result.isEmpty());
    }

    @Test
    void sortsSingleInstructionNodeCorrectly() {
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        ParseTreeNode instrNode = mock(ParseTreeNode.class);
        ParseTreeNode gateNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        children.add(instrNode);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getChildren()).thenReturn(children);
        when(instrNode.getLine()).thenReturn(1);
        when(instrNode.getRule()).thenReturn("instr");
        when(instrNode.getFirstChild()).thenReturn(gateNode);
        when(gateNode.getLine()).thenReturn(1);
        when(gateNode.getRule()).thenReturn("gate");

        SortingNodesToLines sorter = new SortingNodesToLines(rootNode);

        HashMap<Integer, ParseTreeNode> result = sorter.getSortedNodes();

        assertEquals(1, result.size());
        assertEquals(gateNode, result.get(1));
    }

    @Test
    void sortsMultipleInstructionNodesCorrectly() {
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        ParseTreeNode instrNode1 = mock(ParseTreeNode.class);
        ParseTreeNode instrNode2 = mock(ParseTreeNode.class);
        ParseTreeNode gateNode = mock(ParseTreeNode.class);
        ParseTreeNode classicalNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        children.add(instrNode1);
        children.add(instrNode2);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getChildren()).thenReturn(children);
        when(instrNode1.getLine()).thenReturn(1);
        when(instrNode1.getRule()).thenReturn("instr");
        when(instrNode1.getFirstChild()).thenReturn(gateNode);
        when(gateNode.getLine()).thenReturn(1);
        when(gateNode.getRule()).thenReturn("gate");
        when(instrNode2.getLine()).thenReturn(2);
        when(instrNode2.getRule()).thenReturn("instr");
        when(instrNode2.getFirstChild()).thenReturn(classicalNode);
        when(classicalNode.getLine()).thenReturn(2);
        when(classicalNode.getRule()).thenReturn("classical");

        SortingNodesToLines sorter = new SortingNodesToLines(rootNode);

        HashMap<Integer, ParseTreeNode> result = sorter.getSortedNodes();

        assertEquals(2, result.size());
        assertEquals(gateNode, result.get(1));
        assertEquals(classicalNode, result.get(2));
    }
}