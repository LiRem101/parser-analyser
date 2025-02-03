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
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.constant_folding.*;

import java.util.ArrayList;

public class ConstantFolder {

    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<ArrayList<Integer>> adaptedLines = new ArrayList<>();
    boolean calculated = false;

    public ConstantFolder(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions = instructions;
    }

    /**
     * Propagates constants and returns the adapted lines.
     * @return The adapted lines.
     */
    public ArrayList<ArrayList<Integer>> getAdaptedLines() {
        if(!calculated) {
            foldConstants();
            calculated = true;
        }
        return adaptedLines;
    }

    private void foldConstants() {
        for(ArrayList<InstructionNode> instructionList : instructions) {
            ArrayList<Integer> lines = new ArrayList<>();
            for(InstructionNode instruction : instructionList) {
                Handler handler;
                if(instruction.getLineType() == LineType.CLASSICAL) {
                    handler = new ClassicalNodeHandler(instruction);
                } else if(instruction.getLineType() == LineType.QUANTUM) {
                    handler = new QuantumHandler(instruction);
                } else if(instruction.getLineType() == LineType.CLASSICAL_INFLUENCES_QUANTUM || instruction.getLineType() == LineType.QUANTUM_INFLUENCES_CLASSICAL) {
                    handler = new HybridNodeHandler(instruction);
                } else {
                    handler = new DefaultHandler();
                }
                boolean changed = handler.propagateConstant();
                if(changed) {
                    lines.add(instruction.getLine());
                }
            }
            adaptedLines.add(lines);
        }
    }

}
