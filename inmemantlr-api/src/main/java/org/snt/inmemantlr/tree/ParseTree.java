/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package org.snt.inmemantlr.tree;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * an abstract syntax tree
 */
public class ParseTree {

    private ParseTreeNode root = null;
    List<ParseTreeNode> nodes = null;

    private ParseTree() {
        nodes = new Vector<>();
    }

    /**
     * constructor
     *
     * create a new abstract syntax tree
     *
     * @param nt    name of root non-terminal node
     * @param label value of root non-terminal node
     */
    public ParseTree(String nt, String label) {
        this();
        root = newNode(null, nt, label,0,0);
    }

    /**
     * constructor
     *
     * copy constructor
     *
     * @param tree tree to be duplicated
     */
    public ParseTree(ParseTree tree) {
        this();
        root = newNode(tree.getRoot());
    }

    /**
     * constructor
     *
     *
     * @param nod root node
     */
    private ParseTree(ParseTreeNode nod) {
        this();
        root = newNode(nod);
    }

    /**
     * get root node
     *
     * @return root node
     */
    public ParseTreeNode getRoot() {
        return root;
    }

    /**
     * create new ast node
     *
     * @param parent root of new ast node to be created
     * @return newly created ast node
     */
    private ParseTreeNode newNode(ParseTreeNode parent) {
        ParseTreeNode rn = new ParseTreeNode(this, parent);
        nodes.add(rn);
        return rn;
    }

    /**
     * create new ast node
     *
     * @param parent parent node
     * @param nt     name of node to be created
     * @param label  value of node to be created
     * @param sidx   start index
     * @param eidx   end index
     * @return newly created node
     */
    public ParseTreeNode newNode(ParseTreeNode parent, String nt, String label, int sidx,
                                 int eidx) {
        ParseTreeNode rn = new ParseTreeNode(this, parent, nt, label, sidx, eidx);
        nodes.add(rn);
        return rn;
    }

    /**
     * get leaf nodes
     *
     * @return set of leaf nodes
     */
    public Set<ParseTreeNode> getLeafs() {
        return nodes.stream().filter(n -> !n.hasChildren()).collect
                (Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * get all nodes
     *
     * @return list of ast nodes
     */
    public List<ParseTreeNode> getNodes() {
        return nodes;
    }

    /**
     * generate dot representation from ast
     *
     * @return dot format string
     */
    public String toDot() {
        return ParseTreeSerializer.INSTANCE.toDot(this);
    }

    /**
     * generate JSON representation from ast
     *
     * @return JSON format string
     */
    public String toJson() {
        return ParseTreeSerializer.INSTANCE.toJson(this);
    }

    /**
     * generate XML representation from ast
     *
     * @return XML format string
     */
    public String toXml() {
        return ParseTreeSerializer.INSTANCE.toXml(this);
    }


    /**
     * replace oldTree by newTree
     *
     * @param oldTree tree to be replaced
     * @param newTree tree replacement
     * @return true when subtree replacement was successful, false otherwise
     */
    public boolean replaceSubtree(ParseTree oldTree, ParseTree newTree) {
        if (hasSubtree(oldTree)) {
            nodes.stream()
                    .filter(n -> oldTree.getRoot().equals(n))
                    .forEach(n -> n.getParent().replaceChild(oldTree.getRoot(), newTree.getRoot()));
            nodes.addAll(newTree.nodes);
            return nodes.removeAll(oldTree.nodes);
        }
        return false;
    }

    /**
     * remove subtree from ast
     *
     * @param subtree to be removed
     * @return true when removal was succesful, false otherwise
     */
    public boolean removeSubtree(ParseTree subtree) {
        if (hasSubtree(subtree)) {
            nodes.stream()
                    .filter(n -> subtree.getRoot().equals(n))
                    .forEach(n -> n.getParent().delChild(n));
            return nodes.removeAll(subtree.nodes);
        }
        return false;
    }

    /**
     * find dominant subtrees, i.e., subtrees where the distance of the subtree root
     * node to the ast root node is minimal.
     *
     * @param p predicate to search for the dominating subtree root node
     * @return set of dominating subtrees
     */
    public Set<ParseTree> getDominatingSubtrees(Predicate<ParseTreeNode> p) {
        Set<ParseTreeNode> selected = new HashSet<>();
        searchDominatingNodes(root, selected, p);
        return getSubtrees(selected::contains);
    }

    /**
     * helper method for finding the dominating subtrees
     *
     * @param n        current root
     * @param selected set to keep track of visited nodes
     * @param p        predicate to search for the dominating subtree root node
     */
    private void searchDominatingNodes(ParseTreeNode n, Set<ParseTreeNode> selected, Predicate<ParseTreeNode> p) {
        if (p.test(n)) {
            selected.add(n);
        } else {
            n.getChildren().forEach(an -> searchDominatingNodes(an, selected, p));
        }
    }

    /**
     * get subtree with the root node identified by p
     *
     * @param p predicate for identifying the root node
     * @return set of ast nodes
     */
    public Set<ParseTree> getSubtrees(Predicate<ParseTreeNode> p) {
        return nodes.stream().filter(p).map(ParseTree::new).collect(toSet());
    }

    /**
     * check the presence of subree in the current tree
     *
     * @param subtree tree whose presence is checked
     * @return true if subtree is present in actual one, false otherwise
     */
    public boolean hasSubtree(ParseTree subtree) {
        Set<ParseTree> subtrees = getSubtrees(n -> subtree.getRoot().equals(n));
        return subtrees.stream().filter(subtree::equals).count() > 0;
    }

    /**
     * get subtree
     *
     * @param subtree tree whose presence is checked
     * @return subree
     */
    public ParseTree getSubtree(ParseTree subtree) {
        Set<ParseTree> subtrees = getSubtrees(n -> n.equals(subtree.getRoot()));
        return subtrees.stream().filter(subtree::equals).findFirst().orElse(null);
    }

    @Override
    public int hashCode() {
        return root.getId();
    }


    /**
     * recursive helper function for sorting the tree topologically
     * @param ns in/out list to keep sorted nodes
     * @param n node to consider
     */
    private void topoSortRec(List<ParseTreeNode> ns, ParseTreeNode n) {

        ns.add(n);

        for(ParseTreeNode cc : n.getChildren()) {
            topoSortRec(ns, cc);
        }

    }

    public void topoSort() {
        List<ParseTreeNode> nods = new Vector<>();
        topoSortRec(nods, this.getRoot());
        assert nods.size() == this.nodes.size();
        this.nodes = nods;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParseTree))
            return false;

        ParseTree parseTree = (ParseTree) o;
        // will recursively check AST nodes
        return root.equals(parseTree.getRoot());
    }
}
