package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeadCodeAnalyser {
    private final ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    private final ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
    private final ArrayList<Set<Integer>> deadLines = new ArrayList<>();
    private final Set<Integer> indizesOfDeadLines = new HashSet<>();
    private boolean calculated = false;


    public DeadCodeAnalyser(ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<Set<Integer>> indexToJumpTo) {
        this.instructions.addAll(instructions);
        this.indexToJumpTo.addAll(indexToJumpTo);
        for(ArrayList<InstructionNode> i : instructions) {
            deadLines.add(new HashSet<>());
        }
    }

    /**
     * Returns the indizes of the blocks that are not jumped to anymore.
     * @return The indizes of the blocks that are not jumped to anymore.
     */
    public Set<Integer> getIndizesOfDeadLines() {
        if(!calculated) {
            checkWhichInstructionsAreDead();
            checkWhichIndizesAreNoLongerJumpedTo();
        }
        calculated = true;
        return indizesOfDeadLines;
    }

    /**
     * Returns the dead lines in the instructions.
     * @return The dead lines in the instructions.
     */
    public ArrayList<Set<Integer>> getDeadLines() {
        if(!calculated) {
            checkWhichInstructionsAreDead();
            checkWhichIndizesAreNoLongerJumpedTo();
        }
        calculated = true;
        return deadLines;
    }

    /**
     * Add information about dead instructions into JsonObjectBuilder.
     * @param jsonBuilder The JsonObjectBuilder to add the information to.
     */
    public void addDeadVariablesToJson(JsonObjectBuilder jsonBuilder) {
        if (!calculated) {
            checkWhichInstructionsAreDead();
            checkWhichIndizesAreNoLongerJumpedTo();
        }
        JsonObjectBuilder deadCodeAnalysis = Json.createObjectBuilder();
        JsonObjectBuilder deadLinesJson = Json.createObjectBuilder();
        for(int i = 0; i < deadLines.size(); i++) {
            deadLinesJson.add(Integer.toString(i), deadLines.get(i).toString());
        }
        deadCodeAnalysis.add("DeadLines", deadLinesJson);

        JsonObjectBuilder deadInstructionBlocks = Json.createObjectBuilder();
        for(int i = 0; i < indizesOfDeadLines.size(); i++) {
            deadInstructionBlocks.add(Integer.toString(i), indizesOfDeadLines.toString());
        }
        deadCodeAnalysis.add("DeadInstructionBlocks", deadInstructionBlocks);

        jsonBuilder.add("DeadCodeAnalysis", deadCodeAnalysis);
    }

    /**
     * Determine the indizies of instruction blocks no longer jumped to. Consider that the first block is the start
     * block.
     */
    private void checkWhichIndizesAreNoLongerJumpedTo() {
        if(instructions.size() == 1) {
            return;
        }
        indizesOfDeadLines.addAll(IntStream.range(1, instructions.size()).boxed().collect(Collectors.toSet()));
        Set<Integer> aliveIndizes = indexToJumpTo.stream().flatMap(Set::stream).collect(Collectors.toSet());
        indizesOfDeadLines.removeAll(aliveIndizes);
    }

    /**
     * Find the dead lines in the instructions.
     * Instructions are dead if all their classical variables that are assigned are dead and all their quantum variables
     * are dead.
     */
    private void checkWhichInstructionsAreDead() {
        for(int i = 0; i < instructions.size(); i++) {
            Set<Integer> deadLinesInBlock = new HashSet<>();
            for(InstructionNode in : instructions.get(i)) {
                in.calculateDeadCode();
                if(in.getShownToBeDead()) {
                    deadLinesInBlock.add(in.getLine());
                }
            }
            deadLines.set(i, deadLinesInBlock);
        }
    }

}
