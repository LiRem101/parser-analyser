package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.analysis.ConstantPropagator;
import de.hhu.lirem101.quil_optimizer.analysis.DeadCodeAnalyser;
import de.hhu.lirem101.quil_optimizer.analysis.FindHybridDependencies;
import de.hhu.lirem101.quil_optimizer.analysis.LiveVariableAnalyser;
import de.hhu.lirem101.quil_optimizer.transformation.DeadCodeEliminator;
import de.hhu.lirem101.quil_optimizer.transformation.ReOrdererForHybridExecution;
import org.snt.inmemantlr.tree.ParseTreeNode;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

import static de.hhu.lirem101.quil_optimizer.ControlStructureRemover.removeControlStructures;

public class OptimizingQuil {
    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private ArrayList<ArrayList<InstructionNode>> currentOrder = new ArrayList<>();
    private final ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
    private final Set<String> readoutParams = new HashSet<>();


    /**
     * Constructor for the OptimizingQuil class. Creates a list of list of instructions from a control flow block.
     * @param block The control flow block to create the instructions from.
     * @param classes A map with the line number as key and the line type as value.
     * @param root The root node of the parse tree.
     * @param readoutParams The classical params whose values are read out at the end of the program, i.e. that will not
     *                      be dead.
     * @param quilCode The Quil code as an array of strings.
     */
    public OptimizingQuil(ControlFlowBlock block, Map<Integer, LineType> classes, ParseTreeNode root, Set<String> readoutParams, String[] quilCode) {
        InstructionListCreator ilc = new InstructionListCreator(block, classes);
        SortingNodesToLines snl = new SortingNodesToLines(root);
        Map<Integer, ParseTreeNode> sortedNodes = snl.getSortedNodes();
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        ArrayList<ArrayList<InstructionNode>> instructionsWithControlStructures = ilc.getInstructions();
        sorter.appendNodeToInstructions(instructionsWithControlStructures);
        createLinksOfInstructions(instructionsWithControlStructures);
        ArrayList<ArrayList<Integer>> linesToJumpTo = ilc.getLinesToJumpTo();
        replaceLinesByIndex(indexToJumpTo, linesToJumpTo, instructionsWithControlStructures);
        this.readoutParams.addAll(readoutParams);
        this.instructions = removeControlStructures(instructionsWithControlStructures);
        addQuilTextToInstructions(this.instructions, quilCode);
        createListsForOrderedInstructions(this.instructions);
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
        addInstructionsToJson(startBuilder, instructions);
        jsonBuilder.add("Start", startBuilder);

        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        ArrayList<Set<Integer>> deadLines = new ArrayList<>();
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencies = new ArrayList<>();

        for(String optimizationStep : optimizationSteps) {
            switch (optimizationStep) {
                case "LiveVariableAnalysis":
                    LiveVariableAnalyser lva = new LiveVariableAnalyser(currentOrder, readoutParams);
                    lva.addDeadVariablesToJson(jsonBuilder);
                    break;
                case "DeadCodeAnalysis":
                    DeadCodeAnalyser dca = new DeadCodeAnalyser(currentOrder, indexToJumpTo);
                    indizesOfDeadLineBlocks = dca.getIndizesOfDeadLines();
                    deadLines = dca.getDeadLines();
                    dca.addDeadVariablesToJson(jsonBuilder);
                    break;
                case "ConstantPropagation":
                    ConstantPropagator cp = new ConstantPropagator(currentOrder);
                    cp.addConstantVariablesToJson(jsonBuilder);
                    break;
                case "HybridDependencies":
                    FindHybridDependencies fhd = new FindHybridDependencies(currentOrder);
                    hybridDependencies = fhd.getHybridDependencies();
                    fhd.addDeadVariablesToJson(jsonBuilder);
                    break;
                case "DeadCodeElimination":
                    DeadCodeEliminator dce = new DeadCodeEliminator(currentOrder, deadLines, indizesOfDeadLineBlocks);
                    dce.addDeadVariablesToJson(jsonBuilder);
                    break;
                case "ReOrdering":
                    ReOrdererForHybridExecution rofhe = new ReOrdererForHybridExecution(currentOrder, hybridDependencies);
                    currentOrder = rofhe.reOrderInstructions();
                    JsonObjectBuilder reOrderBuilder = Json.createObjectBuilder();
                    addInstructionsToJson(reOrderBuilder, currentOrder);
                    jsonBuilder.add("ReOrdered", reOrderBuilder);
                    break;
            }
        }
        JsonObject json = jsonBuilder.build();
        String jsonString = json.toString();

        return jsonString;
    }


    /**
     * Let instructions create their linking.
     * @param instructionList Instruction lists that need parameter links.
     */
    private void createLinksOfInstructions(ArrayList<ArrayList<InstructionNode>> instructionList) {
        for (ArrayList<InstructionNode> instruction : instructionList) {
            Map<String, InstructionNode> lastInstructionOfParams = new HashMap<>();
            for (InstructionNode node : instruction) {
                node.setParameterLinks(lastInstructionOfParams);
            }
        }
    }

    private void createListsForOrderedInstructions(ArrayList<ArrayList<InstructionNode>> instructionLists) {
        for (ArrayList<InstructionNode> instructionList : instructionLists) {
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
     * @param instructionLists
     */
    private void replaceLinesByIndex(ArrayList<Set<Integer>> indexToJumpTo, ArrayList<ArrayList<Integer>> linesToJumpTo, ArrayList<ArrayList<InstructionNode>> instructionLists) {
        for (ArrayList<Integer> lines : linesToJumpTo) {
            Set<Integer> indexSet = new HashSet<>();
            for(int i = 0; i < instructionLists.size(); i++) {
                if(!instructionLists.get(i).isEmpty()) {
                    InstructionNode node = instructionLists.get(i).get(0);
                    if (lines.contains(node.getLine())) {
                        indexSet.add(i);
                    }
                }
            }
            indexToJumpTo.add(indexSet);
        }
    }

    /**
     * Adds lines to jsonBuilder in the form of "LineNumber: LineContent".
     * The list of list of instructions is used for that.
     * @param instructions The list of list of instructions.
     */
    private void addInstructionsToJson(JsonObjectBuilder json, ArrayList<ArrayList<InstructionNode>> instructions) {
        for (ArrayList<InstructionNode> instruction : instructions) {
            JsonObjectBuilder instructionBuilder = Json.createObjectBuilder();
            for (InstructionNode node : instruction) {
                instructionBuilder.add(Integer.toString(node.getLine()), node.getLineText());
            }
            json.add(instruction.get(0).getName(), instructionBuilder);
        }
    }

    /**
     * Adds the quil code to the instructions.
     */
    public void addQuilTextToInstructions(ArrayList<ArrayList<InstructionNode>> instructions, String[] quilCode) {
        for (ArrayList<InstructionNode> instruction : instructions) {
            for (InstructionNode node : instruction) {
                node.setLineText(quilCode[node.getLine()-1]);
            }
        }
    }
}
