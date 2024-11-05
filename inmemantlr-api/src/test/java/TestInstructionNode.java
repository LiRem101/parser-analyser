import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestInstructionNode {

    @Test
    void returnsEmptyBranchesWhenNoParameters() {
        InstructionNode node = new InstructionNode(1, LineType.CLASSICAL);
        assertTrue(node.getBranches().isEmpty());
    }

    @Test
    void returnsQuantumParameters() {
        InstructionNode node = new InstructionNode(1, LineType.QUANTUM);
        ParseTreeNode ptNode = mock(ParseTreeNode.class);
        when(ptNode.getLine()).thenReturn(1);
        when(ptNode.getRule()).thenReturn("qubit");
        when(ptNode.getLabel()).thenReturn("1");
        when(ptNode.getChildren()).thenReturn(Collections.emptyList());
        node.setParseTreeNode(ptNode);
        assertEquals(Collections.singletonList("1"), node.getParameters());
    }

    @Test
    void returnsClassicalParameters() {
        InstructionNode node = new InstructionNode(1, LineType.CLASSICAL);
        ParseTreeNode ptNode = mock(ParseTreeNode.class);
        when(ptNode.getLine()).thenReturn(1);
        when(ptNode.getRule()).thenReturn("addr");
        when(ptNode.getLabel()).thenReturn("1");
        when(ptNode.getChildren()).thenReturn(Collections.emptyList());
        node.setParseTreeNode(ptNode);
        assertEquals(Collections.singletonList("1"), node.getParameters());
    }

    @Test
    void handlesMixedParametersCorrectly() {
        InstructionNode node = new InstructionNode(1, LineType.CLASSICAL_INFLUENCES_QUANTUM);
        ParseTreeNode quantumNode = mock(ParseTreeNode.class);
        when(quantumNode.getLine()).thenReturn(1);
        when(quantumNode.getRule()).thenReturn("qubit");
        when(quantumNode.getLabel()).thenReturn("1");
        when(quantumNode.getChildren()).thenReturn(Collections.emptyList());
        ParseTreeNode classicalNode = mock(ParseTreeNode.class);
        when(classicalNode.getLine()).thenReturn(1);
        when(classicalNode.getRule()).thenReturn("addr");
        when(classicalNode.getLabel()).thenReturn("ancilla");
        when(classicalNode.getChildren()).thenReturn(Collections.emptyList());
        ParseTreeNode parentNode = mock(ParseTreeNode.class);
        when(parentNode.getLine()).thenReturn(1);
        when(parentNode.getRule()).thenReturn("");
        when(parentNode.getLabel()).thenReturn("");
        when(parentNode.getChildren()).thenReturn(Arrays.asList(quantumNode, classicalNode));
        node.setParseTreeNode(parentNode);
        assertEquals(Arrays.asList("1", "ancilla"), node.getParameters());
    }

    @Test
    void setsParseTreeNodeCorrectly() {
        InstructionNode node = new InstructionNode(1, LineType.CLASSICAL);
        ParseTreeNode ptNode = mock(ParseTreeNode.class);
        when(ptNode.getRule()).thenReturn("");
        node.setParseTreeNode(ptNode);
        assertEquals(ptNode, node.getParseTreeNode());
    }

    @Test
    void handlesParameterLinksCorrectly() {
        InstructionNode prevNode = new InstructionNode(1, LineType.CLASSICAL);
        InstructionNode node = new InstructionNode(1, LineType.CLASSICAL);
        ParseTreeNode classicalNode = mock(ParseTreeNode.class);
        when(classicalNode.getLine()).thenReturn(1);
        when(classicalNode.getRule()).thenReturn("addr");
        when(classicalNode.getLabel()).thenReturn("param1");
        when(classicalNode.getChildren()).thenReturn(Collections.emptyList());
        node.setParseTreeNode(classicalNode);
        prevNode.setParseTreeNode(classicalNode);

        Map<String, InstructionNode> previousParameters = new HashMap<>();
        prevNode.setParameterLinks(previousParameters);

        Map<String, InstructionNode> expected = new HashMap<>();
        expected.put("param1", prevNode);
        assertEquals(expected, previousParameters);

        node.setParameterLinks(previousParameters);
        expected.put("param1", node);
        assertEquals(expected, previousParameters);

        assertTrue(node.getNextInstructions().isEmpty());
        assertEquals(Collections.singletonList(prevNode), node.getBranches());
        assertEquals(Collections.singletonList(node), prevNode.getNextInstructions());
        assertTrue(prevNode.getBranches().isEmpty());
    }
}