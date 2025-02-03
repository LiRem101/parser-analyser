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

package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import org.apache.commons.numbers.complex.Complex;
import org.snt.inmemantlr.tree.ParseTreeNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class to propagate constant values through the instructions.
 */
public class ConstantPropagator {

    private final ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    private final ArrayList<ArrayList<BoxedVariableProperties>> newConstantValues = new ArrayList<>();
    boolean calculated = false;

    /**
     * A class to propagate constant values through the instructions.
     * @param instructions The instructions to propagate the constant values through as list of lists.
     */
    public ConstantPropagator(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions.addAll(instructions);
        for(ArrayList<InstructionNode> i : instructions) {
            newConstantValues.add(new ArrayList<>());
        }
    }

    /**
     * Returns the values that are newly determined to be constant.
     * @return The values that are newly determined to be constant.
     */
    public ArrayList<ArrayList<BoxedVariableProperties>> getNewConstantValues() {
        if(!calculated) {
            propagateConstants();
        }
        return newConstantValues;
    }

    /**
     * Add information about constant variables into a returned JsonArrayBuilder.
     * @return The JsonArrayBuilder with the information to.
     */
    public JsonArrayBuilder addConstantVariablesToJson() {
        if (!calculated) {
            propagateConstants();
        }
        JsonArrayBuilder allConstantVariables = Json.createArrayBuilder();
        for(int i = 0; i < newConstantValues.size(); i++) {
            JsonObjectBuilder theseConstantVariables = Json.createObjectBuilder();
            ArrayList<BoxedVariableProperties> constantValuesList = newConstantValues.get(i);
            ArrayList<Integer> lines = constantValuesList.stream()
                    .map(x -> x.line)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));
            for(int line : lines) {
                JsonObjectBuilder lineBuilder = Json.createObjectBuilder();
                ArrayList<BoxedVariableProperties> constantValues = constantValuesList.stream()
                        .filter(x -> x.line == line)
                        .collect(Collectors.toCollection(ArrayList::new));
                for(BoxedVariableProperties constantValue : constantValues) {
                    if(constantValue.isQuantum) {
                        lineBuilder.add(constantValue.name, constantValue.constantQuantumState.toString());
                    } else {
                        String name = constantValue.name + "; " + (constantValue.assignment ? "assignment" : "usage");
                        lineBuilder.add(name, constantValue.constantValue.toString());
                    }
                }
                theseConstantVariables.add(String.valueOf(line), lineBuilder);
            }
            allConstantVariables.add(theseConstantVariables);
        }
        return allConstantVariables;
    }

    /**
     * Propagate constants through the instructions. The constants are saved in the Variable objects and in the
     * newConstantValues list.
     */
    public void propagateConstants() {
        calculated = true;
        propagateClassicalValues();
        propagateQuantumValues();
    }

    /**
     * Propagate quantum constant values through the instructions. Used values are constant if they are used for the
     * first time (they get assigned X positive) or if they have been used before and had a constant value after the
     * application of the gate.
     */
    private void propagateQuantumValues() {
        if(instructions.isEmpty()) {
            return;
        }
        ArrayList<Set<String>> qubitsInUse = calculateQubitsInUse();
        ArrayList<Set<String>> qubitsUsedOnlyInOneBlock = calculateQubitsOnlyInOneBlock(qubitsInUse);

        calculateConstantQubits(instructions.get(0), qubitsInUse.get(0), newConstantValues.get(0));

        for(int i = 1; i < instructions.size(); i++) {
            calculateConstantQubits(instructions.get(i), qubitsUsedOnlyInOneBlock.get(i), newConstantValues.get(i));
        }

    }

    /**
     * Calculates which qubits are constant in the given instructions.
     * @param instructionList The list of instructions to calculate the constant qubits for.
     * @param newQuantumVars The quantum variables that should be considered in this method.
     * @param foundConstantValues The list of constant values that have been found so far.
     */
    private void calculateConstantQubits(ArrayList<InstructionNode> instructionList, Set<String> newQuantumVars, ArrayList<BoxedVariableProperties> foundConstantValues) {
        Map<String, QuantumCliffordState> qubitStates = new HashMap<>();
        Set<String> unknownQubits = new HashSet<>();
        for (InstructionNode instruction : instructionList) {
            ArrayList<QuantumVariable> quantumVariables = instruction.getQuantumParameters().stream()
                    .filter(variable -> newQuantumVars.contains(variable.getName()))
                    .collect(Collectors.toCollection(ArrayList::new));
            for (QuantumVariable variable : quantumVariables) {
                String variableName = variable.getName();
                QuantumCliffordState stateBeforeGate = variable.getCliffordStateBeforeGate();
                QuantumCliffordState stateAfterGate = variable.getCliffordStateBeforeGate();
                if(stateBeforeGate == null && !unknownQubits.contains(variableName)) {
                    QuantumCliffordState newState = qubitStates.getOrDefault(variableName, QuantumCliffordState.X_POSITIVE);
                    variable.setCliffordStateBeforeGate(newState);
                    qubitStates.remove(variableName);
                    foundConstantValues.add(new BoxedVariableProperties(variableName, instruction.getLine(), newState));
                }
                if(stateAfterGate != null) {
                    qubitStates.put(variableName, stateAfterGate);
                } else {
                    unknownQubits.add(variableName);
                }
            }
        }
    }

    /**
     * Propagate classical constant values through the instructions. Used values are constant if they are constant in a
     * previous instruction and had no new assignment since then.
     * They are also constant if they are assigned a constant value through the MOVE instruction.
     */
    private void propagateClassicalValues() {
        for(int i = 0; i < instructions.size(); i++) {
            Map<String, Complex> variableValues = new HashMap<>();
            for(InstructionNode instruction : instructions.get(i)) {
                checkIfClassicalValuesAreConstant(instruction, variableValues, newConstantValues.get(i));
            }
        }
    }

    /**
     * Check if the classical values are constant and save the new constant values in the newConstantValues list.
     * @param instruction The instruction to check.
     * @param variableValues The values of the variables that are known to be constant.
     * @param foundConstantValues The list of constant values that have been found so far.
     */
    private void checkIfClassicalValuesAreConstant(InstructionNode instruction, Map<String, Complex> variableValues, ArrayList<BoxedVariableProperties> foundConstantValues) {
        ArrayList<ClassicalVariable> classicalVariables = instruction.getClassicalParameters();
        ParseTreeNode ptNode = instruction.getParseTreeNode();
        ParseTreeNode moveNode = getMoveNode(ptNode);
        if(moveNode != null) {
            handleMoveNode(classicalVariables, moveNode, variableValues, foundConstantValues);
        }
        for(ClassicalVariable variable : classicalVariables) {
            String variableName = variable.getName();
            if(variable.isConstant()) {
                variableValues.put(variableName, variable.getValue());
            } else if(variable.getUsage() == ClassicalUsage.USAGE && variableValues.containsKey(variableName)) {
                variable.setValue(variableValues.get(variableName));
                boolean assignment = variable.getUsage() == ClassicalUsage.ASSIGNMENT;
                BoxedVariableProperties boxedVariable = new BoxedVariableProperties(variableName, instruction.getLine(), variableValues.get(variableName), assignment);
                foundConstantValues.add(boxedVariable);
            } else if(variable.getUsage() == ClassicalUsage.ASSIGNMENT) {
                variableValues.remove(variableName);
            }
        }
    }

    /**
     * Handle the MOVE node. If the value is a constant, the variable is set to this constant value.
     * @param classicalVariables The classical variables that have constant values.
     * @param moveNode The MOVE node.
     * @param variableValues The values of the variables that are known to be constant.
     * @param foundConstantValues The list of constant values that have been found so far.
     */
    private void handleMoveNode(ArrayList<ClassicalVariable> classicalVariables, ParseTreeNode moveNode, Map<String, Complex> variableValues, ArrayList<BoxedVariableProperties> foundConstantValues) {
        ClassicalVariable assignedVariable = classicalVariables.get(0);
        ParseTreeNode numberVar = moveNode.getChildren().get(1);
        Complex value = Complex.ofCartesian(0, 0);
        boolean foundValue = false;

        if(numberVar.getRule().equals("addr")) {
            ClassicalVariable usedVariable = classicalVariables.get(1);
            if(usedVariable.isConstant()) {
                value = usedVariable.getValue();
                foundValue = true;
            }
        } else if(numberVar.getRule().equals("number")) {
            String valueString = numberVar.getLabel();
            boolean minus = valueString.startsWith("-");
            if(valueString.endsWith("pi")) {
                value = value.add(Complex.ofCartesian(Math.PI, 0));
                value = value.multiply(Complex.ofCartesian(minus ? -1 : 1, 0));
            }else if(valueString.endsWith("i")) {
                value = value.add(Complex.ofCartesian(0, 1));
                value = value.multiply(Complex.ofCartesian(minus ? -1 : 1, 0));
            } else {
                value = value.add(Complex.ofCartesian(Double.parseDouble(valueString), 0));
            }
            foundValue = true;
        }
        if(foundValue) {
            assignedVariable.setValue(value);
            BoxedVariableProperties boxedVariable = new BoxedVariableProperties(assignedVariable.getName(), moveNode.getLine(), value, true);
            foundConstantValues.add(boxedVariable);
            variableValues.put(assignedVariable.getName(), value);
        }
    }

    /**
     * Gets the MOVE node from a ParseTreeNode and its branches. Returns null if there is no MOVE node.
     * @param ptNode The ParseTreeNode to get the MOVE node from.
     * @return The MOVE node or null if there is no MOVE node.
     */
    private ParseTreeNode getMoveNode(ParseTreeNode ptNode) {
        ParseTreeNode current = ptNode;
        Queue<ParseTreeNode> queue = new LinkedList<>();
        while(current != null) {
            if(current.getRule().equals("move")) {
                return current;
            }
            queue.addAll(current.getChildren());
            current = queue.poll();
        }
        return null;
    }

    /**
     * Calculate which qubits are used in which instruction blocks.
     * @return A list of sets of qubits that are used in each instruction block.
     */
    private ArrayList<Set<String>> calculateQubitsInUse() {
        ArrayList<Set<String>> qubitsInUse = new ArrayList<>();
        for (ArrayList<InstructionNode> instructionList : instructions) {
            Set<String> qubits = new HashSet<>();
            for (InstructionNode instruction : instructionList) {
                qubits.addAll(instruction.getQuantumParameters().stream().map(QuantumVariable::getName).collect(Collectors.toSet()));
            }
            qubitsInUse.add(qubits);
        }
        return qubitsInUse;
    }

    /**
     * Calculate which qubits are only used in one instruction block.
     * @param qubitsInUse The qubits that are used in each instruction block.
     * @return A list of sets of qubits that are only used in the respective instruction block.
     */
    private ArrayList<Set<String>> calculateQubitsOnlyInOneBlock(ArrayList<Set<String>> qubitsInUse) {
        ArrayList<String> allQubits = new ArrayList<>();
        for(Set<String> qubits : qubitsInUse) {
            allQubits.addAll(qubits);
        }
        Set<String> qubitsUsedOnce = allQubits.stream()
                .filter(qubit -> Collections.frequency(allQubits, qubit) == 1)
                .collect(Collectors.toSet());
        ArrayList<Set<String>> qubitsInOnlyOneBlock = new ArrayList<>();
        for(Set<String> qubitsInUseSet : qubitsInUse) {
            qubitsInOnlyOneBlock.add(new HashSet<>());
            Set<String> onceUsedQubitsInThisBlock = qubitsInUseSet.stream()
                    .filter(qubitsUsedOnce::contains)
                    .collect(Collectors.toSet());
            qubitsInOnlyOneBlock.get(qubitsInOnlyOneBlock.size() - 1).addAll(onceUsedQubitsInThisBlock);
        }
        return qubitsInOnlyOneBlock;
    }
}
