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

import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.analysis.BoxedVariableProperties;
import de.hhu.lirem101.quil_optimizer.analysis.ConstantPropagator;
import de.hhu.lirem101.quil_optimizer.quil_variable.*;
import org.apache.commons.numbers.complex.Complex;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestConstantPropagator {

    @Test
    void returnsEmptyNewConstantValuesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ConstantPropagator propagator = new ConstantPropagator(instructions);

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertTrue(result.isEmpty());
    }

    @Test
    void propagatesClassicalConstantsCorrectly() {
        InstructionNode instruction1 = mock(InstructionNode.class);
        InstructionNode instruction2 = mock(InstructionNode.class);
        ClassicalVariable classicalVariableAssignment = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        ClassicalVariable classicalVariableUsage = new ClassicalVariable("c1", ClassicalUsage.USAGE);
        classicalVariableAssignment.setValue(Complex.ofCartesian(1, 0));
        when(instruction1.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariableAssignment)));
        when(instruction2.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariableUsage)));
        ArrayList<InstructionNode> instructions = new ArrayList<>();
        instructions.add(instruction1);
        instructions.add(instruction2);
        ConstantPropagator propagator = new ConstantPropagator(new ArrayList<>(Collections.singletonList(instructions)));

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("c1", result.get(0).get(0).name);
        assertEquals(Complex.ofCartesian(1, 0), result.get(0).get(0).constantValue);
    }

    @Test
    void propagatesQuantumConstantsCorrectlyWithConstantValue() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable = new QuantumVariable("q1", QuantumUsage.SINGLE_GATE);
        quantumVariable.setCliffordStateBeforeGate(QuantumCliffordState.X_POSITIVE);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        ConstantPropagator propagator = new ConstantPropagator(instructions);

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isEmpty());
    }

    @Test
    void propagatesQuantumConstantsCorrectlyWithoutConstantValue() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable = new QuantumVariable("q1", QuantumUsage.SINGLE_GATE);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        ConstantPropagator propagator = new ConstantPropagator(instructions);

        ArrayList<ArrayList<BoxedVariableProperties>> result = propagator.getNewConstantValues();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("q1", result.get(0).get(0).name);
        assertEquals(QuantumCliffordState.X_POSITIVE, result.get(0).get(0).constantQuantumState);
    }
}