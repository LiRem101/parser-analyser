package de.hhu.lirem101.quil_analyser;

import java.util.HashSet;
import java.util.Set;

public class LineParameter implements Comparable<LineParameter> {
    private final int line;
    private final LineType type;
    private final Set<String> quantumParameters = new HashSet<>();
    private final Set<String> classicalParameters = new HashSet<>();

    public LineParameter(int line, LineType type) {
        this.line = line;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LineParameter)) {
            return false;
        }
        LineParameter lp = (LineParameter) obj;
        return lp.line == line;
    }

    @Override
    public int compareTo(LineParameter o) {
        return Integer.compare(line, o.line);
    }

    @Override
    public String toString() {
        return "Line: " + line + ", Type: " + type + ", Quantum Parameters: " + quantumParameters + ", Classical Parameters: " + classicalParameters;
    }

    public void addQuantumParameter(String parameter) {
        quantumParameters.add(parameter);
    }

    public void addClassicalParameter(String parameter) {
        classicalParameters.add(parameter);
    }

    public HashSet<String> getQuantumParameters() {
        return new HashSet<>(quantumParameters);
    }

    public HashSet<String> getClassicalParameters() {
        return new HashSet<>(classicalParameters);
    }

    public int getLineNumber() {
        return line;
    }

    public LineType getLineType() {
        return type;
    }

    public boolean containsQuantumParameter(String parameter) {
        return quantumParameters.contains(parameter);
    }

    public boolean containsClassicalParameter(String parameter) {
        return classicalParameters.contains(parameter);
    }
}
