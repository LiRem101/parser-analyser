import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import de.hhu.lirem101.quil_optimizer.transformation.constant_folding.QuantumHandler;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestQuantumHandler {

    @Test
    void propagatesConstantCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable qv = new QuantumVariable("var", QuantumUsage.SINGLE_GATE);
        qv.setCliffordStateBeforeGate(QuantumCliffordState.X_POSITIVE);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(qv)));
        ParseTreeNode pt = mock(ParseTreeNode.class);
        when(instruction.getParseTreeNode()).thenReturn(pt);
        when(pt.getLabel()).thenReturn("X");
        when(pt.getRule()).thenReturn("name");

        QuantumHandler handler = new QuantumHandler(instruction);

        QuantumCliffordState result = handler.propagateConstant();

        assertNotNull(result);
        assertEquals(QuantumCliffordState.X_POSITIVE, result);
    }

    @Test
    void returnsNullForNonSingleGateUsage() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable qv = new QuantumVariable("var", QuantumUsage.MULTI_GATE);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(qv)));
        QuantumHandler handler = new QuantumHandler(instruction);

        QuantumCliffordState result = handler.propagateConstant();

        assertNull(result);
    }

    @Test
    void returnsNullForNullCliffordStateBeforeGate() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable qv = new QuantumVariable("var", QuantumUsage.SINGLE_GATE);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(qv)));
        QuantumHandler handler = new QuantumHandler(instruction);

        QuantumCliffordState result = handler.propagateConstant();

        assertNull(result);
    }

    @Test
    void returnsNullForNonQuantumInstruction() {
        InstructionNode instruction = mock(InstructionNode.class);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new QuantumHandler(instruction);
        });

        assertEquals("Instruction is not a quantum instruction.", exception.getMessage());
    }

    @Test
    void returnsNullForNullGateName() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable qv = new QuantumVariable("var", QuantumUsage.SINGLE_GATE);
        qv.setCliffordStateBeforeGate(QuantumCliffordState.X_POSITIVE);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(qv)));
        ParseTreeNode pt = mock(ParseTreeNode.class);
        when(instruction.getParseTreeNode()).thenReturn(pt);
        when(pt.getLabel()).thenReturn("NonClifford");
        when(pt.getRule()).thenReturn("name");

        QuantumHandler handler = new QuantumHandler(instruction);

        QuantumCliffordState result = handler.propagateConstant();

        assertNull(result);
    }
}