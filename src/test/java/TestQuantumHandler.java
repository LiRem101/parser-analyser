/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

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

        boolean result = handler.propagateConstant();

        assertTrue(result);
        assertEquals(QuantumCliffordState.X_POSITIVE, qv.getCliffordStateAfterGate());
    }

    @Test
    void returnsNullForNonSingleGateUsage() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable qv = new QuantumVariable("var", QuantumUsage.MULTI_GATE);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(qv)));
        QuantumHandler handler = new QuantumHandler(instruction);

        boolean result = handler.propagateConstant();

        assertFalse(result);
    }

    @Test
    void returnsNullForNullCliffordStateBeforeGate() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable qv = new QuantumVariable("var", QuantumUsage.SINGLE_GATE);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(qv)));
        QuantumHandler handler = new QuantumHandler(instruction);

        boolean result = handler.propagateConstant();

        assertFalse(result);
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

        boolean result = handler.propagateConstant();

        assertFalse(result);
    }
}