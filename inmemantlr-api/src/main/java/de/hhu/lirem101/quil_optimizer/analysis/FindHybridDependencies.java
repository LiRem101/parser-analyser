package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.*;

public class FindHybridDependencies {

    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<Map<Integer, Set<Integer>>> hybridDependencyList = new ArrayList<>();
    private boolean calculated = false;

    public FindHybridDependencies(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions = instructions;
    }

    /**
     * Saves hybrid Nodes in hybrid Dependencies.
     */
    private void findHybridNodes() {
        for (ArrayList<InstructionNode> instructionList : instructions) {
            Map<Integer, Set<Integer>> hybridDependencies = new HashMap<>();
            Set<Integer> handledLines = new HashSet<>();
            ArrayList<InstructionNode> hybridNodes = (ArrayList<InstructionNode>) instructionList.stream()
                    .filter(instruction -> instruction.getLineType() != LineType.CLASSICAL && instruction.getLineType() != LineType.QUANTUM)
                    .collect(java.util.stream.Collectors.toList());
            for (InstructionNode hybridNode : hybridNodes) {
                int line = hybridNode.getCodelines().get(0);
                Set<Integer> dependentLines = hybridNode.getDependencies().stream()
                        .flatMap(x -> x.getCodelines().stream())
                        .filter(x -> !handledLines.contains(x))
                        .collect(java.util.stream.Collectors.toCollection(HashSet::new));
                handledLines.addAll(dependentLines);
                hybridDependencies.put(line, dependentLines);
            }

            hybridDependencyList.add(hybridDependencies);
        }
    }

    public ArrayList<Map<Integer, Set<Integer>>> getHybridDependencies() {
        if (!calculated) {
            findHybridNodes();
            calculated = true;
        }
        return hybridDependencyList;
    }

    /**
     * Add information about hybrid dependencies into JsonObjectBuilder.
     * @param jsonBuilder The JsonObjectBuilder to add the information to.
     */
    public void addDeadVariablesToJson(JsonObjectBuilder jsonBuilder) {
        if (!calculated) {
            findHybridNodes();
            calculated = true;
        }
        JsonArrayBuilder hybridDependencies = Json.createArrayBuilder();
        for (Map<Integer, Set<Integer>> hybridDependency : hybridDependencyList) {
            JsonObjectBuilder hybridDependencyJson = Json.createObjectBuilder();
            for (Map.Entry<Integer, Set<Integer>> entry : hybridDependency.entrySet()) {
                JsonArrayBuilder dependentLines = Json.createArrayBuilder();
                for (Integer line : entry.getValue()) {
                    dependentLines.add(line);
                }
                hybridDependencyJson.add(entry.getKey().toString(), dependentLines);
            }
            hybridDependencies.add(hybridDependencyJson);
        }
        jsonBuilder.add("HybridDependencies", hybridDependencies);
    }
}
