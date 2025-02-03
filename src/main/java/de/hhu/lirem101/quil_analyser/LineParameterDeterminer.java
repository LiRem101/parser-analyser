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

package de.hhu.lirem101.quil_analyser;

import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

public class LineParameterDeterminer {
    private boolean calculated = false;
    private final ParseTreeNode root;
    private final Set<LineParameter> lineParameters = new HashSet<>();

    public LineParameterDeterminer(ParseTree pt, ClassifyLines cl) {
        this.root = pt.getRoot();
        Map<Integer, LineType> classes = cl.classifyLines();
        for (int line : classes.keySet()) {
            LineType type = classes.get(line);
            LineParameter lp = new LineParameter(line, type);
            lineParameters.add(lp);
        }
    }

    public ArrayList<LineParameter> getLineParameters() {
        if (!calculated) {
            calculated = true;
            calculateLineParameters();
        }
        return lineParameters.stream().sorted().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void calculateLineParameters() {
        LinkedList<ParseTreeNode> queue = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()) {
            ParseTreeNode currentNode = queue.pollLast();
            handleParametersOfThisNode(currentNode);
            queue.addAll(currentNode.getChildren());
        }
    }

    private void handleParametersOfThisNode(ParseTreeNode node) {
        int line = node.getLine();
        switch (node.getRule()) {
            case "addr":
                lineParameters.stream().filter(lp -> lp.getLineNumber() == line).forEach(lp -> lp.addClassicalParameter(node.getLabel()));
                break;
            case "memoryDescriptor":
                String param = node.getLabel();
                // Remove 'DECLARE' in the front
                param = param.substring(7);
                // Remove BIT, FLOAT, INTEGER, OCTET or REAL in the middle
                param = param.replaceAll("[BIT|FLOAT|INTEGER|OCTET|REAL]", "");

                String[] paramArray = param.split("\\[");
                String param_prefix = paramArray[0];
                int param_number = 1;
                if(paramArray.length > 1) {
                    param_number = Integer.parseInt(paramArray[1].split("]")[0]);
                    for (int i = 1; i <= param_number; i++) {
                        String finalParam = param_prefix + "[" + (i - 1) + "]";
                        lineParameters.stream()
                                .filter(lp -> lp.getLineNumber() == line)
                                .forEach(lp -> lp.addClassicalParameter(finalParam));
                    }
                } else {
                    lineParameters.stream()
                            .filter(lp -> lp.getLineNumber() == line)
                            .forEach(lp -> lp.addClassicalParameter(param_prefix));
                }

                break;
            case "qubit":
            case "qubitVariable":
                lineParameters.stream().filter(lp -> lp.getLineNumber() == line).forEach(lp -> lp.addQuantumParameter(node.getLabel()));
                break;
        }
    }

}
