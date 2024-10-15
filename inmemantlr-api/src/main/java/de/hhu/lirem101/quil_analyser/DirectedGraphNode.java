package de.hhu.lirem101.quil_analyser;

import java.util.ArrayList;

public interface DirectedGraphNode<T> {

    public ArrayList<T> getBranches();

    public ArrayList<Integer> getCodelines();

    public String getName();

}
