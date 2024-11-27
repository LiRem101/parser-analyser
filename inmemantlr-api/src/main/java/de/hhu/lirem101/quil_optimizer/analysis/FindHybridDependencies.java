package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.*;

public class FindHybridDependencies {

    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencyList = new ArrayList<>();
    private boolean calculated = false;

    public FindHybridDependencies(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions = instructions;
    }

    /**
     * Saves hybrid Nodes in hybrid Dependencies.
     */
    private void findHybridNodes() {
        for (ArrayList<InstructionNode> instructionList : instructions) {
            LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
            Set<Integer> handledLines = new HashSet<>();
            ArrayList<InstructionNode> hybridNodes = (ArrayList<InstructionNode>) instructionList.stream()
                    .filter(instruction -> instruction.getLineType() != LineType.CLASSICAL && instruction.getLineType() != LineType.QUANTUM)
                    .collect(java.util.stream.Collectors.toList());
            for (InstructionNode hybridNode : hybridNodes) {
                int line = hybridNode.getCodelines().get(0);
                Set<InstructionNode> dep = hybridNode.getDependencies();
                Set<Integer> dependentLines = dep.stream()
                        .flatMap(x -> x.getCodelines().stream())
                        .filter(x -> !handledLines.contains(x))
                        .collect(java.util.stream.Collectors.toCollection(HashSet::new));
                handledLines.addAll(dependentLines);
                hybridDependencies.put(line, dependentLines);
            }

            hybridDependencyList.add(hybridDependencies);
        }
    }

    public ArrayList<LinkedHashMap<Integer, Set<Integer>>> getHybridDependencies() {
        if (!calculated) {
            findHybridNodes();
            calculated = true;
        }
        return hybridDependencyList;
    }

    /**
     * Add information about hybrid dependencies into JsonArrayBuilder and return it.
     * @return The JsonArrayBuilder with the applied information.
     */
    public JsonArrayBuilder addDeadVariablesToJson() {
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
        return hybridDependencies;
    }
}
