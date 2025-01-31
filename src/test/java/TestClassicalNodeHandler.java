import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.transformation.constant_folding.ClassicalNodeHandler;
import org.apache.commons.numbers.complex.Complex;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestClassicalNodeHandler {

    @Test
    void propagatesConstantCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVar = mock(ClassicalVariable.class);
        ParseTreeNode root = mock(ParseTreeNode.class);
        ParseTreeNode addrNode = mock(ParseTreeNode.class);
        when(addrNode.getRule()).thenReturn("addr");
        when(addrNode.getLabel()).thenReturn("test");
        when(addrNode.getParent()).thenReturn(root);
        when(root.getChildren()).thenReturn(new ArrayList<>(Collections.singletonList(addrNode)));
        when(instruction.getParseTreeNode()).thenReturn(root);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);
        ClassicalVariable notconstant = new ClassicalVariable("notconstant", ClassicalUsage.USAGE);
        ClassicalVariable assign = new ClassicalVariable("notconstant", ClassicalUsage.ASSIGNMENT);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);
        ArrayList<ClassicalVariable> classicalVars = new ArrayList<>();
        classicalVars.add(classicalVar);
        classicalVars.add(notconstant);
        classicalVars.add(assign);
        when(instruction.getClassicalParameters()).thenReturn(classicalVars);
        when(instruction.getLineText()).thenReturn("ADD notconstant test");
        when(classicalVar.isConstant()).thenReturn(true);
        when(classicalVar.getName()).thenReturn("test");
        when(classicalVar.getValue()).thenReturn(Complex.ofCartesian(3.0, 0.0));
        when(classicalVar.getUsage()).thenReturn(ClassicalUsage.USAGE);
        ClassicalNodeHandler handler = new ClassicalNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertTrue(result);
        verify(instruction).removeClassicalConnection(classicalVar);
        verify(instruction).setLineText("ADD notconstant 3.0");
    }

    @Test
    void doesNotPropagateForNonConstantVariable() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVar = mock(ClassicalVariable.class);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVar)));
        when(classicalVar.isConstant()).thenReturn(false);
        ClassicalNodeHandler handler = new ClassicalNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertFalse(result);
    }

    @Test
    void rewritesToMoveCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVar = mock(ClassicalVariable.class);
        ClassicalVariable usageClassicalVar = mock(ClassicalVariable.class);
        ParseTreeNode parseTreeNode = mock(ParseTreeNode.class);
        ArrayList<ClassicalVariable> classicalVars = new ArrayList<>();
        classicalVars.add(classicalVar);
        classicalVars.add(usageClassicalVar);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);
        when(instruction.getClassicalParameters()).thenReturn(classicalVars);
        when(instruction.getLineText()).thenReturn("NEG test");
        when(instruction.getParseTreeNode()).thenReturn(parseTreeNode);
        when(usageClassicalVar.getName()).thenReturn("test");
        when(usageClassicalVar.isConstant()).thenReturn(true);
        when(usageClassicalVar.getValue()).thenReturn(Complex.ofCartesian(3.0, 0.0));
        when(classicalVar.getUsage()).thenReturn(ClassicalUsage.ASSIGNMENT);
        when(classicalVar.getName()).thenReturn("test");
        when(usageClassicalVar.getUsage()).thenReturn(ClassicalUsage.USAGE);
        ClassicalNodeHandler handler = new ClassicalNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertTrue(result);
        verify(instruction).setLineText("MOVE test -3.0");
        verify(instruction, never()).removeClassicalConnection(classicalVar);
    }

    @Test
    void doesNotRewriteToMoveForNonConstantVariable() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVar = mock(ClassicalVariable.class);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVar)));
        when(classicalVar.isConstant()).thenReturn(false);
        ClassicalNodeHandler handler = new ClassicalNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertFalse(result);
    }

    @Test
    void throwsExceptionForNonClassicalInstruction() {
        InstructionNode instruction = mock(InstructionNode.class);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new ClassicalNodeHandler(instruction);
        });

        assertEquals("Instruction is not a classical instruction.", exception.getMessage());
    }
}