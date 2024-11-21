package de.hhu.lirem101.quil_optimizer.transformation.constant_folding;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.quantum_gates.QuantumCliffordGate;
import de.hhu.lirem101.quil_optimizer.quantum_gates.QuantumGateMapper;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumCliffordState;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumUsage;
import de.hhu.lirem101.quil_optimizer.quil_variable.QuantumVariable;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.LinkedList;
import java.util.Queue;

public class QuantumHandler implements Handler {
    private final InstructionNode instruction;
    private boolean calculated = false;

    public QuantumHandler(InstructionNode instruction) {
        if(instruction.getLineType() != LineType.QUANTUM) {
            throw new IllegalArgumentException("Instruction is not a quantum instruction.");
        }
        this.instruction = instruction;
    }

    /**
     * Propagate constant values through the quantum instruction if a single quantum gate is applied to a constant
     * value.
     * @return True if a constant value is propagated, false if not.
     */
    @Override
    public boolean propagateConstant() {
        QuantumVariable qv = instruction.getQuantumParameters().stream().findFirst().orElse(new QuantumVariable("", QuantumUsage.MULTI_GATE));
        if(calculated) {
            return true;
        } else if(qv.getUsage() != QuantumUsage.SINGLE_GATE || qv.getCliffordStateBeforeGate() == null || qv.getCliffordStateAfterGate() != null) {
            return false;
        }
        calculated = true;

        applyGate();

        if(qv.getCliffordStateAfterGate() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Applies the gate of the quantum instruction to the constant value.
     */
    private void applyGate() {
        QuantumVariable qv = instruction.getQuantumParameters().stream().findFirst().orElse(null);
        ParseTreeNode pt = instruction.getParseTreeNode();
        String gateName = getGateName(pt);
        if(gateName == null || qv == null) {
            return;
        }
        QuantumCliffordGate gate = QuantumGateMapper.getQuantumGateMap().get(gateName);
        if(gate == null) {
            return;
        }
        qv.applyGate(gate);
    }

    /**
     * Get name of gate from the parse tree node.
     * @param pt The parse tree node.
     * @return The name of the gate.
     */
    private String getGateName(ParseTreeNode pt) {
        Queue<ParseTreeNode> queue = new LinkedList<>();
        queue.add(pt);
        while(!queue.isEmpty()) {
            ParseTreeNode node = queue.poll();
            if(node.getRule().equals("name")) {
                return node.getLabel();
            }
            queue.addAll(node.getChildren());
        }
        return null;
    }

}
