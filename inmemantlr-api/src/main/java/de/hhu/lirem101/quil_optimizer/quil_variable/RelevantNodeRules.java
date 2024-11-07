package de.hhu.lirem101.quil_optimizer.quil_variable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RelevantNodeRules {
    // Gates that help deciding on the usage type of a variable
    public static Set<String> gateNames = new HashSet<>(Arrays.asList("NEG", "NOT", "TRUE", "FALSE", "logicalBinaryOp",
            "arithmeticBinaryOp", "move", "exchange", "convert", "classicalComparison"));
}
