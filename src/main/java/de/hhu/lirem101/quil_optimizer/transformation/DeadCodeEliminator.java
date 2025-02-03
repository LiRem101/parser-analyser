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

package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeadCodeEliminator {
    ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
    ArrayList<Set<Integer>> deadLines = new ArrayList<>();
    boolean calculated = false;

    public DeadCodeEliminator(ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<Set<Integer>> deadLines, Set<Integer> indizesOfDeadLineBlocks) {
        this.deadLines.addAll(deadLines);
        this.indizesOfDeadLineBlocks.addAll(indizesOfDeadLineBlocks);
        this.instructions.addAll(instructions);
    }

    public void eliminateDeadCode() {
        if(calculated || deadLines.isEmpty()) {
            return;
        }
        removeDeadBlocks();
        removeDeadLines();
        calculated = true;
    }

    /**
     * Add information about dead instructions into a returned JsonObjectBuilder.
     * @return The JsonObjectBuilder with the information.
     */
    public JsonObjectBuilder addDeadVariablesToJson() {
        if(deadLines.isEmpty()) {
            return Json.createObjectBuilder();
        }
        if (!calculated) {
            removeDeadBlocks();
            removeDeadLines();
        }
        JsonObjectBuilder deadCodeAnalysis = Json.createObjectBuilder();
        JsonObjectBuilder deadLinesJson = Json.createObjectBuilder();
        for(int i = 0; i < deadLines.size(); i++) {
            JsonArrayBuilder theseDeadLines = Json.createArrayBuilder();
            for(int line : deadLines.get(i)) {
                theseDeadLines.add(line);
            }
            deadLinesJson.add(Integer.toString(i), theseDeadLines);
        }
        deadCodeAnalysis.add("RemovedDeadLines", deadLinesJson);

        JsonArrayBuilder deadInstructionBlocks = Json.createArrayBuilder();
        for(int i : indizesOfDeadLineBlocks) {
            deadInstructionBlocks.add(Integer.toString(i));
        }
        deadCodeAnalysis.add("DeadInstructionBlocks", deadInstructionBlocks);

        return deadCodeAnalysis;
    }

    /**
     * Empty dead blocks.
     */
    private void removeDeadBlocks() {
        for (int i : indizesOfDeadLineBlocks) {
            instructions.get(i).clear();
        }
    }

    /**
     * Remove dead lines from instructions.
     */
    private void removeDeadLines() {
        for(int i = 0; i < instructions.size(); i++) {
            ArrayList<InstructionNode> instructionList = instructions.get(i);
            for(int line : deadLines.get(i)) {
                InstructionNode node= instructionList.stream()
                        .filter(instructionNode -> instructionNode.getLine() == line)
                        .findFirst()
                        .orElse(null);
                if(node != null) {
                    node.removeConnections();
                    instructionList.remove(node);
                }
            }
        }
    }

}
