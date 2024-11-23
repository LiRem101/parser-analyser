package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeadCodeEliminator {
    ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
    Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
    ArrayList<Set<Integer>> deadLines = new ArrayList<>();
    boolean calculated = false;

    public DeadCodeEliminator(ArrayList<ArrayList<InstructionNode>> instructions, ArrayList<Set<Integer>> deadLines, Set<Integer> indizesOfDeadLineBlocks) {
        this.deadLines.addAll(deadLines);
        this.indizesOfDeadLineBlocks.addAll(indizesOfDeadLineBlocks);
        this.instructions.addAll(instructions);
    }

    public void eliminateDeadCode() {
        if(calculated || deadLines.isEmpty()) {
            return;
        }
        removeDeadBlocks();
        removeDeadLines();
        calculated = true;
    }

    /**
     * Add information about dead instructions into JsonObjectBuilder.
     * @param jsonBuilder The JsonObjectBuilder to add the information to.
     */
    public void addDeadVariablesToJson(JsonObjectBuilder jsonBuilder) {
        if(deadLines.isEmpty()) {
            jsonBuilder.add("DeadCodeElimination", Json.createObjectBuilder());
            return;
        }
        if (!calculated) {
            removeDeadBlocks();
            removeDeadLines();
        }
        JsonObjectBuilder deadCodeAnalysis = Json.createObjectBuilder();
        JsonObjectBuilder deadLinesJson = Json.createObjectBuilder();
        for(int i = 0; i < deadLines.size(); i++) {
            JsonArrayBuilder theseDeadLines = Json.createArrayBuilder();
            for(int line : deadLines.get(i)) {
                theseDeadLines.add(line);
            }
            deadLinesJson.add(Integer.toString(i), theseDeadLines);
        }
        deadCodeAnalysis.add("RemovedDeadLines", deadLinesJson);

        JsonArrayBuilder deadInstructionBlocks = Json.createArrayBuilder();
        for(int i : indizesOfDeadLineBlocks) {
            deadInstructionBlocks.add(Integer.toString(i));
        }
        deadCodeAnalysis.add("DeadInstructionBlocks", deadInstructionBlocks);

        jsonBuilder.add("DeadCodeElimination", deadCodeAnalysis);
    }

    /**
     * Empty dead blocks.
     */
    private void removeDeadBlocks() {
        for (int i : indizesOfDeadLineBlocks) {
            instructions.get(i).clear();
        }
    }

    /**
     * Remove dead lines from instructions.
     */
    private void removeDeadLines() {
        for(int i = 0; i < instructions.size(); i++) {
            ArrayList<InstructionNode> instructionList = instructions.get(i);
            for(int line : deadLines.get(i)) {
                InstructionNode node= instructionList.stream()
                        .filter(instructionNode -> instructionNode.getLine() == line)
                        .findFirst()
                        .orElse(null);
                if(node != null) {
                    node.removeConnections();
                    instructionList.remove(node);
                }
            }
        }
    }

}
