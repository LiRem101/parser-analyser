package de.hhu.lirem101.quil_analyser;

import java.util.*;

public class LineParameter implements Comparable<LineParameter>, DirectedGraphNode {
    private final int line;
    private final LineType type;
    private final Map<String, ArrayList<LineParameter>> quantumParameters = new HashMap<>();
    private final Map<String, ArrayList<LineParameter>> classicalParameters = new HashMap<>();

    public LineParameter(int line, LineType type) {
        this.line = line;
        this.type = type;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) {
//            return true;
//        }
//        if (!(obj instanceof LineParameter)) {
//            return false;
//        }
//        LineParameter lp = (LineParameter) obj;
//        return lp.line == line;
//    }

    @Override
    public int compareTo(LineParameter o) {
        return Integer.compare(line, o.line);
    }

    @Override
    public String toString() {
        return "Line: " + line + ", Type: " + type + ", Quantum Parameters: " + quantumParameters + ", Classical Parameters: " + classicalParameters;
    }

    public void addQuantumParameter(String parameter) {
        quantumParameters.put(parameter, new ArrayList<>());
    }

    public void addClassicalParameter(String parameter) {
        classicalParameters.put(parameter, new ArrayList<>());
    }

    public HashSet<String> getQuantumParameters() {
        return new HashSet<>(quantumParameters.keySet());
    }

    public HashSet<String> getClassicalParameters() {
        return new HashSet<>(classicalParameters.keySet());
    }

    public HashSet<String> getParameters() {
        HashSet<String> set = new HashSet<>(classicalParameters.keySet());
        set.addAll(quantumParameters.keySet());
        return set;
    }

    public int getLineNumber() {
        return line;
    }

    public LineType getLineType() {
        return type;
    }

    public void addExecuteBeforeLine(String parameter, LineParameter lp) {
        if (quantumParameters.containsKey(parameter)) {
            ArrayList<LineParameter> list = quantumParameters.get(parameter);
            list.add(lp);
        } else if (classicalParameters.containsKey(parameter)) {
            ArrayList<LineParameter> list = classicalParameters.get(parameter);
            list.add(lp);
        } else {
            throw new IllegalArgumentException("Parameter " + parameter + " not found in line " + line);
        }
    }

    public HashSet<LineParameter> getExecuteBeforeLines() {
        HashSet<LineParameter> list = new HashSet<>();
        for (ArrayList<LineParameter> lps : quantumParameters.values()) {
            list.addAll(lps);
        }
        for (ArrayList<LineParameter> lps : classicalParameters.values()) {
            list.addAll(lps);
        }
        return list;
    }

    public boolean containsQuantumParameter(String parameter) {
        return quantumParameters.containsKey(parameter);
    }

    public boolean containsClassicalParameter(String parameter) {
        return classicalParameters.containsKey(parameter);
    }

    public LineParameter copyLineParameter() {
        LineParameter copy = new LineParameter(line, type);
        for (String key : quantumParameters.keySet()) {
            copy.addQuantumParameter(key);
        }
        for (String key : classicalParameters.keySet()) {
            copy.addClassicalParameter(key);
        }
        return copy;
    }


    @Override
    public ArrayList<LineParameter> getBranches() {
        ArrayList<LineParameter> list = new ArrayList<>();
        for (ArrayList<LineParameter> lps : quantumParameters.values()) {
            list.addAll(lps);
        }
        for (ArrayList<LineParameter> lps : classicalParameters.values()) {
            list.addAll(lps);
        }
        return list;
    }

    @Override
    public ArrayList<Integer> getCodelines() {
        return new ArrayList<>(Collections.singletonList(line));
    }

    @Override
    public String getName() {
        return "Line" + line;
    }
}
