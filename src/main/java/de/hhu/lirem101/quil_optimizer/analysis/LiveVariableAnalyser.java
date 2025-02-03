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
import de.hhu.lirem101.quil_optimizer.quil_variable.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The LiveVariableAnalyser class is used to find dead variables in a program.
 * It uses a live variable analysis to find variables that are not used anymore in the program.
 * The analysis is done for both classical and quantum variables.
 */
public class LiveVariableAnalyser {

    /**
     * The types a variable can have in the live variable analysis.
     */
    private enum Variables {
        READOUT, USED, MEASURED, USED_MULTI_QUBIT
    }

    private final Set<String> readoutVariables = new HashSet<>();
    private final ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    private final ArrayList<ArrayList<BoxedVariableProperties>> variablesSetToDead = new ArrayList<>();
    private final int haltIndex;
    boolean calculated = false;

    /**
     * Constructor for the LiveVariableAnalyser.
     * @param instructions The list of lists of instructions of the program.
     * @param readoutVariables The list of readout variables of the program.
     * @param HaltIndex The index in the list of lists of the halt list of instructions.
     */
    public LiveVariableAnalyser(ArrayList<ArrayList<InstructionNode>> instructions, Set<String> readoutVariables, int HaltIndex) {
        this.instructions.addAll(instructions);
        this.readoutVariables.addAll(readoutVariables);
        for(ArrayList<InstructionNode> i : instructions) {
            variablesSetToDead.add(new ArrayList<>());
        }
        haltIndex = HaltIndex;
    }

    /**
     * Get the variables that are set to dead in the order of the instructions.
     * @return The variables that are set to dead in the order of the instructions.
     */
    public ArrayList<ArrayList<BoxedVariableProperties>> getVariablesSetToDead() {
        if(!calculated) {
            findDeadVariables();
        }
        return variablesSetToDead;
    }

    /**
     * Add information about dead variables into a JsonObjectBuilder.
     * @return The JsonArrayBuilder with the result information.
     */
    public JsonArrayBuilder addDeadVariablesToJson() {
        if (!calculated) {
            findDeadVariables();
        }
        JsonArrayBuilder jsonBuilder = Json.createArrayBuilder();
        for(ArrayList<BoxedVariableProperties> variables : variablesSetToDead) {
            JsonObjectBuilder deadVariables = Json.createObjectBuilder();
            for(BoxedVariableProperties variable : variables) {
                JsonObjectBuilder variableBuilder = Json.createObjectBuilder();
                variableBuilder.add("line", variable.line);
                variableBuilder.add("isQuantum", variable.isQuantum);
                deadVariables.add(variable.name, variableBuilder);
            }
            jsonBuilder.add(deadVariables);
        }
        return jsonBuilder;
    }

    /**
     * Set instructions to be dead if they can be shown to be dead.
     * Classical variables are dead if
     * - there are no usages (including the current checking) of non-dead instructions between them and the end of the
     *      program, and they are no readout variables or
     * - there are not usages between them and the next assignment of the variables.
     * If an instruction has a usage and an assignment of the same variable, the assignment is done after the usage.
     * Quantum variables are dead if
     * - there is no measurement of the variable between them and the end of the program and no multi-qubit gate that
     *      contains at least one variable.
     */
    public void findDeadVariables() {
        if(calculated || instructions.isEmpty()) {
            return;
        }
        calculated = true;
        HashMap<String, Variables> variables = new HashMap<>();
        for(String readoutVariable : readoutVariables) {
            variables.put(readoutVariable, Variables.READOUT);
        }
        for(int i = instructions.get(haltIndex).size() - 1; i >= 0; i--) {
            InstructionNode instruction = instructions.get(haltIndex).get(i);
            ArrayList<ClassicalVariable> classicalVariables = instruction.getClassicalParameters();
            // If the quantum variable is measured, it gets the measured type in the variables map
            // If there is a single qubit gate and the quantum variable will not be measured or in a multi-gate, it
            // is dead.
            // All quantum variables are alive if at least one of the variables in a multi-qubit gate is alive.
            checkQuantumVariables(variables, instruction, variablesSetToDead.get(haltIndex));
            // If the classical value is used or readout, it is not dead
            // If it was not already used or readout and is only assigned and not used, it is considered dead
            // Declarations are not considered dead here, because it would have to make sure that the value is not
            // used in any parts of the program anymore.
            checkClassicalVariables(variables, instruction, variablesSetToDead.get(haltIndex));
        }
    }

    /**
     * Check if quantum variables are dead.
     * @param variables The hashmap containing variables that have already been handled in previous instructions.
     * @param instruction The instruction we look at.
     * @param setToDeadVariables The variables that are set to dead in the current instruction.
     */
    private void checkQuantumVariables(HashMap<String, Variables> variables, InstructionNode instruction, ArrayList<BoxedVariableProperties> setToDeadVariables) {
        ArrayList<QuantumVariable> quantumVariables = instruction.getQuantumParameters();
        boolean allDead = true;
        boolean multiGate = false;
        for(QuantumVariable quantumVariable : quantumVariables) {
            String name = quantumVariable.getName();
            QuantumUsage type = quantumVariable.getUsage();
            boolean isDead = quantumVariable.isShownToBeDead();
            if(!isDead && type == QuantumUsage.MEASURE) {
                allDead = false;
                variables.put(name, Variables.MEASURED);
            } else if(!isDead && variables.containsKey(name)) {
                allDead = false;
                if(type == QuantumUsage.MULTI_GATE) {
                    multiGate = true;
                }
            }
        }
        for(QuantumVariable quantumVariable : quantumVariables) {
            String name = quantumVariable.getName();
            if(allDead) {
                quantumVariable.setDead();
                setToDeadVariables.add(new BoxedVariableProperties(name, instruction.getLine(), true, true));
            } else if(multiGate) {
                variables.put(name, Variables.USED_MULTI_QUBIT);
            }
        }
    }

    /**
     * Check if classical variables are dead.
     * @param variables The hashmap containing variables that have already been handled in previous instructions.
     * @param instruction The instruction we look at.
     * @param setToDeadVariables The variables that are set to dead in the current instruction.
     */
    private void checkClassicalVariables(HashMap<String, Variables> variables, InstructionNode instruction, ArrayList<BoxedVariableProperties> setToDeadVariables) {
        ArrayList<ClassicalVariable> classicalVariables = instruction.getClassicalParameters();
        List<ClassicalVariable> usedVariables = classicalVariables
                .stream()
                .filter(x -> x.getUsage() == ClassicalUsage.USAGE)
                .collect(Collectors.toList());
        List<ClassicalVariable> assignedVariables = classicalVariables
                .stream()
                .filter(x -> x.getUsage() == ClassicalUsage.ASSIGNMENT)
                .collect(Collectors.toList());
        for(ClassicalVariable var : assignedVariables) {
            String name = var.getName();
            if(!variables.containsKey(name)) {
                var.setDead();
                setToDeadVariables.add(new BoxedVariableProperties(name, instruction.getLine(), false, true));
            } else {
                variables.remove(name);
            }
        }
        for(ClassicalVariable var : usedVariables) {
            String name = var.getName();
            if(!var.isShownToBeDead()) {
                variables.put(name, Variables.USED);
            }
        }
    }

}
