package de.hhu.lirem101.quil_analyser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the rules for the parse tree.
 */
public class RulesOfParseTree {
    // Gate: Depends on param, just like circuitGate
    // Measure only influences classical if addr is given
    // Param (expression) classical (or rather C -> Q) if it is not exclusively a number
    // defCircuit is a control structure, but its children have to be checked as well

    public static final Set<String> quantum = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("defGate", "qubitVariable", "circuitQubit", "resetState", "circuitResetState")));
    public static final Set<String> classical = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("classicalUnary", "classicalBinary", "classicalComparison", "load", "store",
            "memoryDescriptor")));
    public static final Set<String> quantumInfluClassical = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("measure", "circuitMeasure")));
    public static final Set<String> controlStructure = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("defLabel", "halt", "jump")));
    public static final Set<String> controlStructureClassical = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("jumpWhen", "jumpUnless")));

}
