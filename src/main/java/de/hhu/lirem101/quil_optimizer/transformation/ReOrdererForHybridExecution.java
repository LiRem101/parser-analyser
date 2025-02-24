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

package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import java.util.*;
import java.util.stream.Collectors;

import static de.hhu.lirem101.quil_optimizer.transformation.NodeSorter.sortNodesWithGivenExecutables;

public class ReOrdererForHybridExecution {

    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencies;
    boolean calculated = false;

    public ReOrdererForHybridExecution(ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencies) {
        this.instructions = instructions;
        this.hybridDependencies = hybridDependencies;
    }

    /**
     * Re-order all instructions in the list of instructions. The order is determined by the hybrid dependencies.
     * @return The line numbers of the re-ordered list of instructions.
     */
    public ArrayList<ArrayList<InstructionNode>> reOrderInstructions() {
        if(!calculated) {
            reOrderAllInstructions();
            calculated = true;
        }
        return instructions;
    }

    private void reOrderAllInstructions() {
        for(int i = 0; i < instructions.size(); i++) {
            if(instructions.get(i).isEmpty()) {
                continue;
            }
            ArrayList<InstructionNode> newOrder = reOrderInstructions(instructions.get(i), hybridDependencies.get(i));
            instructions.add(i, newOrder);
            instructions.remove(i + 1);
        }
    }

    /**
     * Re-Order the instructions in the list. The hybrid dependencies are used to determine the order. They give the
     * information which hybrid instructions depend on which other instructions. The instructions on which the next
     * hybrid instruction depends should be executed earliest. If possible, equally many quantum and classical
     * instructions should be executed, as they are executed in parallel.
     * @param instructionList The list of instructions to be re-ordered.
     * @param hybDep The hybrid dependencies.
     * @return The list of the new order for instructionList.
     */
    private ArrayList<InstructionNode> reOrderInstructions(ArrayList<InstructionNode> instructionList, LinkedHashMap<Integer, Set<Integer>> hybDep) {
        List<Integer> hybridInstructionIndices = new ArrayList<>(hybDep.keySet());
        ArrayList<InstructionNode> newOrder = new ArrayList<>();

        long previousQuantumNodes = 0;
        long previousClassicalNodes = 0;

        for(int i = 0; i < hybridInstructionIndices.size(); i++) {
            int finalI = i;
            ArrayList<InstructionNode> dependenciesOfCurrentHybrid = instructionList.stream()
                    .filter(x -> hybDep.get(hybridInstructionIndices.get(finalI)).contains(x.getLine()))
                    .collect(Collectors.toCollection(ArrayList::new));
            ArrayList<InstructionNode> toAdd = dependenciesOfCurrentHybrid.stream()
                    .filter(x -> !newOrder.contains(x))
                    .collect(Collectors.toCollection(ArrayList::new));
            long newNumberOfQuantumNodes = toAdd.stream()
                    .filter(x -> x.getLineType() == LineType.QUANTUM)
                    .count();
            long newNumberOfClassicalNodes = toAdd.stream()
                    .filter(x -> x.getLineType() == LineType.CLASSICAL)
                    .count();
            sortNodesWithGivenExecutables(toAdd, newOrder);
            boolean quantumAndClassicalEqual = newNumberOfQuantumNodes == newNumberOfClassicalNodes;
            int j = i + 1;
            while(!quantumAndClassicalEqual && j <= hybridInstructionIndices.size()) {
                // If there are not equally many quantum and classical instructions, try to add quantum/classical instructions

                ArrayList<InstructionNode> dependenciesToAdd = new ArrayList<>();
                if (j < hybridInstructionIndices.size()) {
                    int finalJ = j;
                    dependenciesToAdd = instructionList.stream()
                            .filter(x -> hybDep.get(hybridInstructionIndices.get(finalJ)).contains(x.getLine()))
                            .collect(Collectors.toCollection(ArrayList::new));
                } else {
                    int biggestHybridLine = 0;
                    if (!hybDep.isEmpty()) {
                        biggestHybridLine = hybridInstructionIndices.get(hybridInstructionIndices.size() - 1);
                    }
                    int finalBiggestHybridLine = biggestHybridLine;
                    dependenciesToAdd = instructionList.stream()
                            .filter(x -> x.getLine() > finalBiggestHybridLine ||
                                    x.getLine() < finalBiggestHybridLine && !newOrder.contains(x))
                            .filter(x -> x.getLineType() == LineType.QUANTUM || x.getLineType() == LineType.CLASSICAL)
                            .collect(Collectors.toCollection(ArrayList::new));
                }

                long originalNumberOfQuantumNodes = newOrder.stream()
                        .filter(x -> x.getLineType() == LineType.QUANTUM)
                        .count();
                long originalNumberOfClassicalNodes = newOrder.stream()
                        .filter(x -> x.getLineType() == LineType.CLASSICAL)
                        .count();

                addNewDependencies(newOrder, dependenciesToAdd, newNumberOfQuantumNodes, newNumberOfClassicalNodes);

                long nextNumberOfQuantumNodes = newOrder.stream()
                        .filter(x -> x.getLineType() == LineType.QUANTUM)
                        .count()
                        - originalNumberOfQuantumNodes;
                long nextNumberOfClassicalNodes = newOrder.stream()
                        .filter(x -> x.getLineType() == LineType.CLASSICAL)
                        .count()
                        - originalNumberOfClassicalNodes;
                if(newNumberOfQuantumNodes > newNumberOfClassicalNodes) {
                    newNumberOfClassicalNodes += nextNumberOfClassicalNodes;
                    if(newNumberOfClassicalNodes >= newNumberOfQuantumNodes) {
                        quantumAndClassicalEqual = true;
                        newNumberOfClassicalNodes = newNumberOfQuantumNodes;
                    }
                } else {
                    newNumberOfQuantumNodes += nextNumberOfQuantumNodes;
                    if(newNumberOfQuantumNodes >= newNumberOfClassicalNodes) {
                        quantumAndClassicalEqual = true;
                        newNumberOfQuantumNodes = newNumberOfClassicalNodes;
                    }
                }

                j++;
            }
            previousClassicalNodes += newNumberOfClassicalNodes;
            previousQuantumNodes += newNumberOfQuantumNodes;
            // Add the hybrid node to the new order and work on the next hybrid node
            int finalI1 = i;
            InstructionNode hybridNode = instructionList.stream()
                    .filter(x -> x.getLine() == hybridInstructionIndices.get(finalI1))
                    .findFirst()
                    .orElse(null);
            if (hybridNode != null) {
                newOrder.add(hybridNode);
            } else {
                throw new RuntimeException("Hybrid instruction not found in instruction list.");
            }
        }
        // Add the instructions that no hybrid instruction depends on at the end
        ArrayList<InstructionNode> remainingInstructions = instructionList.stream()
                .filter(x -> !newOrder.contains(x))
                .collect(Collectors.toCollection(ArrayList::new));
        newOrder.addAll(remainingInstructions);

        return newOrder;
    }

