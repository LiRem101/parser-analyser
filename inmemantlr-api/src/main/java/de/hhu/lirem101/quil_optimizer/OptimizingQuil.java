package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.analysis.BoxedVariableProperties;
import de.hhu.lirem101.quil_optimizer.analysis.LiveVariableAnalyser;
import org.snt.inmemantlr.tree.ParseTreeNode;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class OptimizingQuil {
    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<ArrayList<InstructionNode>> currentOrder = new ArrayList<>();
    private final ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
    private final Set<String> readoutParams = new HashSet<>();


    /**
     * Constructor for the OptimizingQuil class. Creates a list of list of instructions from a control flow block.
     * @param block The control flow block to create the instructions from.
     * @param classes A map with the line number as key and the line type as value.
     * @param root The root node of the parse tree.
     * @param readoutParams The classical params whose values are read out at the end of the program, i.e. that will not
     *                      be dead.
     */
    public OptimizingQuil(ControlFlowBlock block, Map<Integer, LineType> classes, ParseTreeNode root, Set<String> readoutParams) {
        InstructionListCreator ilc = new InstructionListCreator(block, classes);
        SortingNodesToLines snl = new SortingNodesToLines(root);
        Map<Integer, ParseTreeNode> sortedNodes = snl.getSortedNodes();
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        this.instructions = ilc.getInstructions();
        sorter.appendNodeToInstructions(this.instructions);
        createLinksOfInstructions();
        createListsForOrderedInstructions();
        ArrayList<ArrayList<Integer>> linesToJumpTo = ilc.getLinesToJumpTo();
        replaceLinesByIndex(indexToJumpTo, linesToJumpTo);
        this.readoutParams.addAll(readoutParams);
    }


    /**
     * Apply optimization steps to the instructions. Save the optimized instructions in currentOrder. Create a json
     * string with the optimized instructions.
     * @param optimizationSteps The optimization steps to apply as strings.
     * @return The json string with the optimized instructions.
     */
    public String applyOptimizationSteps(ArrayList<String> optimizationSteps){
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        JsonObjectBuilder startBuilder = Json.createObjectBuilder();
        startBuilder.add("codeLines", instructionsToLines(instructions).toString());
        jsonBuilder.add("Start", startBuilder);

        for(String optimizationStep : optimizationSteps) {
            switch (optimizationStep) {
                case "LiveVariableAnalysis":
                    LiveVariableAnalyser lva = new LiveVariableAnalyser(currentOrder, readoutParams);
                    lva.addDeadVariablesToJson(jsonBuilder);
                    break;
            }
        }
        JsonObject json = jsonBuilder.build();
        String jsonString = json.toString();

        return jsonString;
    }


    /**
     * Let instructions create their linking.
     */
    private void createLinksOfInstructions() {
        for (ArrayList<InstructionNode> instruction : instructions) {
            Map<String, InstructionNode> lastInstructionOfParams = new HashMap<>();
            for (InstructionNode node : instruction) {
                node.setParameterLinks(lastInstructionOfParams);
            }
        }
    }

    private void createListsForOrderedInstructions() {
        for (ArrayList<InstructionNode> instructionList : instructions) {
            currentOrder.add(new ArrayList<>());
            currentOrder.get(currentOrder.size() - 1).addAll(instructionList);
        }
    }

    private ArrayList<ArrayList<InstructionNode>> getExecutableInstructions() {
        ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(instructions);
        return eie.getExecutableInstructions(currentOrder);
    }

    /** Take a list of lists of line numbers the corresponding instruction block jumps to. Replace the line numbers by
     * the index numbers of the corresponding instruction block in the instructions list. Save result in indexToJumpTo.
     * @param indexToJumpTo The list of lists of index numbers.
     * @param linesToJumpTo The list of lists of line numbers.
     */
    private void replaceLinesByIndex(ArrayList<Set<Integer>> indexToJumpTo, ArrayList<ArrayList<Integer>> linesToJumpTo) {
        for (ArrayList<Integer> lines : linesToJumpTo) {
            Set<Integer> indexSet = new HashSet<>();
            for(int i = 0; i < instructions.size(); i++) {
                if(!instructions.get(i).isEmpty()) {
                    InstructionNode node = instructions.get(i).get(0);
                    if (lines.contains(node.getLine())) {
                        indexSet.add(i);
                    }
                }
            }
            indexToJumpTo.add(indexSet);
        }
    }

    /**
     * Transform list of list of instructions to a list of list of line of ints (of the lines of the instructions).
     * @param instructions The list of list of instructions.
     * @return The list of list of line of ints.
     */
    private ArrayList<ArrayList<Integer>> instructionsToLines(ArrayList<ArrayList<InstructionNode>> instructions) {
        ArrayList<ArrayList<Integer>> lines = new ArrayList<>();
        for (ArrayList<InstructionNode> instruction : instructions) {
            lines.add(new ArrayList<>(instruction.stream().map(InstructionNode::getLine).collect(Collectors.toList())));
        }
        return lines;
    }
}
