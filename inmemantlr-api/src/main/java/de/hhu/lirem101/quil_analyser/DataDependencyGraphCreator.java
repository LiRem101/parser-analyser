package de.hhu.lirem101.quil_analyser;

import java.lang.reflect.Array;
import java.util.*;

public class DataDependencyGraphCreator {

    private final ArrayList<ControlFlowBlock> blocks = new ArrayList<>();
    private final ArrayList<LineParameter> lines;
    private boolean calculated = false;
    private final ArrayList<LineParameter> dataDependencyGraph = new ArrayList<>();

    public DataDependencyGraphCreator(ArrayList<ControlFlowBlock> rankedControlFlowBlocks, ArrayList<LineParameter> lines) {
        this.blocks.addAll(rankedControlFlowBlocks);
        this.lines = lines;
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

        for (ControlFlowBlock block : blocks) {
            for (int line : block.getCodelines()) {
                LineParameter currentLine = lines.stream().filter(lp -> lp.getLineNumber() == line).findFirst().orElse(null);
                if (currentLine == null) {
                    continue;
                }
                for (String param : currentLine.getParameters()) {
                    addParameterDependency(block, param, params, currentLine, blocks);
                }
                dataDependencyGraph.add(currentLine);
            }
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
    private void addParameterDependency(ControlFlowBlock block, String param, Map<String, ArrayList<LineParameter>> params, LineParameter currentLine, ArrayList<ControlFlowBlock> blocks) {
        if (!params.containsKey(param)) {
            ArrayList<LineParameter> list = new ArrayList<>();
            list.add(currentLine);
            params.put(param, list);
        } else {
            Set<LineParameter> toAdd = new TreeSet<>();
            ArrayList<Integer> allPreviousLines = allPreviousLines(block, currentLine.getLineNumber());
            LinkedList<Integer> queue = new LinkedList<>(allPreviousLines);
            while(!queue.isEmpty()) {
                int line = queue.poll();
                LineParameter lp = lines.stream().filter(l -> l.getLineNumber() == line).findFirst().orElse(null);
                if (lp == null) {
                    continue;
                }
                if (lp.containsQuantumParameter(param) || lp.containsClassicalParameter(param)) {
                    toAdd.add(lp);
                    ControlFlowBlock blockOfLine = blocks.stream().filter(b -> b.getCodelines().contains(lp.getLineNumber())).findFirst().orElse(null);
                    ArrayList<Integer> previousLinesOfThisLine = allPreviousLines(blockOfLine, lp.getLineNumber());
                    queue.removeAll(previousLinesOfThisLine);
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
