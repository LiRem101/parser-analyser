package de.hhu.lirem101.quil_analyser;

import java.util.ArrayList;

public interface DirectedGraphNode<T extends DirectedGraphNode> {

    public ArrayList<T> getBranches();

    public ArrayList<Integer> getCodelines();

    public String getName();

    public LineType getLineType();

}
