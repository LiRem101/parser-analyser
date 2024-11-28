package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.sound.sampled.Line;
import java.util.*;

public class JITQuantumExecuter {

    LinkedHashMap<Integer, Set<Integer>> hybridDependencies;
    ArrayList<InstructionNode> instructions;
    boolean calculated = false;
    boolean orderRemainedEqual = false;

    public JITQuantumExecuter(LinkedHashMap<Integer, Set<Integer>> hybridDependencies, ArrayList<InstructionNode> instructions) {
        this.hybridDependencies = hybridDependencies;
        this.instructions = instructions;
    }

    public ArrayList<InstructionNode> reorderInstructions() {
        if(hybridDependencies.isEmpty()) {
            return new ArrayList<>();
        }
        if(!calculated) {
            reOrderAllInstructions();
            calculated = true;
        }

        if(orderRemainedEqual) {
            return new ArrayList<>();
        }

        return this.instructions;
    }

    private void reOrderAllInstructions() {
        Set<Integer> firstDependencies = hybridDependencies.values().stream().findFirst().orElse(null);
        if (firstDependencies == null) {
            return;
        }
        int firstHybridLine = hybridDependencies.keySet().stream()
                .filter(x -> hybridDependencies.get(x).equals(firstDependencies))
                .findFirst()
                .orElse(-1);
        if (firstHybridLine == -1) {
            return;
        }
        ArrayList<InstructionNode> executableClassicalInstructions = executableClassicalInstrutionsWithoutQuantum();

        reOrder(executableClassicalInstructions, firstHybridLine);

        orderRemainedEqual = executableClassicalInstructions.equals(instructions);
        instructions = executableClassicalInstructions;
    }

    private void reOrder(ArrayList<InstructionNode> executableClassicalInstructions, int firstHybridLine) {
        InstructionNode hybridInstruction = instructions.stream()
                .filter(x -> x.getLine() == firstHybridLine)
                .findFirst()
                .orElse(null);
        if(hybridInstruction == null) {
            return;
        }
        Set<InstructionNode> necessaryNodes = hybridInstruction.getDependencies();
        ArrayList<InstructionNode> necessaryNodesList = instructions.stream()
                .filter(necessaryNodes::contains)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        executableClassicalInstructions.addAll(necessaryNodesList.stream()
                .filter(x -> x.getLineType() == LineType.CLASSICAL)
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        executableClassicalInstructions.addAll(necessaryNodesList.stream()
                .filter(x -> x.getLineType() != LineType.CLASSICAL)
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        executableClassicalInstructions.add(hybridInstruction);

        executableClassicalInstructions.addAll(instructions.stream()
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    private ArrayList<InstructionNode> executableClassicalInstrutionsWithoutQuantum() {
        ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructions)));
        return eie.getExecutableInstructionsOfOneType(0, LineType.CLASSICAL, new ArrayList<>());
    }
}
