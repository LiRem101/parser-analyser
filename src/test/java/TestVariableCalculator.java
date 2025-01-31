import de.hhu.lirem101.quil_optimizer.quil_variable.*;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VariableCalculatorTest {

    @Test
    void returnsEmptyQuantumVariablesWhenNoVariables() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        when(root.getChildren()).thenReturn(Collections.emptyList());
        when(root.getRule()).thenReturn("instruction");
        VariableCalculator calculator = new VariableCalculator(root);

        Set<QuantumVariable> result = calculator.getQuantumVariables();

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyClassicalVariablesWhenNoVariables() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        when(root.getChildren()).thenReturn(Collections.emptyList());
        when(root.getRule()).thenReturn("instruction");
        VariableCalculator calculator = new VariableCalculator(root);

        Set<ClassicalVariable> result = calculator.getClassicalVariables();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesQuantumVariablesCorrectly() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode quantumNode = mock(ParseTreeNode.class);
        when(root.getRule()).thenReturn("instruction");
        when(root.getChildren()).thenReturn(Collections.singletonList(quantumNode));
        when(quantumNode.getRule()).thenReturn("qubit");
        when(quantumNode.getLabel()).thenReturn("q1");
        VariableCalculator calculator = new VariableCalculator(root);

        Set<QuantumVariable> result = calculator.getQuantumVariables();

        assertEquals(1, result.size());

        QuantumVariable variable = result.iterator().next();
        assertEquals("q1", variable.getName());
        assertEquals(QuantumUsage.SINGLE_GATE, variable.getUsage());
    }

    @Test
    void calculatesClassicalVariablesCorrectly() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode classicalNode = mock(ParseTreeNode.class);
        when(root.getRule()).thenReturn("instruction");
        when(root.getChildren()).thenReturn(Collections.singletonList(classicalNode));
        when(classicalNode.getRule()).thenReturn("addr");
        when(classicalNode.getLabel()).thenReturn("c1");
        VariableCalculator calculator = new VariableCalculator(root);

        Set<ClassicalVariable> result = calculator.getClassicalVariables();

        assertEquals(1, result.size());

        ClassicalVariable variable = result.iterator().next();
        assertEquals("c1", variable.getName());
        assertEquals(ClassicalUsage.USAGE, variable.getUsage());
    }

    @Test
    void throwsExceptionForMultipleRelevantClassicalNodes() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode classicalNode1 = mock(ParseTreeNode.class);
        ParseTreeNode classicalNode2 = mock(ParseTreeNode.class);
        when(root.getRule()).thenReturn("instruction");
        when(root.getChildren()).thenReturn(Arrays.asList(classicalNode1, classicalNode2));
        when(classicalNode1.getRule()).thenReturn("NEG");
        when(classicalNode2.getRule()).thenReturn("memoryDescriptor");
        VariableCalculator calculator = new VariableCalculator(root);

        IllegalStateException exception = assertThrows(IllegalStateException.class, calculator::getClassicalVariables);

        assertEquals("There are multiple relevant classical nodes in one instruction.", exception.getMessage());
    }

    @Test
    void throwsExceptionForMeasurementAndRelevantClassicalNode() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode measurementNode = mock(ParseTreeNode.class);
        ParseTreeNode classicalNode = mock(ParseTreeNode.class);
        when(root.getRule()).thenReturn("instruction");
        when(root.getChildren()).thenReturn(Arrays.asList(measurementNode, classicalNode));
        when(measurementNode.getRule()).thenReturn("measure");
        when(classicalNode.getRule()).thenReturn("NEG");
        VariableCalculator calculator = new VariableCalculator(root);

        IllegalStateException exception = assertThrows(IllegalStateException.class, calculator::getClassicalVariables);

        assertEquals("There is a measurement node and a relevant classical node in one instruction.", exception.getMessage());
    }

    @Test
    void calculatesUsageTypesCorrectlyForMultipleQuantumVariables() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode quantumNode1 = mock(ParseTreeNode.class);
        ParseTreeNode quantumNode2 = mock(ParseTreeNode.class);
        when(root.getRule()).thenReturn("instruction");
        when(root.getChildren()).thenReturn(Arrays.asList(quantumNode1, quantumNode2));
        when(quantumNode1.getRule()).thenReturn("qubit");
        when(quantumNode1.getLabel()).thenReturn("q1");
        when(quantumNode2.getRule()).thenReturn("qubit");
        when(quantumNode2.getLabel()).thenReturn("q2");
        VariableCalculator calculator = new VariableCalculator(root);

        Set<QuantumVariable> result = calculator.getQuantumVariables();

        assertEquals(2, result.size());
        for (QuantumVariable variable : result) {
            assertEquals(QuantumUsage.MULTI_GATE, variable.getUsage());
        }
    }

    @Test
    void calculatesUsageTypesCorrectlyForClassicalVariables() {
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode quantumNode1 = mock(ParseTreeNode.class);
        ParseTreeNode quantumNode2 = mock(ParseTreeNode.class);
        when(root.getRule()).thenReturn("move");
        when(root.getChildren()).thenReturn(Arrays.asList(quantumNode1, quantumNode2));
        when(quantumNode1.getRule()).thenReturn("addr");
        when(quantumNode1.getLabel()).thenReturn("assign");
        when(quantumNode2.getRule()).thenReturn("addr");
        when(quantumNode2.getLabel()).thenReturn("use");
        VariableCalculator calculator = new VariableCalculator(root);

        Set<ClassicalVariable> result = calculator.getClassicalVariables();

        assertEquals(2, result.size());
        ClassicalVariable assign = result.stream().filter(x -> x.getName().equals("assign")).findFirst().orElse(null);
        ClassicalVariable use = result.stream().filter(x -> x.getName().equals("use")).findFirst().orElse(null);
        assertEquals(ClassicalUsage.ASSIGNMENT, assign.getUsage());
        assertEquals(ClassicalUsage.USAGE, use.getUsage());
    }
}
