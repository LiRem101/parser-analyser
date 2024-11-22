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
