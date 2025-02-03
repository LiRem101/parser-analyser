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

    private static class ConnectedInstructions {
        private final ArrayList<InstructionNode> previous = new ArrayList<>();
        private final ArrayList<InstructionNode> next = new ArrayList<>();

        public ConnectedInstructions copyConnections() {
            ConnectedInstructions copy = new ConnectedInstructions();
            copy.previous.addAll(previous);
            copy.next.addAll(next);
            return copy;
        }
    }

    private final int line;
    private LineType type;
    private String lineText;
    private ParseTreeNode ptNode;
    private ParseTreeNode originalPtNode;
    private boolean shownToBeDead = false;
    private final Map<QuantumVariable, ConnectedInstructions> quantumParameters = new HashMap<>();
    private final Map<ClassicalVariable, ConnectedInstructions> classicalParameters = new HashMap<>();

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
        return line + ": " + this.lineText;
    }

    @Override
    public LineType getLineType() {
        return type;
    }

    public void setLineText(String text) {
        this.lineText = text;
    }

    public void setLineType(LineType type) {
        this.type = type;
    }

    public String getLineText() {
        return lineText;
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
        if (this.originalPtNode == null) {
            originalPtNode = ptNode;
        }
        this.ptNode = ptNode;
        calculateLineParameters(ptNode);
    }

    private void calculateLineParameters(ParseTreeNode node) {
        if (this.originalPtNode == null) {
            originalPtNode = node;
        }
        VariableCalculator vc = new VariableCalculator(node);
        Set<QuantumVariable> quantumVariables = vc.getQuantumVariables();
        Set<ClassicalVariable> classicalVariables = vc.getClassicalVariables();
        for (QuantumVariable qv : quantumVariables) {
            quantumParameters.put(qv, new ConnectedInstructions());
        }
        for (ClassicalVariable cv : classicalVariables) {
            classicalParameters.put(cv, new ConnectedInstructions());
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

        ArrayList<InstructionNode> dependencyList = new ArrayList<>(Collections.singletonList(this));
        int index = 0;
        while(index < dependencyList.size()) {
            InstructionNode currentNode = dependencyList.get(index);
            Set<InstructionNode> newDependencies = new HashSet<>();
            Set<QuantumVariable> quantumVariables = currentNode.quantumParameters.keySet();
            Set<ClassicalVariable> classicalVariables = currentNode.classicalParameters.keySet();

            for (QuantumVariable qv : quantumVariables) {
                newDependencies.addAll(currentNode.quantumParameters.get(qv).previous);
            }
            for (ClassicalVariable cv : classicalVariables) {
                newDependencies.addAll(currentNode.classicalParameters.get(cv).previous);
            }

            newDependencies.removeAll(dependencies);
            dependencyList.addAll(newDependencies);
            index++;
        }
        dependencyList.remove(this);
        dependencies.addAll(dependencyList);
        return dependencies;
    }

    /**
     * Prepare the removal of th einstruction by removing the connections to other instructions.
     * The previous instruction becomes the previous instruction of thenext instruction and vice versa.
     */
    public void removeConnections(){
        List<QuantumVariable> qvs = new ArrayList<>(quantumParameters.keySet());
        for(QuantumVariable qv : qvs){
            removeQuantumConnection(qv);
        }
        List<ClassicalVariable> cvs = new ArrayList<>(classicalParameters.keySet());
        for(ClassicalVariable cv : cvs){
            removeClassicalConnection(cv);
        }
    }

    public void removeQuantumConnection(QuantumVariable qv) {
        ConnectedInstructions ci = quantumParameters.get(qv);
        ArrayList<InstructionNode> prevNodes = ci.previous;
        ArrayList<InstructionNode> nextNodes = ci.next;
        for(InstructionNode prevNode : prevNodes){
            QuantumVariable var = prevNode.getQuantumVariable(qv.getName());
            prevNode.quantumParameters.get(var).next.remove(this);
            prevNode.quantumParameters.get(var).next.addAll(nextNodes);
        }
        for(InstructionNode nextNode : nextNodes){
            QuantumVariable var = nextNode.getQuantumVariable(qv.getName());
            nextNode.quantumParameters.get(var).previous.remove(this);
            nextNode.quantumParameters.get(var).previous.addAll(prevNodes);
        }
        quantumParameters.remove(qv);
    }

    public void removeClassicalConnection(ClassicalVariable cv) {
        ConnectedInstructions ci = classicalParameters.get(cv);
        ArrayList<InstructionNode> prevNodes = ci.previous;
        ArrayList<InstructionNode> nextNodes = ci.next;
        for(InstructionNode prevNode : prevNodes){
            ClassicalVariable var = prevNode.getClassicalVariable(cv.getName());
            prevNode.classicalParameters.get(var).next.remove(this);
            prevNode.classicalParameters.get(var).next.addAll(nextNodes);
        }
        for(InstructionNode nextNode : nextNodes){
            ClassicalVariable var = nextNode.getClassicalVariable(cv.getName());
            nextNode.classicalParameters.get(var).previous.remove(this);
            nextNode.classicalParameters.get(var).previous.addAll(prevNodes);
        }
        classicalParameters.remove(cv);
    }

    /**
     * Copy the instruction node.
     * @return The copied instruction node.
     */
    public InstructionNode copyInstruction() {
        InstructionNode copy = new InstructionNode(line, type);
        copy.lineText = lineText;
        copy.ptNode = originalPtNode;
        copy.shownToBeDead = shownToBeDead;
        for (QuantumVariable qv : quantumParameters.keySet()) {
            copy.quantumParameters.put(qv.copyQV(), quantumParameters.get(qv).copyConnections());
        }
        for (ClassicalVariable cv : classicalParameters.keySet()) {
            copy.classicalParameters.put(cv.copyCV(), classicalParameters.get(cv).copyConnections());
        }
        return copy;
    }

}
