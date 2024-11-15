package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.*;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class LiveVariableAnalyser {
    private enum Variables {
        READOUT, USED, MEASURED, USED_MULTI_QUBIT
    }

    private final Set<String> readoutVariables = new HashSet<>();
    private final ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    private final ArrayList<ArrayList<BoxedVariableProperties>> variablesSetToDead = new ArrayList<>();
    boolean calculated = false;


    public LiveVariableAnalyser(ArrayList<ArrayList<InstructionNode>> instructions, Set<String> readoutVariables) {
        this.instructions.addAll(instructions);
        this.readoutVariables.addAll(readoutVariables);
        for(ArrayList<InstructionNode> i : instructions) {
            variablesSetToDead.add(new ArrayList<>());
        }
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
     * Add information about dead variables into JsonObjectBuilder.
     * @param jsonBuilder The JsonObjectBuilder to add the information to.
     */
    public void addDeadVariablesToJson(JsonObjectBuilder jsonBuilder) {
        if (!calculated) {
            findDeadVariables();
        }
        for(ArrayList<BoxedVariableProperties> variables : variablesSetToDead) {
            JsonObjectBuilder deadVariables = Json.createObjectBuilder();
            for(BoxedVariableProperties variable : variables) {
                JsonObjectBuilder variableBuilder = Json.createObjectBuilder();
                variableBuilder.add("line", variable.line);
                variableBuilder.add("isQuantum", variable.isQuantum);
                deadVariables.add(variable.name, variableBuilder);
            }
            jsonBuilder.add("DeadVariableAnalysis", deadVariables);
        }
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
        if(calculated) {
            return;
        }
        calculated = true;
        for(int j = 0; j < instructions.size(); j++) {
             HashMap<String, Variables> variables = new HashMap<>();
             for(String readoutVariable : readoutVariables) {
                 variables.put(readoutVariable, Variables.READOUT);
             }
             for(int i = instructions.get(j).size() - 1; i >= 0; i--) {
                 InstructionNode instruction = instructions.get(j).get(i);
                 ArrayList<ClassicalVariable> classicalVariables = instruction.getClassicalParameters();
                 // If the quantum variable is measured, it gets the measured type in the variables map
                 // If there is a single qubit gate and the quantum variable will not be measured or in a multi-gate, it
                 // is dead.
                 // All quantum variables are alive if at least one of the variables in a multi-qubit gate is alive.
                 checkQuantumVariables(variables, instruction, variablesSetToDead.get(j));
                 // If the classical value is used or readout, it is not dead
                 // If it was not already used or readout and is only assigned and not used, it is considered dead
                 // Declarations are not considered dead here, because it would have to make sure that the value is not
                 // used in any parts of the program anymore.
                 checkClassicalVariables(variables, instruction, variablesSetToDead.get(j));
             }
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
