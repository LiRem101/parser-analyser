/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-FileCopyrightText: 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-License-Identifier: MIT
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
import de.hhu.lirem101.quil_optimizer.analysis.BoxedVariableProperties;
import de.hhu.lirem101.quil_optimizer.analysis.LiveVariableAnalyser;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestLiveVariableAnalyser {

    @Test
    void returnsEmptyVariablesSetToDeadWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesDeadVariablesCorrectlyForSingleInstruction() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVariable = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("c1", result.get(0).get(0).name);
    }

    @Test
    void handlesReadoutVariablesCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        ClassicalVariable classicalVariable = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        when(instruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>(Collections.singletonList("c1"));
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertTrue(result.get(0).isEmpty());
    }

    @Test
    void calculatesDeadQuantumVariablesCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable = new QuantumVariable("q1", QuantumUsage.SINGLE_GATE);
        when(instruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable)));
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("q1", result.get(0).get(0).name);
    }

    @Test
    void handlesMultiQubitGatesCorrectlyDeadQubits() {
        InstructionNode instruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable1 = new QuantumVariable("q1", QuantumUsage.MULTI_GATE);
        QuantumVariable quantumVariable2 = new QuantumVariable("q2", QuantumUsage.MULTI_GATE);
        ArrayList<QuantumVariable> quantumVariables = new ArrayList<>();
        quantumVariables.add(quantumVariable1);
        quantumVariables.add(quantumVariable2);
        when(instruction.getQuantumParameters()).thenReturn(quantumVariables);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        Set<String> readoutVariables = new HashSet<>();
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).size());
        ArrayList<String> names = new ArrayList<>(Arrays.asList(result.get(0).get(0).name, result.get(0).get(1).name));
        assertTrue(names.contains("q1"));
        assertTrue(names.contains("q2"));
    }

    @Test
    void handlesMultiQubitGatesCorrectly() {
        InstructionNode multiGateInstruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable1 = new QuantumVariable("q1", QuantumUsage.MULTI_GATE);
        QuantumVariable quantumVariable2 = new QuantumVariable("q2", QuantumUsage.MULTI_GATE);
        ArrayList<QuantumVariable> quantumVariables = new ArrayList<>();
        quantumVariables.add(quantumVariable1);
        quantumVariables.add(quantumVariable2);
        when(multiGateInstruction.getQuantumParameters()).thenReturn(quantumVariables);

        InstructionNode measureInstruction = mock(InstructionNode.class);
        QuantumVariable quantumVariable3 = new QuantumVariable("q1", QuantumUsage.MEASURE);
        ClassicalVariable classicalVariable = new ClassicalVariable("c1", ClassicalUsage.ASSIGNMENT);
        when(measureInstruction.getQuantumParameters()).thenReturn(new ArrayList<>(Collections.singletonList(quantumVariable3)));
        when(measureInstruction.getClassicalParameters()).thenReturn(new ArrayList<>(Collections.singletonList(classicalVariable)));

        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Arrays.asList(multiGateInstruction, measureInstruction))));
        Set<String> readoutVariables = new HashSet<>(Collections.singletonList("c1"));
        LiveVariableAnalyser analyser = new LiveVariableAnalyser(instructions, readoutVariables, 0);

        ArrayList<ArrayList<BoxedVariableProperties>> result = analyser.getVariablesSetToDead();

        assertTrue(result.get(0).isEmpty());
    }
}