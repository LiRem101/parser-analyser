package de.hhu.lirem101.quil_optimizer.transformation.constant_folding;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.numbers.complex.Complex;
import org.snt.inmemantlr.tree.ParseTreeNode;

import static de.hhu.lirem101.quil_optimizer.transformation.constant_folding.ParseTreeCreator.createMoveInstruction;

public class HybridNodeHandler implements Handler {

    private final InstructionNode instruction;
    private boolean calculated = false;

    public HybridNodeHandler(InstructionNode instruction) {
        LineType t = instruction.getLineType();
        if (instruction.getLineType() != LineType.CLASSICAL_INFLUENCES_QUANTUM && instruction.getLineType() != LineType.QUANTUM_INFLUENCES_CLASSICAL) {
            throw new IllegalArgumentException("Instruction is not a hybrid instruction.");
        }
        this.instruction = instruction;
    }

    /**
     * Propagate constant values through the instruction. If a parametrize gate has a constant classical parameter,
     * exchange the classical parameter with the constant value. Remove dependency of the classical parameter in the
     * instruction and make it a quantum instruction.
     * If the instruction is a measurement, propagation can only happen if the qubit is in the Z basis. If that is the
     * case, the measurement is replaced by a classical MOVE instruction.
     * @return True if the instruction was changed, false otherwise.
     */
    @Override
    public boolean propagateConstant() {
        if(calculated) {
            return true;
        } else if(instruction.getLineType() == LineType.CLASSICAL_INFLUENCES_QUANTUM) {
            calculated = true;
            return propagateParametrizedGate();
        } else {
            calculated = true;
            return propagateMeasurement();
        }
    }

    /**
     * For measurements, propagation can only happen if the qubit is in the Z basis. If that is the case, the
     * measurement is replaced by a classical MOVE instruction.
     * @return True if the measurement was changed, false otherwise.
     */
    private boolean propagateMeasurement() {
        QuantumVariable qubit = instruction.getQuantumParameters().get(0);
        QuantumCliffordState state = qubit.getCliffordStateBeforeGate();
        if(state != QuantumCliffordState.Z_NEGATIVE && state != QuantumCliffordState.Z_POSITIVE) {
            return false;
        }
        int result = state == QuantumCliffordState.Z_NEGATIVE ? 1 : 0;
        ParseTreeNode pt = createMoveInstruction(instruction.getClassicalParameters().get(0), result, instruction.getParseTreeNode());
        instruction.setParseTreeNode(pt);
        instruction.removeQuantumConnection(qubit);
        instruction.setLineType(LineType.CLASSICAL);
        String newText = "MOVE " + instruction.getClassicalParameters().get(0).getName() + " " + result;
        instruction.setLineText(newText);
        ClassicalVariable cv = instruction.getClassicalParameters().get(0);
        cv.setValue(result);
        return true;
    }

    /**
     * For parametrized gates, exchange the classical parameter with the constant value. Remove dependency of the
     * classical parameter in the instruction and make it a quantum instruction.
     */
    private boolean propagateParametrizedGate() {
        ClassicalVariable classicalParameter = instruction.getClassicalParameters().get(0);
        if (!classicalParameter.isConstant()) {
            return false;
        }
        Complex value = classicalParameter.getValue();
        if(Double.compare(value.imag(), 0.0) > 0.000001) {
            throw new NotImplementedException("Complex numbers are not supported for parametrized gates.");
        }
        double realValue = value.real();
        String variableString = classicalParameter.getName();
        String text = instruction.getLineText().replace(variableString, Double.toString(realValue));
        instruction.removeClassicalConnection(classicalParameter);
        instruction.setLineType(LineType.QUANTUM);
        instruction.setLineText(text);

        return true;
    }

}
