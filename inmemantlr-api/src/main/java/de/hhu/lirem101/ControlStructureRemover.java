package de.hhu.lirem101;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlStructureRemover {

    /**
     * Removes all non-conditional control structures from the instructions. If we result in an empty block, we add an
     * end instruction to the block. An empty block can only occur in a list that leads to program halt.
     * @param instructionLists The instructions to remove the control structures from.
     * @return List of lists of instructions with removed unconditional control structures.
     */
    public static ArrayList<ArrayList<InstructionNode>> removeControlStructures(ArrayList<ArrayList<InstructionNode>> instructionLists) {
        ArrayList<ArrayList<InstructionNode>> removedInstructions = new ArrayList<>();
        for (ArrayList<InstructionNode> instructionList : instructionLists) {
            ArrayList<InstructionNode> theseRemovedInstructions = instructionList.stream()
                    .filter(instruction -> instruction.getLineType() != LineType.CONTROL_STRUCTURE)
                    .collect(Collectors.toCollection(ArrayList::new));
            removedInstructions.add(theseRemovedInstructions);
        }
        ArrayList<Integer> indizesOfEmptyBlocks = IntStream.range(0, removedInstructions.size())
                .filter(i -> removedInstructions.get(i).isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        for (Integer index : indizesOfEmptyBlocks) {
            int size = instructionLists.get(index).size();
            InstructionNode endInstruction = instructionLists.get(index).get(size-1);
            removedInstructions.get(index).add(endInstruction);
        }
        return removedInstructions;
    }
}
