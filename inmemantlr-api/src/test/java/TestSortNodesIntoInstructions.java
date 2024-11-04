import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.SortNodesIntoInstructions;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestSortNodesIntoInstructions {

    @Test
    void appendsNodeToEmptyInstructions() {
        Map<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
        sortedNodes.put(1, mock(ParseTreeNode.class));
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();

        sorter.appendNodeToInstructions(instructions);

        assertTrue(instructions.isEmpty());
    }

    @Test
    void appendsNodeToSingleInstruction() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("instr");
        Map<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
        sortedNodes.put(1, node);
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        InstructionNode instrNode = new InstructionNode(1, LineType.CLASSICAL);
        ArrayList<InstructionNode> innerList = new ArrayList<>();
        innerList.add(instrNode);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(innerList);

        sorter.appendNodeToInstructions(instructions);

        assertEquals(node, instrNode.getParseTreeNode());
    }

    @Test
    void appendsNodeToMultipleInstructions() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("instr");
        Map<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
        sortedNodes.put(1, node);
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        InstructionNode instrNode1 = new InstructionNode(1, LineType.CLASSICAL);
        InstructionNode instrNode2 = new InstructionNode(1, LineType.QUANTUM);
        ArrayList<InstructionNode> innerList1 = new ArrayList<>();
        innerList1.add(instrNode1);
        ArrayList<InstructionNode> innerList2 = new ArrayList<>();
        innerList1.add(instrNode2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(innerList1);
        instructions.add(innerList2);

        sorter.appendNodeToInstructions(instructions);

        assertEquals(node, instrNode1.getParseTreeNode());
        assertEquals(node, instrNode2.getParseTreeNode());
    }
}