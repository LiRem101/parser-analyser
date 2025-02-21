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

package de.hhu.lirem101.quil_analyser;

import java.util.*;

public class DataDependencyGraphCreator {

    private final ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
    private final ArrayList<Integer> startBlockIndizes = new ArrayList<>();
    private final ArrayList<LineParameter> originalLines = new ArrayList<>();
    private final ArrayList<LineParameter> newLines = new ArrayList<>();
    private boolean calculated = false;
    private final ArrayList<LineParameter> dataDependencyGraph = new ArrayList<>();

    public DataDependencyGraphCreator(ArrayList<ControlFlowBlock> rankedControlFlowBlocks, ArrayList<Integer> startBlockIndizes, ArrayList<LineParameter> lines) {
        this.blocks.addAll(rankedControlFlowBlocks);
        this.originalLines.addAll(lines);
        this.startBlockIndizes.addAll(startBlockIndizes);
    }

    /**
     * Returns all previous lines (lines in blocks previous to the current and the current block) of a block.
     * @param block The current block.
     * @param currentLine The current line.
     * @return A list of all previous lines.
     */
    private ArrayList<Integer> allPreviousLines(ControlFlowBlock block, int currentLine) {
        ArrayList<Integer> previousLines = block.getAllDominatingLines();
        ArrayList<Integer> currentLineInBlock = block.getCodelines()
                .stream()
                .filter(line -> line < currentLine)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        Collections.reverse(currentLineInBlock);
        currentLineInBlock.addAll(previousLines);
        return currentLineInBlock;
    }

    /**
     * Calculates the data dependency graph.
     */
    private void calculateDataDependencyGraph() {
        Map<String, ArrayList<LineParameter>> params = new HashMap<>();
        ArrayList<LineParameter> currentLineParams = new ArrayList<>();
        int index = 0;

        for (ControlFlowBlock block : blocks) {
            for (int line : block.getCodelines()) {
                LineParameter originalLine = originalLines.stream().filter(lp -> lp.getLineNumber() == line).findFirst().orElse(null);
                if (originalLine == null) {
                    continue;
                }
                LineParameter currentLine = originalLine.copyLineParameter();
                currentLineParams.add(currentLine);
                for (String param : currentLine.getParameters()) {
                    addParameterDependency(block, param, params, currentLine, currentLineParams, blocks);
                }
                dataDependencyGraph.add(currentLine);
            }

            if(index == blocks.size() - 1 || startBlockIndizes.contains(index + 1)) {
                params = new HashMap<>();
                newLines.addAll(currentLineParams);
                currentLineParams = new ArrayList<>();
            }

            index++;

        }
    }

    /**
     * Adds a parameter dependency to the data dependency graph by adding a link to the currentLine if the parameter was
     * already used in a previous line.
     * @param block The current block.
     * @param param The parameter to add.
     * @param params A list of all parameters that were already used.
     * @param blocks A list of all blocks.
     * @param currentLine The LineParameter that potentially gets a new link.
     */
    private void addParameterDependency(ControlFlowBlock block, String param, Map<String, ArrayList<LineParameter>> params, LineParameter currentLine, ArrayList<LineParameter> currentLineParams, ArrayList<ControlFlowBlock> blocks) {
        if (!params.containsKey(param)) {
            ArrayList<LineParameter> list = new ArrayList<>();
            list.add(currentLine);
            params.put(param, list);
        } else {
            Set<LineParameter> toAdd = new TreeSet<>();
            ArrayList<Integer> allPreviousLines = allPreviousLines(block, currentLine.getLineNumber());
            LinkedList<Integer> queue = new LinkedList<>(allPreviousLines);
            boolean foundDependency = false;
            while(!queue.isEmpty() && !foundDependency) {
                int line = queue.poll();
                LineParameter lp = currentLineParams.stream().filter(l -> l.getLineNumber() == line).findFirst().orElse(null);
                if (lp == null) {
                    continue;
                }
                if (lp.containsQuantumParameter(param) || lp.containsClassicalParameter(param)) {
                    toAdd.add(lp);
                    foundDependency = true;
                }
            }
            for (LineParameter lp : toAdd) {
                currentLine.addExecuteBeforeLine(param, lp);
            }
        }
    }

    /**
     * Returns the data dependency graph as linked LineParameters.
     * @return The data dependency graph.
     */
    public ArrayList<LineParameter> getDataDependencyGraph() {
        if (!calculated) {
            calculated = true;
            calculateDataDependencyGraph();
        }
        return dataDependencyGraph;
    }
}
