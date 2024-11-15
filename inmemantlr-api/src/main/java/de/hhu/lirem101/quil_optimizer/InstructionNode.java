package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.DirectedGraphNode;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.VariableCalculator;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;
import java.util.stream.Collectors;

public class InstructionNode implements DirectedGraphNode<InstructionNode> {

    private static class connectedInstructions {
        private final ArrayList<InstructionNode> previous = new ArrayList<>();
        private final ArrayList<InstructionNode> next = new ArrayList<>();
    }

    private final int line;
    private final LineType type;
    private ParseTreeNode ptNode;
    private boolean shownToBeDead = false;
    private final Map<QuantumVariable, connectedInstructions> quantumParameters = new HashMap<>();
    private final Map<ClassicalVariable, connectedInstructions> classicalParameters = new HashMap<>();

    public InstructionNode(int line, LineType type) {
        this.line = line;
        this.type = type;
    }

    /**
     * Returns all previous InstructionNodes from both the quantum and classical parameters.
     * @return ArrayList of previous InstructionNodes
     */
    @Override
    public ArrayList<InstructionNode> getBranches() {
        Set<InstructionNode> branches = quantumParameters
                .values()
                .stream()
                .flatMap(x -> x.previous.stream())
                .collect(Collectors.toSet());
        branches.addAll(classicalParameters
                .values()
                .stream()
                .flatMap(x -> x.previous.stream())
                .collect(Collectors.toSet()));
        return new ArrayList<>(branches);
    }

    @Override
    public ArrayList<Integer> getCodelines() {
        return new ArrayList<>(Collections.singletonList(line));
    }

    @Override
    public String getName() {
        return "Line " + line;
    }

    @Override
    public LineType getLineType() {
        return type;
    }

    public boolean getShownToBeDead() {
        return shownToBeDead;
    }

    public int getLine() {
        return line;
    }

    public ParseTreeNode getParseTreeNode() {
        return ptNode;
    }

    public ArrayList<String> getParameters() {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.addAll(quantumParameters.keySet().stream().map(QuantumVariable::getName).collect(Collectors.toSet()));
        parameters.addAll(classicalParameters.keySet().stream().map(ClassicalVariable::getName).collect(Collectors.toSet()));
        return parameters;
    }

    public ArrayList<QuantumVariable> getQuantumParameters() {
        return new ArrayList<>(quantumParameters.keySet());
    }

    public ArrayList<ClassicalVariable> getClassicalParameters() {
        return new ArrayList<>(classicalParameters.keySet());
    }

    private QuantumVariable getQuantumVariable(String name) {
        return quantumParameters.keySet().stream().filter(x -> x.getName().equals(name)).findFirst().orElse(null);
    }

