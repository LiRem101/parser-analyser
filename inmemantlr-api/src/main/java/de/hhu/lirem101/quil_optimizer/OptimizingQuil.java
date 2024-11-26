package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.analysis.ConstantPropagator;
import de.hhu.lirem101.quil_optimizer.analysis.DeadCodeAnalyser;
import de.hhu.lirem101.quil_optimizer.analysis.FindHybridDependencies;
import de.hhu.lirem101.quil_optimizer.analysis.LiveVariableAnalyser;
import de.hhu.lirem101.quil_optimizer.transformation.ConstantFolder;
import de.hhu.lirem101.quil_optimizer.transformation.DeadCodeEliminator;
import de.hhu.lirem101.quil_optimizer.transformation.JITQuantumExecuter;
import de.hhu.lirem101.quil_optimizer.transformation.ReOrdererForHybridExecution;
import org.snt.inmemantlr.tree.ParseTreeNode;

import javax.json.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
     * Fuzz optimization steps and save in a json file. If an error is thrown, save this as well.
     * @param jsonFileName The name of the json file to save the results in.
     * @param iterations The number of iterations.
     * @param numberOfOptimizations The number of optimizations to apply in one iteration.
     * @return The result json string and the applied optimizations.
     */
    public void fuzzOptimization(String jsonFileName,int iterations, int numberOfOptimizations) {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("LiveVariableAnalysis", "DeadCodeAnalysis",
                "ConstantPropagation", "HybridDependencies", "DeadCodeElimination", "ReOrdering", "ConstantFolding",
                "QuantumJIT"));
        Random random = new Random();
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder instructionNumberBuilder = Json.createArrayBuilder();
        ArrayList<Integer> numberOfInstructions = numberOfInstructions(currentOrder);
        int minNumberOfInstructions = numberOfInstructions.stream().mapToInt(x -> x).sum();
        int minimumIndex = -1;
        numberOfInstructions.forEach(instructionNumberBuilder::add);
        result.add("OriginalNumberOfInstructions", instructionNumberBuilder);
        for(int i = 0; i < iterations; i++) {
            JsonObjectBuilder iterationBuilder = Json.createObjectBuilder();
            iterationBuilder.add("Iteration", i);
            JsonArrayBuilder appliedOptBuilder = Json.createArrayBuilder();
            ArrayList<String> appliedOptimizations = new ArrayList<>();
            for(int j = 0; j < numberOfOptimizations; j++) {
                int index = random.nextInt(optimizationSteps.size());
                String optimization = optimizationSteps.get(index);
                appliedOptimizations.add(optimization);
                appliedOptBuilder.add(optimization);
            }
            iterationBuilder.add("AppliedOptimizations", appliedOptBuilder);
            try {
                JsonObjectBuilder resultJson = applyOptimizationSteps(appliedOptimizations);
                numberOfInstructions = numberOfInstructions(currentOrder);
                JsonArrayBuilder numberOfInstructionsBuilder = Json.createArrayBuilder();
                numberOfInstructions.forEach(numberOfInstructionsBuilder::add);
                iterationBuilder.add("FinalNumberOfInstructions", numberOfInstructionsBuilder);
                iterationBuilder.add("Optimizations", resultJson);
                int totalNumberOfInstructions = numberOfInstructions.stream().mapToInt(x -> x).sum();
                if(totalNumberOfInstructions < minNumberOfInstructions) {
                    minNumberOfInstructions = totalNumberOfInstructions;
                    minimumIndex = i;
                }
            } catch (Exception e) {
                JsonObjectBuilder errorBuilder = Json.createObjectBuilder();
                JsonArrayBuilder stackTraceBuilder = Json.createArrayBuilder();
                errorBuilder.add("Error", e.getMessage());
                // Add stack trace to stackTraceBuilder
                for (StackTraceElement element : e.getStackTrace()) {
                    stackTraceBuilder.add(element.toString());
                }
                errorBuilder.add("StackTrace", stackTraceBuilder);
                iterationBuilder.add("Error", errorBuilder);
                System.err.println("Error in iteration " + i);
            }
            createListsForOrderedInstructions(this.instructions);
            result.add("Iteration" + i, iterationBuilder);
        }
        result.add("MinimumIndex", minimumIndex);
        result.add("MinimumNumberOfInstructions", minNumberOfInstructions);

        JsonObject json = result.build();

        try (OutputStream os = new FileOutputStream(jsonFileName);
             JsonWriter jsonWriter = Json.createWriter(os)) {
            jsonWriter.writeObject(json);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Apply optimization steps to the instructions. Save the optimized instructions in currentOrder. Create a
     * JsonObjectBuilder with the original instructions, the optimized instructions and the applied optimizations.
     * @param optimizationSteps The optimization steps to apply as strings.
     * @return The JsonObjectBuilder with the original instructions, the optimized instructions and the applied
     * optimizations.
     */
    public JsonObjectBuilder applyOptimizationSteps(ArrayList<String> optimizationSteps){
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
                    if(!deadLines.isEmpty()) {
                        DeadCodeEliminator dce = new DeadCodeEliminator(currentOrder, deadLines, indizesOfDeadLineBlocks);
                        dce.addDeadVariablesToJson(jsonBuilder);
                    }
                    break;
                case "ReOrdering":
                    if(!hybridDependencies.isEmpty()) {
                        ReOrdererForHybridExecution rofhe = new ReOrdererForHybridExecution(currentOrder, hybridDependencies);
                        currentOrder = rofhe.reOrderInstructions();
                        JsonObjectBuilder reOrderBuilder = Json.createObjectBuilder();
                        addInstructionsToJson(reOrderBuilder, currentOrder);
                        jsonBuilder.add("ReOrdered", reOrderBuilder);
                    }
                    break;
                case "ConstantFolding":
                    ConstantFolder cf = new ConstantFolder(currentOrder);
                    ArrayList<ArrayList<Integer>> changedLines = cf.getAdaptedLines();
                    JsonObjectBuilder constantFolderBuilder = Json.createObjectBuilder();
                    addLinesToJson(constantFolderBuilder, currentOrder, changedLines);
                    jsonBuilder.add("ConstantFolding", constantFolderBuilder);
                    break;
                case "QuantumJIT":
                    if (!hybridDependencies.isEmpty()) {
                        JITQuantumExecuter jqe = new JITQuantumExecuter(hybridDependencies.get(0), currentOrder.get(0));
                        ArrayList<InstructionNode> reOrdered = jqe.reorderInstructions();
                        JsonObjectBuilder reOrderJIT = Json.createObjectBuilder();
                        addInstructionsToJson(reOrderJIT, new ArrayList<>(Collections.singletonList(reOrdered)));
                        jsonBuilder.add("QuantumJITOrdering", reOrderJIT);
                        if(!reOrdered.isEmpty()) {
                            currentOrder.set(0, reOrdered);
                        }
                    }
                    break;
            }
        }
        JsonObjectBuilder finalResult = Json.createObjectBuilder();
        addInstructionsToJson(finalResult, currentOrder);
        jsonBuilder.add("FinalResult", finalResult);
        return jsonBuilder;
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
        currentOrder = new ArrayList<>();
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
            if(!instruction.isEmpty()) {
                JsonObjectBuilder instructionBuilder = Json.createObjectBuilder();
                for (InstructionNode node : instruction) {
                    instructionBuilder.add(Integer.toString(node.getLine()), node.getLineText());
                }
                json.add(instruction.get(0).getName(), instructionBuilder);
            }
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

    /**
     * Adds lines of given line numbers to jsonBuilder in the form of "LineNumber: LineContent".
     * @param json The jsonBuilder to add the lines to.
     * @param instructions The list of list of line numbers.
     * @param lines The line numbers to add.
     */
    private void addLinesToJson(JsonObjectBuilder json, ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<ArrayList<Integer>> lines) {
        ArrayList<ArrayList<InstructionNode>> theseInstructions = new ArrayList<>();
        for(int i = 0; i < instructions.size(); i++) {
            int finalI = i;
            ArrayList<InstructionNode> ins = instructions.get(i).stream()
                    .filter(x -> lines.get(finalI).contains(x.getLine()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            theseInstructions.add(ins);
        }
        addInstructionsToJson(json, theseInstructions);
    }

    /**
     * Calculate number of instructions in the instructions list.
     * @param instructionList The list of list of instructions.
     * @return The number of instructions.
     */
    private ArrayList<Integer> numberOfInstructions(ArrayList<ArrayList<InstructionNode>> instructionList) {
        ArrayList<Integer> numberOfInstructions = new ArrayList<>();
        for (ArrayList<InstructionNode> instructions : instructionList) {
            numberOfInstructions.add(instructions.size());
        }
        return numberOfInstructions;
    }
}
