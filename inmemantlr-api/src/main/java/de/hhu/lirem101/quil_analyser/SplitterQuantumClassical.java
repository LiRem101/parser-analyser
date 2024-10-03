package de.hhu.lirem101.quil_analyser;

import java.util.*;

public class SplitterQuantumClassical {

    private final ControlFlowBlock originalRoot;
    private final Map<Integer, LineType> classes;
    private ControlFlowBlock newRoot;
    private final Map<String, LineType> typesOfBlocks = new HashMap<>();
    boolean calculated = false;

    public SplitterQuantumClassical(ControlFlowBlock root, Map<Integer, LineType> classes) {
        this.originalRoot = root;
        this.classes = classes;
    }

    public ControlFlowBlock getNewNode() {
        if (!calculated) {
            calculated = true;
            calculateBlocks();
        }
        return newRoot;
    }

    private void calculateBlocks() {
        String name = "start";
        newRoot = new ControlFlowBlock(name);
        newRoot.setLineType(LineType.CONTROL_STRUCTURE);
        List<Integer> codelines = originalRoot.getCodelines();
        List<Integer> quantumCodelines = new ArrayList<>();
        List<Integer> classicalCodelines = new ArrayList<>();
        for (int codeline : codelines) {
            LineType type = classes.get(codeline);
            if (type == LineType.QUANTUM) {
                quantumCodelines.add(codeline);
            } else if(type == LineType.CLASSICAL) {
                classicalCodelines.add(codeline);
            }
        }
        ControlFlowBlock quantumBlock = new ControlFlowBlock(name + "quantum");
        quantumBlock.setLineType(LineType.QUANTUM);
        quantumBlock.addCodelines(quantumCodelines);
        newRoot.addBranch(quantumBlock);
        ControlFlowBlock classicalBlock = new ControlFlowBlock(name + "classical");
        classicalBlock.setLineType(LineType.CLASSICAL);
        classicalBlock.addCodelines(classicalCodelines);
        newRoot.addBranch(classicalBlock);

        ControlFlowBlock halt = new ControlFlowBlock("halt");
        halt.setLineType(LineType.CONTROL_STRUCTURE);
        quantumBlock.addBranch(halt);
        classicalBlock.addBranch(halt);
    }


}
