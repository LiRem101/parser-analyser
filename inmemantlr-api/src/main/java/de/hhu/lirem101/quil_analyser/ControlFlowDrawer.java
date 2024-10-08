package de.hhu.lirem101.quil_analyser;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static guru.nidi.graphviz.attribute.Label.Justification.LEFT;
import static guru.nidi.graphviz.model.Factory.*;

/**
 * Draws a control flow graph from linked ControlFlowBlocks.
 */
public class ControlFlowDrawer {

    private final ControlFlowBlock block;
    private final Map<Integer, LineType> classes;
    private static final String QUANTUM_COLOR = "#fe4eda";
    private static final String CLASSICAL_COLOR = "#96c8a2";
    private static final String QUANTUM_INFLUENCES_CLASSICAL_COLOR = "#008000";
    private static final String CLASSICAL_INFLUENCES_QUANTUM_COLOR = "#ff4500";

    public ControlFlowDrawer(ControlFlowBlock block, Map<Integer, LineType> classes) {
        this.block = block;
        this.classes = classes;
    }

    private Set<ControlFlowBlock> setOfAllBlocks(ControlFlowBlock block) {
        Set<ControlFlowBlock> blocks = new HashSet<>();
        LinkedList<ControlFlowBlock> blockQueue = new LinkedList<>();
        blockQueue.add(block);

        while (!blockQueue.isEmpty()) {
            ControlFlowBlock currentBlock = blockQueue.poll();
            blocks.add(currentBlock);
            for (ControlFlowBlock b : currentBlock.getBranches()) {
                if (!blocks.contains(b)) {
                    blockQueue.add(b);
                }
            }
        }
        return blocks;
    }

    private String[] getBlockText(ControlFlowBlock block, List<String> quilFileLines) {
        String[] blockText = new String[block.getCodelines().size()];
        String name = block.getName();
        if (name.equals("start")) {
            return new String[]{"START"};
        } else if (name.equals("halt")) {
            return new String[]{"HALT"};
        } else if(block.getCodelines().isEmpty()) {
            return new String[]{name};
        }
        int i = 0;
        for (int linenumber : block.getCodelines()) {
            String line = quilFileLines.get(linenumber - 1);
            String color = "#000000";
            if (classes.get(linenumber) == LineType.QUANTUM) {
                color = QUANTUM_COLOR;
            } else if (classes.get(linenumber) == LineType.CLASSICAL) {
                color = CLASSICAL_COLOR;
            } else if (classes.get(linenumber) == LineType.QUANTUM_INFLUENCES_CLASSICAL) {
                color = QUANTUM_INFLUENCES_CLASSICAL_COLOR;
            } else if (classes.get(linenumber) == LineType.CLASSICAL_INFLUENCES_QUANTUM) {
                color = CLASSICAL_INFLUENCES_QUANTUM_COLOR;
            }
            blockText[i] = "<font color=\"" + color + "\">" + linenumber + ": " + line + "</font>";
            i++;
        }
        return blockText;
    }

    public void drawControlFlowGraph(File psFile, File dotFile, String filename) throws IOException {
        Graph g = graph("ControlFlowGraph").directed();
        Path filePath = new File(filename).toPath();
        List<String> fileLines = Files.readAllLines(filePath);

        Set<ControlFlowBlock> blocks = setOfAllBlocks(block);
        Set<Node> nodes = new HashSet<>();
        for(ControlFlowBlock block : blocks) {
            String[] blockText = getBlockText(block, fileLines);
            Color color = Color.BLACK;
            if(block.getLineType() == LineType.QUANTUM) {
                color = Color.rgb(QUANTUM_COLOR);
            } else if(block.getLineType() == LineType.CLASSICAL) {
                color = Color.rgb(CLASSICAL_COLOR);
            }
            Node node = node(block.getName()).with(Shape.RECTANGLE, color, Label.htmlLines(LEFT, blockText));
            nodes.add(node);
        }

        for (ControlFlowBlock block : blocks) {
            List<ControlFlowBlock> branches = block.getBranches();
            Node node = nodes.stream().filter(n -> n.name().toString().equals(block.getName())).findFirst().orElse(null);
            for (ControlFlowBlock branch : branches) {
                Node branchNode = nodes.stream().filter(n -> n.name().toString().equals(branch.getName())).findFirst().orElse(null);
                if (node != null && branchNode != null) {
                    g = g.with(node.link(to(branchNode)));
                }
            }
        }

        Graphviz.fromGraph(g).render(Format.DOT).toFile(dotFile);
        Graphviz.fromGraph(g).width(200).render(Format.PS).toFile(psFile);

    }

}
