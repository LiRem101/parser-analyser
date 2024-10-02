import de.hhu.lirem101.quil_analyser.ClassifyLines;
import de.hhu.lirem101.quil_analyser.LineType;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestClassifyLines {

    public ParseTreeNode mockNode(String rule, int line) {
        ParseTreeNode node = mock(ParseTreeNode.class);
        when(node.getLine()).thenReturn(line);
        when(node.getRule()).thenReturn(rule);
        return node;
    }

    @Test
    public void testMeasurement() {
        ParseTreeNode root = mockNode("quil", 1);
        ParseTreeNode branch1 = mockNode("allInstr", 1);
        ParseTreeNode branch2 = mockNode("allInstr", 2);
        ParseTreeNode branch3 = mockNode("allInstr", 3);
        when(root.getChildren()).thenReturn(Arrays.asList(branch1, branch2, branch3));

        ParseTreeNode instr1 = mockNode("instr", 1);
        when(branch1.getChildren()).thenReturn(Collections.singletonList(instr1));
        ParseTreeNode instr2 = mockNode("instr", 2);
        when(branch2.getChildren()).thenReturn(Collections.singletonList(instr2));
        ParseTreeNode instr3 = mockNode("instr", 3);
        when(branch3.getChildren()).thenReturn(Collections.singletonList(instr3));

        ParseTreeNode mem = mockNode("memoryDescriptor", 1);
        when(instr1.getChildren()).thenReturn(Collections.singletonList(mem));

        ParseTreeNode measure2 = mockNode("measure", 2);
        when(instr2.getChildren()).thenReturn(Collections.singletonList(measure2));
        ParseTreeNode qubit1 = mockNode("qubit", 2);
        when(measure2.getChildren()).thenReturn(Collections.singletonList(qubit1));

        ParseTreeNode measure3 = mockNode("measure", 3);
        when(instr3.getChildren()).thenReturn(Collections.singletonList(measure3));
        ParseTreeNode qubit2 = mockNode("qubit", 3);
        ParseTreeNode addr = mockNode("addr", 3);
        when(measure3.getChildren()).thenReturn(Arrays.asList(qubit2,addr));

        ClassifyLines cl = new ClassifyLines(root);
        Map<Integer, LineType> result = cl.classifyLines();
        assertEquals(LineType.CLASSICAL, result.get(1));
        assertEquals(LineType.QUANTUM, result.get(2));
        assertEquals(LineType.QUANTUM_INFLUENCES_CLASSICAL, result.get(3));
    }

}
