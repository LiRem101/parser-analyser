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

package de.hhu.lirem101.quil_optimizer.transformation.constant_folding;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.numbers.complex.Complex;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.List;
import java.util.stream.Collectors;

import static de.hhu.lirem101.quil_optimizer.transformation.constant_folding.ParseTreeCreator.replaceVariableByConstant;

public class ClassicalNodeHandler implements Handler {

    private final InstructionNode instruction;
    private boolean changed;
    private boolean calculated = false;

    public ClassicalNodeHandler(InstructionNode instruction) {
        if(instruction.getLineType() != LineType.CLASSICAL) {
            throw new IllegalArgumentException("Instruction is not a classical instruction.");
        }
        this.instruction = instruction;
    }

    @Override
    public boolean propagateConstant() {
        if(calculated) {
            return changed;
        }
        calculated = true;
        changed = replaceConstants();
        changed |= rewriteToMove();
        return changed;
    }

    /**
     * Replace all constant usage values in the instruction with the constant value.
     * @return True if the instruction was changed, false otherwise.
     */
    private boolean replaceConstants() {
        List<ClassicalVariable> variables = instruction.getClassicalParameters();

        // Make sure only constants that do not get assigned a value are replaced
        List<String> usageVariableNames = variables.stream()
                .filter(x -> x.getUsage() == ClassicalUsage.ASSIGNMENT)
                .map(ClassicalVariable::getName)
                .collect(Collectors.toList());

        List<ClassicalVariable> constants = variables.stream()
                .filter(ClassicalVariable::isConstant)
                .filter(x -> x.getUsage() == ClassicalUsage.USAGE)
                .filter(x -> !usageVariableNames.contains(x.getName()))
                .collect(Collectors.toList());

        for(ClassicalVariable cv : constants) {
            Complex value = cv.getValue();
            if(Double.compare(value.imag(), 0.0) > 0.000001) {
                throw new NotImplementedException("Complex numbers are not supported for parametrized gates.");
            }
            replaceVariableByConstant(instruction.getParseTreeNode(), cv.getName(), value.imag());

            String text = instruction.getLineText().replace(cv.getName(), Double.toString(value.real()));
            instruction.setLineText(text);
            instruction.removeClassicalConnection(cv);
        }

        return !constants.isEmpty();
    }

    /**
     * If an instruction is evaluable, rewrite the instruction to a MOVE instruction.
     * @return True if the instruction was changed, false otherwise.
     */
    private boolean rewriteToMove() {
        // The instruction is evaluable if there is no non-constant usage value.
        List<ClassicalVariable> variables = instruction.getClassicalParameters();

        ClassicalVariable assignmentVariable = variables.stream()
                .filter(x -> x.getUsage() == ClassicalUsage.ASSIGNMENT)
                .findFirst()
                .orElse(null);

        ClassicalVariable usageVariable = variables.stream()
                .filter(x -> x.getUsage() == ClassicalUsage.USAGE)
                .filter(x -> x.getName().equals(assignmentVariable.getName()))
                .findFirst()
                .orElse(null);

        List<ClassicalVariable> nonConstants = variables.stream()
                .filter(x -> !x.isConstant())
                .filter(x -> x.getUsage() == ClassicalUsage.USAGE)
                .collect(Collectors.toList());

        if(assignmentVariable == null || !nonConstants.isEmpty() || usageVariable == null || !usageVariable.isConstant()) {
            return false;
        }

        double value = usageVariable.getValue().real();
        if(Math.abs(usageVariable.getValue().imag()) > 0.000001) {
            throw new NotImplementedException("Complex numbers are not supported for parametrized gates.");
        }

        double result;
        try {
            result = applyOperation(instruction.getLineText(), value);
        } catch(IllegalArgumentException e) {
            return false;
        }

        ParseTreeNode moveNode = ParseTreeCreator.createMoveInstruction(assignmentVariable, result, instruction.getParseTreeNode());
        instruction.setParseTreeNode(moveNode);
        instruction.setLineType(LineType.CLASSICAL);
        String newText = "MOVE " + assignmentVariable.getName() + " " + result;
        instruction.setLineText(newText);
        return true;
    }

    /**
     * Apply classical operations to instructions that have only the assignment value as non-constant left.
     * Returns the result of the operation.
     * @param instructionText The instruction text.
     * @param value The value of the operation left.
     * @return The result of the operation.
     */
    private double applyOperation(String instructionText, double value) {
        String[] parts = instructionText.split(" ");
        if(parts.length < 2) {
            throw new IllegalArgumentException("Instruction is not a classical operation.");
        }
        int intValue = Math.round((float) value);
        double result;
        switch(parts[0]) {
            case "NEG":
                result = -value;
                break;
            case "NOT":
                result = ~intValue;
                break;
            case "TRUE":
                result = 1;
                break;
            case "FALSE":
                result = 0;
                break;
            case "AND":
                result = intValue & Integer.parseInt(parts[2]);
                break;
            case "OR":
                result = intValue | Integer.parseInt(parts[2]);
                break;
            case "XOR":
                result = intValue ^ Integer.parseInt(parts[2]);
                break;
            case "ADD":
                result = value + Double.parseDouble(parts[2]);
                break;
            case "SUB":
                result = value - Double.parseDouble(parts[2]);
                break;
            case "MUL":
                result = value * Double.parseDouble(parts[2]);
                break;
            case "DIV":
                result = value / Double.parseDouble(parts[2]);
                break;
            default:
                throw new IllegalArgumentException("Instruction is not a classical operation.");
        }
        return result;
    }
}