    private ClassicalVariable getClassicalVariable(String name) {
        return classicalParameters.keySet().stream().filter(x -> x.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Returns all next InstructionNodes from both the quantum and classical parameters.
     * @return ArrayList of next InstructionNodes
     */
    public ArrayList<InstructionNode> getNextInstructions() {
        Set<InstructionNode> branches = quantumParameters
                .values()
                .stream()
                .flatMap(x -> x.next.stream())
                .collect(Collectors.toSet());
        branches.addAll(classicalParameters
                .values()
                .stream()
                .flatMap(x -> x.next.stream())
                .collect(Collectors.toSet()));
        return new ArrayList<>(branches);
    }

    /**
     * Adds the instruction's node to the instruction and extracts all parameters associated with the node.
     * @param ptNode The node to add to the instruction.
     */
    public void setParseTreeNode(ParseTreeNode ptNode) {
        this.ptNode = ptNode;
        calculateLineParameters(ptNode);
    }

    private void calculateLineParameters(ParseTreeNode node) {
        VariableCalculator vc = new VariableCalculator(node);
        Set<QuantumVariable> quantumVariables = vc.getQuantumVariables();
        Set<ClassicalVariable> classicalVariables = vc.getClassicalVariables();
        for (QuantumVariable qv : quantumVariables) {
            quantumParameters.put(qv, new connectedInstructions());
        }
        for (ClassicalVariable cv : classicalVariables) {
            classicalParameters.put(cv, new connectedInstructions());
        }
    }

    /**
     * Sets the previous instruction node and the next instruction node for the given parameters. On the previous
     * instruction node, the next instruction node is set to this instruction node.
     * Changes the previous parameters such that it can be given to the next node.
     * @param previousParameters The previous instruction nodes.
     */
    public void setParameterLinks(Map<String, InstructionNode> previousParameters) {
        ArrayList<String> parameters = getParameters();
        for (String parameter : parameters) {
            if (!previousParameters.containsKey(parameter)) {
                // If parameter is not in the previousParameters, add it to previous Parameters
                previousParameters.put(parameter, this);
            } else {
                // If parameter is in the previousParameters, set the next instruction node for the parameter
                setPreviousInstruction(parameter, previousParameters.get(parameter));
                setNextInstruction(parameter, previousParameters.get(parameter));
                previousParameters.put(parameter, this);
            }
        }
    }

    /**
     * Sets the previous instruction node for the given parameter.
     * @param parameter The parameter to set the previous instruction node for.
     * @param previous The previous instruction node.
     */
    private void setPreviousInstruction(String parameter, InstructionNode previous) {
        QuantumVariable qv = getQuantumVariable(parameter);
        ClassicalVariable cv = getClassicalVariable(parameter);
        if (qv != null) {
            quantumParameters.get(qv).previous.add(previous);
        } else if (cv != null) {
            classicalParameters.get(cv).previous.add(previous);
        } else {
            throw new IllegalArgumentException("Parameter " + parameter + " not found in instruction " + line);
        }
    }

    /**
     * Sets the next instruction node for the given parameter and previous instruction.
     * @param parameter The parameter to set the next instruction node for.
     * @param previous The previous instruction node.
     */
    private void setNextInstruction(String parameter, InstructionNode previous) {
        QuantumVariable qv = getQuantumVariable(parameter);
        ClassicalVariable cv = getClassicalVariable(parameter);
        if (qv != null) {
            QuantumVariable prevQv = previous.getQuantumVariable(parameter);
            previous.quantumParameters.get(prevQv).next.add(this);
        } else if (cv != null) {
            ClassicalVariable prevCv = previous.getClassicalVariable(parameter);
            previous.classicalParameters.get(prevCv).next.add(this);
        } else {
            throw new IllegalArgumentException("Parameter " + parameter + " not found in instruction " + line);
        }
    }

    /**
     * Calculates if the instruction is dead code. An instruction is dead code if it is not a control structure or
     * measure structure and:
     * - All its quantum parameters are dead and
     * - All its classical parameters that are not usage type are dead
     */
    public void calculateDeadCode() {
        if (type == LineType.CONTROL_STRUCTURE || type == LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL || shownToBeDead) {
            return;
        }
        boolean quantumDead = quantumParameters.keySet().stream().allMatch(QuantumVariable::isShownToBeDead);
        boolean classicalDead = classicalParameters
                .keySet()
                .stream()
                .filter(x -> x.getUsage() != ClassicalUsage.USAGE)
                .allMatch(ClassicalVariable::isShownToBeDead);
        shownToBeDead = quantumDead && classicalDead;
    }

    /**
     * Fetch all previous instructions that this instruction depends on.
     * @return List of previous instructions.
     */
    public Set<InstructionNode> getDependencies() {
        Set<InstructionNode> dependencies = new HashSet<>();
        dependencies.addAll(quantumParameters
                .values()
                .stream()
                .flatMap(x -> x.previous.stream())
                .collect(Collectors.toSet()));
        dependencies.addAll(classicalParameters
                .values()
                .stream()
                .flatMap(x -> x.previous.stream())
                .collect(Collectors.toSet()));
        Queue<InstructionNode> queue = new LinkedList<>(dependencies);
        while(!queue.isEmpty()) {
            InstructionNode node = queue.poll();
            Set<InstructionNode> newDependencies = node.getDependencies().stream()
                    .filter(x -> !dependencies.contains(x))
                    .collect(Collectors.toSet());
            dependencies.addAll(newDependencies);
            queue.addAll(newDependencies);
        }
        return dependencies;
    }
}
