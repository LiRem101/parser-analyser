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
                String finalParam = param;
                lineParameters.stream().filter(lp -> lp.getLineNumber() == line).forEach(lp -> lp.addClassicalParameter(finalParam));
                break;
            case "qubit":
            case "qubitVariable":
                lineParameters.stream().filter(lp -> lp.getLineNumber() == line).forEach(lp -> lp.addQuantumParameter(node.getLabel()));
                break;
        }
    }

}
