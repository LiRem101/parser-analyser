package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

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
        int firstHybridLine = hybridDependencies.keySet().stream().findFirst().orElse(-1);
        if (firstHybridLine == -1) {
            return;
        }
        ArrayList<InstructionNode> executableClassicalInstructions = executableClassicalInstrutionsWithoutQuantum();
        reOrder(executableClassicalInstructions, firstHybridLine);

        orderRemainedEqual = executableClassicalInstructions.equals(instructions);
        instructions = executableClassicalInstructions;
    }

    private void reOrder(ArrayList<InstructionNode> executableClassicalInstructions, int firstHybridLine) {
        ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructions)));
        ArrayList<InstructionNode> newExecutableInstructions = eie.getExecutableInstructionsOfOneBlock(0, executableClassicalInstructions)
                .stream()
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        ArrayList<InstructionNode> quantumInstructions = newExecutableInstructions.stream()
                .filter(x -> x.getLineType() == LineType.QUANTUM)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        ArrayList<InstructionNode> classicalInstructions = newExecutableInstructions.stream()
                .filter(x -> x.getLineType() == LineType.CLASSICAL)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        executableClassicalInstructions.addAll(classicalInstructions);
        executableClassicalInstructions.addAll(quantumInstructions);
        executableClassicalInstructions.add(instructions.stream()
                .filter(x -> x.getLine() == firstHybridLine)
                .findFirst()
                .orElse(null));
        ArrayList<InstructionNode> stillMissingInstructions = instructions.stream()
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        executableClassicalInstructions.addAll(stillMissingInstructions);
    }

    private ArrayList<InstructionNode> executableClassicalInstrutionsWithoutQuantum() {
        ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructions)));
        ArrayList<InstructionNode> executableClassicalInstructions = eie.getExecutableInstructionsOfOneBlock(0, new ArrayList<>())
                .stream()
                .filter(x -> x.getLineType() == LineType.CLASSICAL)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        boolean newInstructions = !executableClassicalInstructions.isEmpty();
        while(newInstructions) {
            ExecutableInstructionsExtractor nextEie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(executableClassicalInstructions)));
            ArrayList<InstructionNode> nextExecutableClassicalInstructions = nextEie.getExecutableInstructionsOfOneBlock(0, new ArrayList<>())
                    .stream()
                    .filter(x -> x.getLineType() == LineType.CLASSICAL && !executableClassicalInstructions.contains(x))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            newInstructions = !nextExecutableClassicalInstructions.isEmpty();
            executableClassicalInstructions.addAll(nextExecutableClassicalInstructions);
        }
        return executableClassicalInstructions;
    }
}
