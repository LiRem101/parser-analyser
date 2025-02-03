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

package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class to analyse the dead code in a list of instructions.
 */
public class DeadCodeAnalyser {
    private final ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    private final ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
    private final ArrayList<Set<Integer>> deadLines = new ArrayList<>();
    private final Set<Integer> indizesOfDeadLines = new HashSet<>();
    private boolean calculated = false;

    /**
     * Constructor for the DeadCodeAnalyser.
     * @param instructions The instructions to analyse as list of lists.
     * @param indexToJumpTo The indizes the instruction lists jump to as list of sets.
     */
    public DeadCodeAnalyser(ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<Set<Integer>> indexToJumpTo) {
        this.instructions.addAll(instructions);
        this.indexToJumpTo.addAll(indexToJumpTo);
        for(ArrayList<InstructionNode> i : instructions) {
            deadLines.add(new HashSet<>());
        }
    }

    /**
     * Returns the indizes of the blocks that are not jumped to anymore.
     * @return The indizes of the blocks that are not jumped to anymore.
     */
    public Set<Integer> getIndizesOfDeadLines() {
        if(!calculated) {
            checkWhichInstructionsAreDead();
            checkWhichIndizesAreNoLongerJumpedTo();
        }
        calculated = true;
        return indizesOfDeadLines;
    }

    /**
     * Returns the dead lines in the instructions.
     * @return The dead lines in the instructions.
     */
    public ArrayList<Set<Integer>> getDeadLines() {
        if(!calculated) {
            checkWhichInstructionsAreDead();
            checkWhichIndizesAreNoLongerJumpedTo();
        }
        calculated = true;
        return deadLines;
    }

    /**
     * Add information about dead instructions into a JsonObjectBuilder.
     * @return The JsonArrayBuilder with the information.
     */
    public JsonObjectBuilder addDeadVariablesToJson() {
        if (!calculated) {
            checkWhichInstructionsAreDead();
            checkWhichIndizesAreNoLongerJumpedTo();
        }
        JsonObjectBuilder deadCodeAnalysis = Json.createObjectBuilder();
        JsonObjectBuilder deadLinesJson = Json.createObjectBuilder();
        for(int i = 0; i < deadLines.size(); i++) {
            deadLinesJson.add(Integer.toString(i), deadLines.get(i).toString());
        }
        deadCodeAnalysis.add("DeadLines", deadLinesJson);

        JsonObjectBuilder deadInstructionBlocks = Json.createObjectBuilder();
        for(int i = 0; i < indizesOfDeadLines.size(); i++) {
            deadInstructionBlocks.add(Integer.toString(i), indizesOfDeadLines.toString());
        }
        deadCodeAnalysis.add("DeadInstructionBlocks", deadInstructionBlocks);

        return deadCodeAnalysis;
    }

    /**
     * Determine the indizies of instruction blocks no longer jumped to. Consider that the first block is the start
     * block.
     */
    private void checkWhichIndizesAreNoLongerJumpedTo() {
        if(instructions.size() == 1) {
            return;
        }
        indizesOfDeadLines.addAll(IntStream.range(1, instructions.size()).boxed().collect(Collectors.toSet()));
        Set<Integer> aliveIndizes = indexToJumpTo.stream().flatMap(Set::stream).collect(Collectors.toSet());
        indizesOfDeadLines.removeAll(aliveIndizes);
    }

    /**
     * Find the dead lines in the instructions.
     * Instructions are dead if all their classical variables that are assigned are dead and all their quantum variables
     * are dead.
     */
    private void checkWhichInstructionsAreDead() {
        for(int i = 0; i < instructions.size(); i++) {
            Set<Integer> deadLinesInBlock = new HashSet<>();
            for(InstructionNode in : instructions.get(i)) {
                in.calculateDeadCode();
                if(in.getShownToBeDead()) {
                    deadLinesInBlock.add(in.getLine());
                }
            }
            deadLines.set(i, deadLinesInBlock);
        }
    }

}
