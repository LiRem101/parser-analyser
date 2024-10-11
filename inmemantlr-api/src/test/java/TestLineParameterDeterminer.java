import de.hhu.lirem101.quil_analyser.ClassifyLines;
import de.hhu.lirem101.quil_analyser.LineParameter;
import de.hhu.lirem101.quil_analyser.LineParameterDeterminer;
import de.hhu.lirem101.quil_analyser.LineType;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestLineParameterDeterminer {

    @Test
    void returnsEmptySetWhenNoParameters() {
        ParseTree pt = mock(ParseTree.class);
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        when(pt.getRoot()).thenReturn(rootNode);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getLine()).thenReturn(1);
        when(rootNode.getChildren()).thenReturn(new LinkedList<>());

        ClassifyLines cl = mock(ClassifyLines.class);
        when(cl.classifyLines()).thenReturn(new HashMap<>());

        LineParameterDeterminer determiner = new LineParameterDeterminer(pt, cl);
        ArrayList<LineParameter> result = determiner.getLineParameters();

        assertTrue(result.isEmpty());
    }

    @Test
    void processesClassicalParametersCorrectly() {
        ParseTree pt = mock(ParseTree.class);
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        ParseTreeNode addrNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> addrNodes = new ArrayList<>();
        addrNodes.add(addrNode);
        when(pt.getRoot()).thenReturn(rootNode);
        when(rootNode.getChildren()).thenReturn(addrNodes);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getLine()).thenReturn(1);
        when(addrNode.getLine()).thenReturn(1);
        when(addrNode.getRule()).thenReturn("addr");
        when(addrNode.getLabel()).thenReturn("param1");

        ClassifyLines cl = mock(ClassifyLines.class);
        Map<Integer, LineType> lineTypes = new HashMap<>();
        lineTypes.put(1, LineType.CLASSICAL);
        when(cl.classifyLines()).thenReturn(lineTypes);

        LineParameterDeterminer determiner = new LineParameterDeterminer(pt, cl);
        ArrayList<LineParameter> result = determiner.getLineParameters();

        assertEquals(1, result.size());
        assertTrue(result.iterator().next().getClassicalParameters().contains("param1"));
    }

    @Test
    void processesQuantumParametersCorrectly() {
        ParseTree pt = mock(ParseTree.class);
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        ParseTreeNode qubitNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> nodes = new ArrayList<>();
        nodes.add(qubitNode);
        when(pt.getRoot()).thenReturn(rootNode);
        when(pt.getRoot()).thenReturn(rootNode);
        when(rootNode.getChildren()).thenReturn(nodes);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getLine()).thenReturn(1);
        when(qubitNode.getLine()).thenReturn(2);
        when(qubitNode.getRule()).thenReturn("qubit");
        when(qubitNode.getLabel()).thenReturn("q1");

        ClassifyLines cl = mock(ClassifyLines.class);
        Map<Integer, LineType> lineTypes = new HashMap<>();
        lineTypes.put(2, LineType.QUANTUM);
        when(cl.classifyLines()).thenReturn(lineTypes);

        LineParameterDeterminer determiner = new LineParameterDeterminer(pt, cl);
        ArrayList<LineParameter> result = determiner.getLineParameters();

        assertEquals(1, result.size());
        assertEquals(2, result.iterator().next().getLineNumber());
        assertTrue(result.iterator().next().getQuantumParameters().contains("q1"));
    }

    @Test
    void handlesMemoryDescriptorCorrectly() {
        ParseTree pt = mock(ParseTree.class);
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        ParseTreeNode memoryNode = mock(ParseTreeNode.class);
        when(pt.getRoot()).thenReturn(rootNode);
        List<ParseTreeNode> nodes = new ArrayList<>();
        nodes.add(memoryNode);
        when(rootNode.getChildren()).thenReturn(nodes);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getLine()).thenReturn(1);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getLine()).thenReturn(1);
        when(memoryNode.getLine()).thenReturn(3);
        when(memoryNode.getRule()).thenReturn("memoryDescriptor");
        when(memoryNode.getLabel()).thenReturn("DECLAREparam2REAL[1]");

        ClassifyLines cl = mock(ClassifyLines.class);
        Map<Integer, LineType> lineTypes = new HashMap<>();
        lineTypes.put(3, LineType.CLASSICAL);
        when(cl.classifyLines()).thenReturn(lineTypes);

        LineParameterDeterminer determiner = new LineParameterDeterminer(pt, cl);
        ArrayList<LineParameter> result = determiner.getLineParameters();

        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().getLineNumber());
        assertTrue(result.iterator().next().getClassicalParameters().contains("param2[1]"));
    }

    @Test
    void handlesMultipleParametersCorrectly() {
        ParseTree pt = mock(ParseTree.class);
        ParseTreeNode rootNode = mock(ParseTreeNode.class);
        ParseTreeNode addrNode = mock(ParseTreeNode.class);
        ParseTreeNode qubitNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> nodes = new ArrayList<>();
        nodes.add(addrNode);
        nodes.add(qubitNode);
        when(pt.getRoot()).thenReturn(rootNode);
        when(rootNode.getRule()).thenReturn("root");
        when(rootNode.getLine()).thenReturn(1);
        when(rootNode.getChildren()).thenReturn(nodes);
        when(addrNode.getLine()).thenReturn(4);
        when(addrNode.getRule()).thenReturn("addr");
        when(addrNode.getLabel()).thenReturn("param3");
        when(qubitNode.getLine()).thenReturn(5);
        when(qubitNode.getRule()).thenReturn("qubit");
        when(qubitNode.getLabel()).thenReturn("q2");

        ClassifyLines cl = mock(ClassifyLines.class);
        Map<Integer, LineType> lineTypes = new HashMap<>();
        lineTypes.put(4, LineType.CLASSICAL);
        lineTypes.put(5, LineType.QUANTUM);
        when(cl.classifyLines()).thenReturn(lineTypes);

        LineParameterDeterminer determiner = new LineParameterDeterminer(pt, cl);
        ArrayList<LineParameter> result = determiner.getLineParameters();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(lp -> lp.getClassicalParameters().contains("param3")));
        assertTrue(result.stream().anyMatch(lp -> lp.getQuantumParameters().contains("q2")));
        assertEquals(4, result.stream().filter(lp -> lp.getClassicalParameters().contains("param3")).findFirst().get().getLineNumber());
        assertEquals(5, result.stream().filter(lp -> lp.getQuantumParameters().contains("q2")).findFirst().get().getLineNumber());
    }
}