    /**
     * Add new executable dependencies to the list of dependencies from a set of given dependencies. Do so until the
     * number of quantum and classical instructions is equal or there are no new instructions that can be added.
     * @param instructions The list of dependencies to add new dependencies to.
     * @param possibleNewInstructions The list of instructions that can be added.
     * @param numberOfQuantumNodes The number of quantum instructions that have been added since the last hybrid
     *                             instrcution.
     * @param numberOfClassicalNodes The number of classical instructions that have been added since the last hybrid
     *                               instruction.
     */
    private void addNewDependencies(ArrayList<InstructionNode> instructions, ArrayList<InstructionNode> possibleNewInstructions, long numberOfQuantumNodes, long numberOfClassicalNodes) {
        boolean quantumAndClassicalEqual = numberOfQuantumNodes == numberOfClassicalNodes;
        boolean foundNewDependencies = true;

        while(!quantumAndClassicalEqual && foundNewDependencies) {
            LineType typeToAdd = numberOfQuantumNodes < numberOfClassicalNodes ? LineType.QUANTUM : LineType.CLASSICAL;
            long typeDifference = Math.abs(numberOfQuantumNodes - numberOfClassicalNodes);
            ArrayList<InstructionNode> instructionsOfRightType = possibleNewInstructions.stream()
                    .filter(x -> x.getLineType() == typeToAdd)
                    .collect(Collectors.toCollection(ArrayList::new));
            ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructionsOfRightType)));
            ArrayList<InstructionNode> executableInstructions = eie.getExecutableInstructions(new ArrayList<>(Collections.singletonList(instructions))).get(0);
            List<InstructionNode> instructionsToAdd = executableInstructions.stream()
                    .filter(x -> !instructions.contains(x))
                    .collect(Collectors.toList());
            foundNewDependencies = !instructionsToAdd.isEmpty();
            int numberToAdd = instructionsToAdd.size();

            if(typeDifference > numberToAdd) {
                instructions.addAll(instructionsToAdd);
                if (typeToAdd == LineType.QUANTUM) {
                    numberOfQuantumNodes += numberToAdd;
                } else {
                    numberOfClassicalNodes += numberToAdd;
                }
            } else {
                instructions.addAll(instructionsToAdd.stream()
                        .limit(typeDifference)
                        .collect(Collectors.toSet()));
                quantumAndClassicalEqual = true;
            }
        }
    }

}
