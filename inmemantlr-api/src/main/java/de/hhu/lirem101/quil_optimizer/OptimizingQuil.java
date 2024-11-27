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
     * @param block The control flow block to create the instructions from.
     * @param classes A map with the line number as key and the line type as value.
     * @param root The root node of the parse tree.
     * @param readoutParams The classical params whose values are read out at the end of the program, i.e. that will not
     *                      be dead.
     * @param quilCode The Quil code as an array of strings.
     */
    public static void fuzzOptimization(String jsonFileName, int iterations, int numberOfOptimizations,
                                        ControlFlowBlock block, Map<Integer, LineType> classes, ParseTreeNode root,
                                        Set<String> readoutParams, String[] quilCode) {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("LiveVariableAnalysis", "DeadCodeAnalysis",
                "ConstantPropagation", "HybridDependencies", "DeadCodeElimination", "ReOrdering", "ConstantFolding",
                "QuantumJIT"));
        Random random = new Random();
        ArrayList<ArrayList<String>> optimizations = new ArrayList<>();
        for(int i = 0; i < iterations; i++) {
            ArrayList<String> iterationOptimizations = new ArrayList<>();
            for(int j = 0; j < numberOfOptimizations; j++) {
                int randomIndex = random.nextInt(optimizationSteps.size());
                iterationOptimizations.add(optimizationSteps.get(randomIndex));
            }
            optimizations.add(iterationOptimizations);
        }
        JsonObject result = fuzzOptimization(optimizations, block, classes, root, readoutParams, quilCode);

        try (OutputStream os = new FileOutputStream(jsonFileName);
             JsonWriter jsonWriter = Json.createWriter(os)) {
            jsonWriter.writeObject(result);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Fuzz optimization steps and save in a json file. If an error is thrown, save this as well.
     * @param optimizations The list of lists of optimization steps to apply.
     * @param block The control flow block to create the instructions from.
     * @param classes A map with the line number as key and the line type as value.
     * @param root The root node of the parse tree.
     * @param readoutParams The classical params whose values are read out at the end of the program, i.e. that will not
     *                      be dead.
     * @param quilCode The Quil code as an array of strings.
     * @return The JsonObject with the results of the optimizations.
     */
    public static JsonObject fuzzOptimization(ArrayList<ArrayList<String>> optimizations, ControlFlowBlock block,
                                              Map<Integer, LineType> classes, ParseTreeNode root,
                                              Set<String> readoutParams, String[] quilCode) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder instructionNumberBuilder = Json.createArrayBuilder();
        OptimizingQuil oQuil = new OptimizingQuil(block, classes, root, readoutParams, quilCode);
        ArrayList<Integer> numberOfInstructions = numberOfInstructions(oQuil.currentOrder);
        ArrayList<Integer> numberOfQuantumInstructions = numberOfQuantumInstructions(oQuil.currentOrder);

        int minNumberOfInstructions = numberOfInstructions.stream().mapToInt(x -> x).sum();
        int minimumInstructionsIndex = -1;
        int minQuantumInstructions = numberOfQuantumInstructions.stream().mapToInt(x -> x).sum();
        int minimumQuantumInstructionsIndex = -1;
        int minDifference = oQuil.getAmountOfInstructionsBetweenFirstAndLastQuantumInstruction();
        int minimumDifferenceIndex = -1;

        numberOfInstructions.forEach(instructionNumberBuilder::add);
        result.add("OriginalNumberOfInstructions", instructionNumberBuilder);
        result.add("OriginalNumberOfQuantumInstructions", minQuantumInstructions);
        result.add("OriginalDifferenceBetweenFirstAndLastQuantumInstruction", minDifference);
        for(int i = 0; i < optimizations.size(); i++) {
            if(i != 0) {
                oQuil = new OptimizingQuil(block, classes, root, readoutParams, quilCode);
            }

            JsonObjectBuilder iterationBuilder = Json.createObjectBuilder();
            iterationBuilder.add("Iteration", i);
            JsonArrayBuilder appliedOptBuilder = Json.createArrayBuilder();
            optimizations.get(i).forEach(appliedOptBuilder::add);
            iterationBuilder.add("AppliedOptimizations", appliedOptBuilder);
            try {
                JsonObjectBuilder resultJson = oQuil.applyOptimizationSteps(optimizations.get(i));
                numberOfInstructions = numberOfInstructions(oQuil.currentOrder);
                JsonArrayBuilder numberOfInstructionsBuilder = Json.createArrayBuilder();
                numberOfInstructions.forEach(numberOfInstructionsBuilder::add);
                iterationBuilder.add("FinalNumberOfInstructions", numberOfInstructionsBuilder);

                int totalNumberOfInstructions = numberOfInstructions.stream().mapToInt(x -> x).sum();
                if(totalNumberOfInstructions < minNumberOfInstructions) {
                    minNumberOfInstructions = totalNumberOfInstructions;
                    minimumInstructionsIndex = i;
                }
                int totalQuantumInstructions = numberOfQuantumInstructions(oQuil.currentOrder).stream().mapToInt(x -> x).sum();
                if(totalQuantumInstructions < minQuantumInstructions) {
                    minQuantumInstructions = totalQuantumInstructions;
                    minimumQuantumInstructionsIndex = i;
                }
                int difference = oQuil.getAmountOfInstructionsBetweenFirstAndLastQuantumInstruction();
                if(difference < minDifference) {
                    minDifference = difference;
                    minimumDifferenceIndex = i;
                }

                iterationBuilder.add("NumberOfQuantumInstructions", totalQuantumInstructions);
                iterationBuilder.add("DifferenceBetweenFirstAndLastQuantumInstruction", difference);
                iterationBuilder.add("Optimizations", resultJson);

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
            result.add("Iteration" + i, iterationBuilder);
        }
        result.add("MinimumInstructionsIndex", minimumInstructionsIndex);
        result.add("MinimumNumberOfInstructions", minNumberOfInstructions);

        result.add("MinimumQuantumInstructionsIndex", minimumQuantumInstructionsIndex);
        result.add("MinimumNumberOfQuantumInstructions", minQuantumInstructions);

        result.add("MinimumDifferenceIndex", minimumDifferenceIndex);
        result.add("MinimumDifference", minDifference);

        JsonObject json = result.build();

        return json;
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
        JsonArrayBuilder startBuilder = Json.createArrayBuilder();
        addInstructionsToJson(startBuilder, instructions);
        jsonBuilder.add("Start", startBuilder);

        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        ArrayList<Set<Integer>> deadLines = new ArrayList<>();
        ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencies = new ArrayList<>();

        JsonArrayBuilder optimizationStepsBuilder = Json.createArrayBuilder();

        for(String optimizationStep : optimizationSteps) {
            JsonObjectBuilder appliedSteps = Json.createObjectBuilder();
            appliedSteps.add("Optimization", optimizationStep);
            switch (optimizationStep) {
                case "LiveVariableAnalysis":
                    LiveVariableAnalyser lva = new LiveVariableAnalyser(currentOrder, readoutParams);
                    appliedSteps.add("Result", lva.addDeadVariablesToJson());
                    break;
                case "DeadCodeAnalysis":
                    DeadCodeAnalyser dca = new DeadCodeAnalyser(currentOrder, indexToJumpTo);
                    indizesOfDeadLineBlocks = dca.getIndizesOfDeadLines();
                    deadLines = dca.getDeadLines();
                    appliedSteps.add("Result", dca.addDeadVariablesToJson());
                    break;
                case "ConstantPropagation":
                    ConstantPropagator cp = new ConstantPropagator(currentOrder);
                    appliedSteps.add("Result", cp.addConstantVariablesToJson());
                    break;
                case "HybridDependencies":
                    FindHybridDependencies fhd = new FindHybridDependencies(currentOrder);
                    hybridDependencies = fhd.getHybridDependencies();
                    appliedSteps.add("Result", fhd.addDeadVariablesToJson());
                    break;
                case "DeadCodeElimination":
                    if(!deadLines.isEmpty()) {
                        DeadCodeEliminator dce = new DeadCodeEliminator(currentOrder, deadLines, indizesOfDeadLineBlocks);
                        appliedSteps.add("Result", dce.addDeadVariablesToJson());
                    }
                    break;
                case "ReOrdering":
                    if(!hybridDependencies.isEmpty()) {
                        ReOrdererForHybridExecution rofhe = new ReOrdererForHybridExecution(currentOrder, hybridDependencies);
                        currentOrder = rofhe.reOrderInstructions();
                        JsonArrayBuilder reOrderBuilder = Json.createArrayBuilder();
                        addInstructionsToJson(reOrderBuilder, currentOrder);
                        appliedSteps.add("Result", reOrderBuilder);
                    }
                    break;
                case "ConstantFolding":
                    ConstantFolder cf = new ConstantFolder(currentOrder);
                    ArrayList<ArrayList<Integer>> changedLines = cf.getAdaptedLines();
                    JsonArrayBuilder constantFolderBuilder = Json.createArrayBuilder();
                    addLinesToJson(constantFolderBuilder, currentOrder, changedLines);
                    appliedSteps.add("Result", constantFolderBuilder);
                    break;
                case "QuantumJIT":
                    if (!hybridDependencies.isEmpty()) {
                        JITQuantumExecuter jqe = new JITQuantumExecuter(hybridDependencies.get(0), currentOrder.get(0));
                        ArrayList<InstructionNode> reOrdered = jqe.reorderInstructions();
                        JsonArrayBuilder reOrderJIT = Json.createArrayBuilder();
                        addInstructionsToJson(reOrderJIT, new ArrayList<>(Collections.singletonList(reOrdered)));
                        appliedSteps.add("Result", reOrderJIT);
                        if(!reOrdered.isEmpty()) {
                            currentOrder.set(0, reOrdered);
                        }
                    }
                    break;
            }
            optimizationStepsBuilder.add(appliedSteps);
        }
        jsonBuilder.add("Optimizations", optimizationStepsBuilder);
        JsonArrayBuilder finalResult = Json.createArrayBuilder();
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
            ArrayList<InstructionNode> instructionListCopy = new ArrayList<>();
            for(InstructionNode node : instructionList) {
                instructionListCopy.add(node.copyInstruction());
            }
            currentOrder.add(instructionListCopy);
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
    private void addInstructionsToJson(JsonArrayBuilder json, ArrayList<ArrayList<InstructionNode>> instructions) {
        for (ArrayList<InstructionNode> instruction : instructions) {
            if(!instruction.isEmpty()) {
                JsonArrayBuilder instructionBuilder = Json.createArrayBuilder();
                for (InstructionNode node : instruction) {
                    instructionBuilder.add(node.getLine() + ": " + node.getLineText());
                }
                json.add(instructionBuilder);
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
     * @param json The jsonArrayBuilder to add the lines to.
     * @param instructions The list of list of line numbers.
     * @param lines The line numbers to add.
     */
    private void addLinesToJson(JsonArrayBuilder json, ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<ArrayList<Integer>> lines) {
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
     * Get the amount of instructions between the first and the last quantum instruction (including). Take the index of
     * the last quantum instruction in the halt instruction block and the index of the first quantum instruction in the
     * first instruction block. Return the difference. If the first block has no quantum instruction, return the last
     * index. If the last block has no quantum instruction, return 0. The complete optimization should not
     * have a purely classical block anyway.
     * @return The amount of instructions between the first and the last quantum instruction.
     */
    private int getAmountOfInstructionsBetweenFirstAndLastQuantumInstruction() {
        ArrayList<InstructionNode> firstInstructionBlock = currentOrder.get(0);
        ArrayList<InstructionNode> lastInstructionBlock = currentOrder.get(getHaltIndex());
        InstructionNode firstQuantumInstruction = firstInstructionBlock.stream()
                .filter(x -> x.getLineType() == LineType.QUANTUM)
                .findFirst()
                .orElse(null);
        int firstQuantumIndex = firstQuantumInstruction != null ? firstInstructionBlock.indexOf(firstQuantumInstruction) : firstInstructionBlock.size();

        ArrayList<InstructionNode> lastQuantumInstruction = lastInstructionBlock.stream()
                .filter(x -> x.getLineType() == LineType.QUANTUM)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        int lastQuantumIndex = lastQuantumInstruction.isEmpty() ? 0 : lastInstructionBlock.indexOf(lastQuantumInstruction.get(lastQuantumInstruction.size()-1));

        return lastQuantumIndex - firstQuantumIndex;
    }

    /**
     * Return the index of the halt instruction block, i.e. the one that does not branch to any other.
     * @return The index of the halt instruction block.
     */
    private int getHaltIndex() {
        for (int i = 0; i < indexToJumpTo.size(); i++) {
            if (indexToJumpTo.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Calculate number of instructions in the instructions list.
     * @param instructionList The list of list of instructions.
     * @return The number of instructions.
     */
    private static ArrayList<Integer> numberOfInstructions(ArrayList<ArrayList<InstructionNode>> instructionList) {
        ArrayList<Integer> numberOfInstructions = new ArrayList<>();
        for (ArrayList<InstructionNode> instructions : instructionList) {
            numberOfInstructions.add(instructions.size());
        }
        return numberOfInstructions;
    }

    /**
     * Calculate number of quantum instructions in the instructions list.
     * @param instructionList The list of list of instructions.
     * @return The number of quantum instructions.
     */
    private static ArrayList<Integer> numberOfQuantumInstructions(ArrayList<ArrayList<InstructionNode>> instructionList) {
        ArrayList<Integer> numberOfInstructions = new ArrayList<>();
        for (ArrayList<InstructionNode> instructions : instructionList) {
            ArrayList<InstructionNode> quantumInstructions = instructions.stream()
                    .filter(x -> x.getLineType() == LineType.QUANTUM
                            || x.getLineType() == LineType.CLASSICAL_INFLUENCES_QUANTUM
                            || x.getLineType() == LineType.QUANTUM_INFLUENCES_CLASSICAL)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            numberOfInstructions.add(quantumInstructions.size());
        }
        return numberOfInstructions;
    }
}
