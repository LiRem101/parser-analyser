package de.hhu.lirem101.quil_analyser;

import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

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

    public void drawControlFlowGraph(File file) {
        MutableGraph g = mutGraph("ControlFlowGraph").setDirected(true);

        Set<ControlFlowBlock> blocks = setOfAllBlocks(block);
        Set<MutableNode> nodes = new HashSet<>();
        blocks.forEach(block -> {
            nodes.add(mutNode(block.getName()));
        });

        for(MutableNode node : nodes) {
            node.add(Shape.RECTANGLE);
        }

        for (ControlFlowBlock block : blocks) {
            List<ControlFlowBlock> branches = block.getBranches();
            MutableNode node = nodes.stream().filter(n -> n.name().toString().equals(block.getName())).findFirst().orElse(null);
            for (ControlFlowBlock branch : branches) {
                MutableNode branchNode = nodes.stream().filter(n -> n.name().toString().equals(branch.getName())).findFirst().orElse(null);
                if (node != null || branchNode != null) {
                    node.addLink(branchNode);
                }
            }
        }

        for (MutableNode node : nodes) {
            g.add(node);
        }

        try {
            Graphviz.fromGraph(g).width(200).render(Format.PNG).toFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
