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
    public void memoryDescriptor() {
        ParseTreeNode root = mockNode("quil", 1);
        ParseTreeNode branch = mockNode("allInstr", 1);
        when(root.getChildren()).thenReturn(Collections.singletonList(branch));

        ParseTreeNode instr = mockNode("instr", 1);
        when(branch.getChildren()).thenReturn(Collections.singletonList(instr));

        ParseTreeNode mem = mockNode("memoryDescriptor", 1);
        when(instr.getChildren()).thenReturn(Collections.singletonList(mem));

        ClassifyLines cl = new ClassifyLines(root);
        Map<Integer, LineType> result = cl.classifyLines();
        assertEquals(LineType.CLASSICAL, result.get(1));
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

    @Test
    public void testParams() {
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

        ParseTreeNode gate1 = mockNode("gate", 1);
        when(instr1.getChildren()).thenReturn(Collections.singletonList(gate1));
        ParseTreeNode gate2 = mockNode("gate", 2);
        when(instr2.getChildren()).thenReturn(Collections.singletonList(gate2));
        ParseTreeNode gate3 = mockNode("gate", 3);
        when(instr3.getChildren()).thenReturn(Collections.singletonList(gate3));

        ParseTreeNode name1 = mockNode("name", 1);
        ParseTreeNode name2 = mockNode("name", 2);
        ParseTreeNode name3 = mockNode("name", 3);
        ParseTreeNode qubit1 = mockNode("qubit", 1);
        ParseTreeNode qubit2 = mockNode("qubit", 2);
        ParseTreeNode qubit3 = mockNode("qubit", 3);

        ParseTreeNode param1 = mockNode("param", 1);
        ParseTreeNode expression1 = mockNode("expression", 1);
        ParseTreeNode addr1 = mockNode("addr", 1);
        when(param1.getChildren()).thenReturn(Collections.singletonList(expression1));
        when(expression1.getChildren()).thenReturn(Collections.singletonList(addr1));
        when(gate1.getChildren()).thenReturn(Arrays.asList(name1, param1, qubit1));

        ParseTreeNode param2 = mockNode("param", 2);
        ParseTreeNode expression21 = mockNode("expression", 2);
        ParseTreeNode expression22 = mockNode("expression", 2);
        ParseTreeNode expression23 = mockNode("expression", 2);
        when(param2.getChildren()).thenReturn(Collections.singletonList(expression21));
        when(expression21.getChildren()).thenReturn(Arrays.asList(expression22, expression23));
        when(gate2.getChildren()).thenReturn(Arrays.asList(name2, param2, qubit2));

        ParseTreeNode param3 = mockNode("param", 3);
        ParseTreeNode expression3 = mockNode("expression", 3);
        ParseTreeNode number = mockNode("number", 3);
        when(param3.getChildren()).thenReturn(Collections.singletonList(expression3));
        when(expression3.getChildren()).thenReturn(Collections.singletonList(number));
        when(gate3.getChildren()).thenReturn(Arrays.asList(name3, param3, qubit3));

        ClassifyLines cl = new ClassifyLines(root);
        Map<Integer, LineType> result = cl.classifyLines();
        assertEquals(LineType.CLASSICAL_INFLUENCES_QUANTUM, result.get(1));
        assertEquals(LineType.CLASSICAL_INFLUENCES_QUANTUM, result.get(2));
        assertEquals(LineType.QUANTUM, result.get(3));
    }

}
