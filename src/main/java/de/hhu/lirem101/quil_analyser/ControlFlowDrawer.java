package de.hhu.lirem101.quil_analyser;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

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

    private final Set<DirectedGraphNode> blocks;
    private final Map<Integer, LineType> classes;
    private static final String QUANTUM_COLOR = "#fe4eda";
    private static final String CLASSICAL_COLOR = "#464df7";
    private static final String QUANTUM_INFLUENCES_CLASSICAL_COLOR = "#008000";
    private static final String CLASSICAL_INFLUENCES_QUANTUM_COLOR = "#008000";

    public ControlFlowDrawer(DirectedGraphNode block, Map<Integer, LineType> classes) {
        this.blocks = setOfAllBlocks(block);
        this.classes = classes;
    }

    public ControlFlowDrawer(Set<DirectedGraphNode> blocks, Map<Integer, LineType> classes) {
        this.blocks = blocks;
        this.classes = classes;
    }

    private Set<DirectedGraphNode> setOfAllBlocks(DirectedGraphNode block) {
        Set<DirectedGraphNode> blocks = new HashSet<>();
        LinkedList<DirectedGraphNode> blockQueue = new LinkedList<>();
        blockQueue.add(block);

        while (!blockQueue.isEmpty()) {
            DirectedGraphNode currentBlock = blockQueue.poll();
            blocks.add(currentBlock);
            ArrayList<DirectedGraphNode> branches = currentBlock.getBranches();
            for (DirectedGraphNode b : branches) {
                if (!blocks.contains(b)) {
                    blockQueue.add(b);
                }
            }
        }
        return blocks;
    }

    private String[] getBlockText(DirectedGraphNode block, List<String> quilFileLines) {
        String[] blockText = new String[block.getCodelines().size() + 1];
        String name = block.getName();
        if (name.equals("start")) {
            return new String[]{"START"};
        } else if (name.equals("halt")) {
            return new String[]{"HALT"};
        } else if(block.getCodelines().isEmpty()) {
            return new String[]{name};
        }
        int i = 0;
        ArrayList<Integer> codelines = block.getCodelines();
        for (int linenumber : codelines) {
            String line = quilFileLines.get(linenumber - 1);
            String color = "#000000";
            String type = "Hybrid:";
            if (classes.get(linenumber) == LineType.QUANTUM) {
                color = QUANTUM_COLOR;
                type = "QPU:";
            } else if (classes.get(linenumber) == LineType.CLASSICAL) {
                color = CLASSICAL_COLOR;
                type = "CPU:";
            } else if (classes.get(linenumber) == LineType.QUANTUM_INFLUENCES_CLASSICAL) {
                color = QUANTUM_INFLUENCES_CLASSICAL_COLOR;
            } else if (classes.get(linenumber) == LineType.CLASSICAL_INFLUENCES_QUANTUM) {
                color = CLASSICAL_INFLUENCES_QUANTUM_COLOR;
            }
            if(i == 0) {
                blockText[i] = type;
                i++;
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

        // Set<DirectedGraphNode> blocks = setOfAllBlocks(this.blocks);
        Map<DirectedGraphNode, Node> nodes = new HashMap<>();
        int i = 0;
        for(DirectedGraphNode block : blocks) {
            String[] blockText = getBlockText(block, fileLines);
            Color color = Color.BLACK;
            if(block.getLineType() == LineType.QUANTUM) {
                color = Color.rgb(QUANTUM_COLOR);
            } else if(block.getLineType() == LineType.CLASSICAL) {
                color = Color.rgb(CLASSICAL_COLOR);
            } else if (block.getLineType() == LineType.QUANTUM_INFLUENCES_CLASSICAL || block.getLineType() == LineType.CLASSICAL_INFLUENCES_QUANTUM) {
                color = Color.rgb(CLASSICAL_INFLUENCES_QUANTUM_COLOR);
            }
            Node node = node(block.getName() + i).with(Shape.RECTANGLE, color, Label.htmlLines(LEFT, blockText));
            nodes.put(block, node);
            i++;
        }

        for (DirectedGraphNode block : blocks) {
            List<DirectedGraphNode> branches = block.getBranches();
            Node node = nodes.get(block);
            for (DirectedGraphNode branch : branches) {
                Node branchNode = nodes.get(branch);
                if (node != null && branchNode != null) {
                    g = g.with(node.link(to(branchNode)));
                }
            }
        }

        Graphviz.fromGraph(g).render(Format.DOT).toFile(dotFile);
        Graphviz.fromGraph(g).width(200).render(Format.PS).toFile(psFile);

    }

}
