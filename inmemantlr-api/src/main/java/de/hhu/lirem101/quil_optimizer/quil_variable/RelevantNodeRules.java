package de.hhu.lirem101.quil_optimizer.quil_variable;

import java.util.*;

public class RelevantNodeRules {
    // Gates that help deciding on the usage type of a classical variable
    public static HashMap<String, List<ClassicalUsage>> classicalUsage() {
        HashMap<String, List<ClassicalUsage>> gateUsage = new HashMap<>();
        gateUsage.put("memoryDescriptor", Collections.singletonList(ClassicalUsage.DECLARE));
        gateUsage.put("NEG", Collections.singletonList(ClassicalUsage.USAGE_ASSIGNMENT));
        gateUsage.put("NOT", Collections.singletonList(ClassicalUsage.USAGE_ASSIGNMENT));
        gateUsage.put("TRUE", Collections.singletonList(ClassicalUsage.ASSIGNMENT));
        gateUsage.put("FALSE", Collections.singletonList(ClassicalUsage.ASSIGNMENT));
        gateUsage.put("logicalBinaryOp", Arrays.asList(ClassicalUsage.USAGE_ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("arithmeticBinaryOp", Arrays.asList(ClassicalUsage.USAGE_ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("move", Arrays.asList(ClassicalUsage.ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("exchange", Arrays.asList(ClassicalUsage.USAGE_ASSIGNMENT, ClassicalUsage.USAGE_ASSIGNMENT));
        gateUsage.put("convert", Arrays.asList(ClassicalUsage.ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("classicalComparison", Arrays.asList(ClassicalUsage.ASSIGNMENT, ClassicalUsage.USAGE, ClassicalUsage.USAGE));
        return gateUsage;
    }

    public static Set<String> measurementNodes() {
        Set<String> quantumNodes = new HashSet<>();
        quantumNodes.add("measure");
        quantumNodes.add("circuitMeasure");
        return quantumNodes;
    }
}
