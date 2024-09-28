import de.hhu.lirem101.quil_analyser.OneLevelCodeBlock;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestOneLevelCodeBlock {

    @Test
    void constructsWithValidNode() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("defLabel");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("label1");
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getLabels().containsKey("label1"));
        assertEquals(1, block.getLabels().get("label1"));
    }

    @Test
    void processesDefCircuit() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("defCircuit");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("circuit1");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("gate");
        when(childNode2.getChildren()).thenReturn(new ArrayList<>());
        when(childNode2.getLine()).thenReturn(2);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getCircuitsNextLevel().containsKey("circuit1"));
        assertEquals(1, block.getLinesCircuitsNextLevel().get("circuit1"));
    }

    @Test
    void callDefCircuit() {
        ParseTreeNode parent = mock(ParseTreeNode.class);
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        ParseTreeNode callCircuitNode = mock(ParseTreeNode.class);
        ParseTreeNode callCircuitChildNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> parentsChildren = new ArrayList<>();
        parentsChildren.add(node);
        parentsChildren.add(callCircuitNode);
        ArrayList<ParseTreeNode> callNodeChildren = new ArrayList<>();
        callNodeChildren.add(callCircuitChildNode);
        when(parent.getRule()).thenReturn("root");
        when(parent.getLine()).thenReturn(1);
        when(parent.getChildren()).thenReturn(parentsChildren);
        when(node.getRule()).thenReturn("defCircuit");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("circuit1");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("gate");
        when(childNode2.getChildren()).thenReturn(new ArrayList<>());
        when(childNode2.getLine()).thenReturn(2);
        when(callCircuitNode.getRule()).thenReturn("gate");
        when(callCircuitNode.getLine()).thenReturn(3);
        when(callCircuitNode.getChildren()).thenReturn(callNodeChildren);
        when(callCircuitNode.getFirstChild()).thenReturn(callCircuitChildNode);
        when(callCircuitChildNode.getLabel()).thenReturn("circuit1");
        when(callCircuitChildNode.getRule()).thenReturn("name");
        when(callCircuitChildNode.getChildren()).thenReturn(new ArrayList<>());
        when(callCircuitChildNode.getLine()).thenReturn(3);

        OneLevelCodeBlock block = new OneLevelCodeBlock(parent);
        assertTrue(block.getJumpToCircuits().containsKey("circuit1"));
        assertEquals(3, (int) block.getJumpToCircuits().get("circuit1"));
    }

    @Test
    void processesDefGate() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("defGate");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("gate1");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("gate");
        when(childNode2.getChildren()).thenReturn(new ArrayList<>());
        when(childNode2.getLine()).thenReturn(2);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getDefinedGates().contains("gate1"));
    }

    @Test
    void processesJumpInstructions() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        children.add(childNode);
        children.add(childNode2);
        when(node.getRule()).thenReturn("jump");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(node.getChildren()).thenReturn(children);
        when(childNode.getLabel()).thenReturn("label1");
        when(childNode.getRule()).thenReturn("label");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("addr");
        when(childNode2.getLine()).thenReturn(1);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getBranchesSameLevel().containsKey("label1"));
    }

    @Test
    void processesJumpUnlessInstructions() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        children.add(childNode);
        children.add(childNode2);
        when(node.getRule()).thenReturn("jumpUnless");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(node.getChildren()).thenReturn(children);
        when(childNode.getLabel()).thenReturn("label1");
        when(childNode.getRule()).thenReturn("label");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("addr");
        when(childNode2.getLine()).thenReturn(1);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getBranchesCondSameLevel().containsKey("label1"));
    }

    @Test
    void handlesEmptyNode() {
        OneLevelCodeBlock block = new OneLevelCodeBlock(null);
        assertTrue(block.getValidCodelines().isEmpty());
    }
}