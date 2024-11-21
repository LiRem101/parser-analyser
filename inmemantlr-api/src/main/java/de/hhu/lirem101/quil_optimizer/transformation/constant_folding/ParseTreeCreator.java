package de.hhu.lirem101.quil_optimizer.transformation.constant_folding;

import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;

import static org.stringtemplate.v4.compiler.STLexer.str;

public class ParseTreeCreator {

    /**
     * Create a ParseTree node to MOVE the value to the variable.
     * @param variable The variable to move the value to.
     * @param value The value to move.
     * @param pt The ParseTree node to replace.
     * @return The ParseTree node.
     */
    public static ParseTreeNode createMoveInstruction(ClassicalVariable variable, int value, ParseTreeNode pt) {
        ParseTree tree = new ParseTree("MOVE", "MOVE");
        String label = "MOVE" + variable + str(value);
        ParseTreeNode node = tree.newNode(pt.getParent(), "classicalBinary", label, pt.getSidx(), pt.getEidx(), pt.getLine(), pt.getCharPositionInLine());
        ParseTreeNode moveNode = tree.newNode(node, "move", label, pt.getSidx(), pt.getEidx(), pt.getLine(), pt.getCharPositionInLine());
        node.addChild(moveNode);
        ParseTreeNode paramNode = tree.newNode(moveNode, "addr", variable.getName(), pt.getSidx(), pt.getEidx(), pt.getLine(), 5);
        moveNode.addChild(paramNode);
        ParseTreeNode valueNode = tree.newNode(moveNode, "number", Integer.toString(value), pt.getSidx(), pt.getEidx(), pt.getLine(), 5 + variable.getName().length());
        moveNode.addChild(valueNode);
        ParseTreeNode realNNode = tree.newNode(valueNode, "realN", Integer.toString(value), pt.getSidx(), pt.getEidx(), pt.getLine(), valueNode.getCharPositionInLine());
        valueNode.addChild(realNNode);
        return node;
    }

}
