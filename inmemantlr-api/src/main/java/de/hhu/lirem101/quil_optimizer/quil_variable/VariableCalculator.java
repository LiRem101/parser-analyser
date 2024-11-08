package de.hhu.lirem101.quil_optimizer.quil_variable;

import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

public class VariableCalculator {

    private final ParseTreeNode root;
    private final Set<QuantumVariable> quantumVariables = new HashSet<>();
    private final Set<ClassicalVariable> classicalVariables = new HashSet<>();
    private boolean calculated = false;

    /**
     * Constructor for the VariableCalculator. Takes the root of a ParseTree for **one** Quil instruction.
     * @param root The root of the ParseTree of the Quil instruction.
     */
    public VariableCalculator(ParseTreeNode root) {
        this.root = root;
    }

    /**
     * Returns a Set of the classical variables of this Quil instruction.
     */
    public Set<QuantumVariable> getQuantumVariables() {
        if (!calculated) {
            calculateVariables();
            calculated = true;
        }
        return quantumVariables;
    }

    /**
     * Returns a Set of the quantum variables of this Quil instruction.
     */
    public Set<ClassicalVariable> getClassicalVariables() {
        if (!calculated) {
            calculateVariables();
            calculated = true;
        }
        return classicalVariables;
    }

    /**
     * Calculates the quantum and classical variables of this Quil instruction. Goes through the ParseTree and saves
     * all variables. Additionally, with respect to the relevant nodes (see RelevantNodeRules), it decides the usage
     * type of each variable (see QuantumUsage and ClassicalUsage).
     */
    private void calculateVariables() {
        ArrayList<String> classicalVariableNames = new ArrayList<>();
        ArrayList<String> quantumVariableNames = new ArrayList<>();
        ParseTreeNode currentNode = root;
        Queue<ParseTreeNode> queue = new LinkedList<>();
        boolean isMeasurement = false;
        String[] classicalNode = new String[1]; // There is at maximum one classical node in an instruction
        while(currentNode != null) {
            String nodeRule = currentNode.getRule();

            // Check if the node is a measurement node
            if(RelevantNodeRules.measurementNodes().contains(nodeRule)) {
                isMeasurement = true;
            }

            // Check if node is a relevant classical node
            if(RelevantNodeRules.classicalUsage().containsKey(nodeRule)) {
                if (classicalNode[0] != null) {
                    throw new IllegalStateException("There are multiple relevant classical nodes in one instruction.");
                }
                classicalNode[0] = nodeRule;
            }

            // Check if the node holds a variable
            String classicalVariable = checkForClassicalVariable(currentNode);
            String quantumVariable = checkForQuantumVariable(currentNode);
            if (!classicalVariable.isEmpty()) {
                classicalVariableNames.add(classicalVariable);
            } else if (!quantumVariable.isEmpty()) {
                quantumVariableNames.add(quantumVariable);
            }

            // Poll queue
            queue.addAll(currentNode.getChildren());
            currentNode = queue.poll();
        }

        if (isMeasurement && classicalNode[0] != null) {
            throw new IllegalStateException("There is a measurement node and a relevant classical node in one instruction.");
        }

        calculateUsageTypes(classicalVariableNames, quantumVariableNames, isMeasurement, classicalNode[0]);
    }

    /**
     * Checks the label and rule of the node. If the node holds a classical variable, it is returned. Otherwise, an
     * empty string is returned.
     * @param node The node to check.
     * @return The classical variable name or an empty string.
     */
    private String checkForClassicalVariable(ParseTreeNode node) {
        switch (node.getRule()) {
            case "addr":
                return node.getLabel();
            case "memoryDescriptor":
                String param = node.getLabel();
                // Remove 'DECLARE' in the front
                param = param.substring(7);
                // Remove BIT, FLOAT, INTEGER, OCTET or REAL in the middle
                param = param.replaceAll("[BIT|FLOAT|INTEGER|OCTET|REAL]", "");
                // Replace "[1]" with "[0]"
                // TODO: Make this work for all numbers
                param = param.replaceAll("\\[1\\]", "[0]");
                return param;
        }
        return "";
    }

    /**
     * Checks the label and rule of the node. If the node holds a quantum variable, it is returned. Otherwise, an
     * empty string is returned.
     * @param node The node to check.
     * @return The quantum variable name or an empty string.
     */
    private String checkForQuantumVariable(ParseTreeNode node) {
        switch (node.getRule()) {
            case "qubit":
            case "qubitVariable":
                return node.getLabel();
        }
        return "";
    }

    /**
     * Calculates the usage type of classical and quantum variables.
     * @param classicalVariableNames The names of the classical variables.
     * @param quantumVariableNames The names of the quantum variables.
     * @param isMeasurement Whether the instruction contains a measurement node.
     * @param classicalNode The name of the relevant classical node.
     */
    private void calculateUsageTypes(ArrayList<String> classicalVariableNames, ArrayList<String> quantumVariableNames,
                                     boolean isMeasurement, String classicalNode) {
        if(classicalNode != null) {
            // Calculate the usage type of the classical variables
            List<ClassicalUsage> usages = RelevantNodeRules.classicalUsage().get(classicalNode);
            for(int i = 0; i < classicalVariableNames.size(); i++) {
                classicalVariables.add(new ClassicalVariable(classicalVariableNames.get(i), usages.get(i)));
            }
            // quantumVariables have to be empty
            if (!quantumVariableNames.isEmpty()) {
                throw new IllegalStateException("There are quantum variables in an instruction with a relevant classical node.");
            }
        } else if(isMeasurement) {
            for(String quantumVariable : quantumVariableNames) {
                quantumVariables.add(new QuantumVariable(quantumVariable, QuantumUsage.MEASURE));
            }
            for(String classicalVariable : classicalVariableNames) {
                classicalVariables.add(new ClassicalVariable(classicalVariable, ClassicalUsage.ASSIGNMENT));
            }
        } else {
            if(quantumVariableNames.size() == 1) {
                // There is only one quantum variable
                quantumVariables.add(new QuantumVariable(quantumVariableNames.get(0), QuantumUsage.SINGLE_GATE));
            } else {
                // There are multiple quantum variables
                for(String quantumVariable : quantumVariableNames) {
                    quantumVariables.add(new QuantumVariable(quantumVariable, QuantumUsage.MULTI_GATE));
                }
            }
            for(String classicalVariable : classicalVariableNames) {
                classicalVariables.add(new ClassicalVariable(classicalVariable, ClassicalUsage.USAGE));
            }
        }
    }

}