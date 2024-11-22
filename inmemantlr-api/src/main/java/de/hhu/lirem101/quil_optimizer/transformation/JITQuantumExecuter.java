package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

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
        findExecutableClassicalAndQuantumInstructions(executableClassicalInstructions);
        executableClassicalInstructions.add(instructions.stream()
                .filter(x -> x.getLine() == firstHybridLine)
                .findFirst()
                .orElse(null));
        ArrayList<InstructionNode> stillMissingInstructions = instructions.stream()
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        executableClassicalInstructions.addAll(stillMissingInstructions);
    }

    private void findExecutableClassicalAndQuantumInstructions(ArrayList<InstructionNode> executableClassicalInstructions) {
        boolean newFoundInstructions = true;
        ArrayList<InstructionNode> newExecutableQuantumInstructions = new ArrayList<>();
        ArrayList<InstructionNode> newExecutableClassicalInstructions = new ArrayList<>();
        while(newFoundInstructions) {
            ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructions)));
            ArrayList<InstructionNode> executedInstructions = new ArrayList<>();
            executedInstructions.addAll(executableClassicalInstructions);
            executedInstructions.addAll(newExecutableClassicalInstructions);
            executedInstructions.addAll(newExecutableQuantumInstructions);
            ArrayList<InstructionNode>  newExecutableInstructions = eie.getExecutableInstructionsOfOneBlock(0, executedInstructions)
                    .stream()
                    .filter(x -> !executedInstructions.contains(x))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            ArrayList<InstructionNode> classicalInstructions = newExecutableInstructions.stream()
                    .filter(x -> x.getLineType() == LineType.CLASSICAL)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            ArrayList<InstructionNode> quantumInstructions = newExecutableInstructions.stream()
                    .filter(x -> x.getLineType() == LineType.QUANTUM)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            newFoundInstructions = !classicalInstructions.isEmpty() || !quantumInstructions.isEmpty();
            newExecutableClassicalInstructions.addAll(classicalInstructions);
            newExecutableQuantumInstructions.addAll(quantumInstructions);
        }

        executableClassicalInstructions.addAll(newExecutableClassicalInstructions);
        executableClassicalInstructions.addAll(newExecutableQuantumInstructions);
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
