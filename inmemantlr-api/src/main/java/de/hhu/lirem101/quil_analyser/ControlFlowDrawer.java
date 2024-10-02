package de.hhu.lirem101.quil_analyser;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.Node;
import org.apache.commons.lang3.mutable.Mutable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static guru.nidi.graphviz.attribute.Label.Justification.LEFT;
import static guru.nidi.graphviz.model.Factory.*;

/**
 * Draws a control flow graph from linked ControlFlowBlocks.
 */
public class ControlFlowDrawer {

    private final ControlFlowBlock block;

    public ControlFlowDrawer(ControlFlowBlock block) {
        this.block = block;
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
        String[] blockText = new String[block.getCodelines().size() + 2];
        blockText[0] = block.getName();
        blockText[1] = "---";
        int i = 2;
        for (int linenumber : block.getCodelines()) {
            String line = quilFileLines.get(linenumber - 1);
            blockText[i] = linenumber + ": " + line;
            i++;
        }
        return blockText;
    }

    public void drawControlFlowGraph(File file, String filename) throws IOException {
        Graph g = graph("ControlFlowGraph").directed();
        Path filePath = new File(filename).toPath();
        List<String> fileLines = Files.readAllLines(filePath);

        Set<ControlFlowBlock> blocks = setOfAllBlocks(block);
        Set<Node> nodes = new HashSet<>();
        blocks.forEach(block -> {
            String[] blockText = getBlockText(block, fileLines);
            Node node = node(block.getName()).with(Shape.RECTANGLE).with(Label.lines(LEFT, blockText));
            nodes.add(node);
        });

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

        try {
            Graphviz.fromGraph(g).width(200).render(Format.PS).toFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
