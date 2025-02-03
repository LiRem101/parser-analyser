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
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import de.hhu.lirem101.quil_optimizer.transformation.constant_folding.HybridNodeHandler;
import org.apache.commons.numbers.complex.Complex;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestHybridNodeHandler {

    @Test
    void propagatesConstantForParametrizedGateCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVar = mock(ClassicalVariable.class);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(instruction.getLineText()).thenReturn("RX(test) 0");
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVar)));
        when(classicalVar.isConstant()).thenReturn(true);
        when(classicalVar.getName()).thenReturn("test");
        when(classicalVar.getValue()).thenReturn(Complex.ofCartesian(3.0, 0.0));
        HybridNodeHandler handler = new HybridNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertTrue(result);
        verify(instruction).removeClassicalConnection(classicalVar);
        verify(instruction).setLineType(LineType.QUANTUM);
        verify(instruction).setLineText("RX(3.0) 0");
    }

    @Test
    void doesNotPropagateForNonConstantParametrizedGate() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVar = mock(ClassicalVariable.class);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL_INFLUENCES_QUANTUM);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVar)));
        when(classicalVar.isConstant()).thenReturn(false);
        HybridNodeHandler handler = new HybridNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertFalse(result);
    }

    @Test
    void propagatesMeasurementCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVar = mock(QuantumVariable.class);
        ParseTreeNode pt = mock(ParseTreeNode.class);
        ClassicalVariable cv = new ClassicalVariable("test", ClassicalUsage.ASSIGNMENT);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM_INFLUENCES_CLASSICAL);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVar)));
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(cv)));
        when(instruction.getParseTreeNode()).thenReturn(pt);
        when(quantumVar.getCliffordStateBeforeGate()).thenReturn(QuantumCliffordState.Z_POSITIVE);
        HybridNodeHandler handler = new HybridNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertTrue(result);
        verify(instruction).setLineType(LineType.CLASSICAL);
        verify(instruction).setLineText("MOVE test 0");
        verify(instruction).removeQuantumConnection(quantumVar);
        assertEquals(cv.getValue(), Complex.ofCartesian(0.0, 0.0));
    }

    @Test
    void doesNotPropagateMeasurementForNonZBasis() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVar = mock(QuantumVariable.class);
        ClassicalVariable cv = new ClassicalVariable("test", ClassicalUsage.ASSIGNMENT);
        when(instruction.getLineType()).thenReturn(LineType.QUANTUM_INFLUENCES_CLASSICAL);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVar)));
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(cv)));
        when(quantumVar.getCliffordStateBeforeGate()).thenReturn(QuantumCliffordState.X_POSITIVE);
        HybridNodeHandler handler = new HybridNodeHandler(instruction);

        boolean result = handler.propagateConstant();

        assertFalse(result);
    }

    @Test
    void throwsExceptionForNonHybridInstruction() {
        InstructionNode instruction = mock(InstructionNode.class);
        when(instruction.getLineType()).thenReturn(LineType.CLASSICAL);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new HybridNodeHandler(instruction);
        });

        assertEquals("Instruction is not a hybrid instruction.", exception.getMessage());
    }
}