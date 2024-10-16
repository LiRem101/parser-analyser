import de.hhu.lirem101.quil_analyser.*;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Main {
    public static final String resourcePath = System.getProperty("user.dir") + "/inmemantlr-api/src/main/resources/";

    private static ParseTree getParseTree(String grammarFileName, String quilFileName) throws FileNotFoundException, CompilationException, IllegalWorkflowException, ParsingException {
        File grammarFile = new File(grammarFileName);
        GenericParser gp = new GenericParser(grammarFile);
        // 2. load file content into string
        String s = FileUtils.loadFileContent(quilFileName);
        // 3. set listener for checking parse tree elements. Here you could use any ParseTreeListener implementation. The default listener is used per default
        // this listener will create a parse tree from the java file
        DefaultTreeListener dlist = new DefaultTreeListener();
        gp.setListener(dlist);
        // 4. compile Lexer and parser in-memory
        gp.compile();
        // 5. parse the string
        gp.parse(s, null, GenericParser.CaseSensitiveType.NONE);

        return dlist.getParseTree();
    }

    private static ControlFlowBlock getControlFlow(ParseTree pt, Map<Integer, LineType> classes) {
        OneLevelCodeBlock codeBlock = new OneLevelCodeBlock(pt.getRoot());
        ControlFlowCreator cfc = new ControlFlowCreator(codeBlock);
        ControlFlowBlock cfb = cfc.createControlFlowBlock();
        SplitterQuantumClassical sqc = new SplitterQuantumClassical(cfb, classes);
        ControlFlowBlock blocks = sqc.getNewNode();
        return blocks;
    }

    private static Set<DirectedGraphNode> getDirectedGraph(ControlFlowBlock blocks, ArrayList<LineParameter> lines) {
        ControlFlowRanker cfr = new ControlFlowRanker(blocks, lines);
        ArrayList<ControlFlowBlock> rankedBlocks = cfr.getRankedBlocks();
        ArrayList<Integer> startBlockIndizes = cfr.getIndizesOfStartBlocks();
        DataDependencyGraphCreator ddgc = new DataDependencyGraphCreator(rankedBlocks, startBlockIndizes, lines);
        ArrayList<LineParameter> dataDependencyGraph = ddgc.getDataDependencyGraph();
        Set<DirectedGraphNode> dataDependencySet = new HashSet<>(dataDependencyGraph);
        return dataDependencySet;
    }

    private static void drawDataDependencyGraph(ParseTree pt, String quilFileName, String dotFileName, String graphImageFileName) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        ClassifyLines cl = new ClassifyLines(pt.getRoot());
        Map<Integer, LineType> classes = cl.classifyLines();
        ControlFlowBlock blocks = getControlFlow(pt, classes);

        LineParameterDeterminer lpd = new LineParameterDeterminer(pt, cl);
        ArrayList<LineParameter> lines = lpd.getLineParameters();
        Set<DirectedGraphNode> dataDependencySet = getDirectedGraph(blocks, lines);

        ControlFlowDrawer cfd = new ControlFlowDrawer(dataDependencySet, classes);
        File dotFile = new File(dotFileName);
        File graphic = new File(graphImageFileName);
        cfd.drawControlFlowGraph(graphic, dotFile, quilFileName);
    }

    private static void drawQuilCfg(ParseTree pt, String quilFileName, String dotFileName, String graphImageFileName) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        ClassifyLines cl = new ClassifyLines(pt.getRoot());
        Map<Integer, LineType> classes = cl.classifyLines();
        ControlFlowBlock blocks = getControlFlow(pt, classes);

        ControlFlowDrawer cfd = new ControlFlowDrawer(blocks, classes);
        File dotFile = new File(dotFileName);
        File graphic = new File(graphImageFileName);
        cfd.drawControlFlowGraph(graphic, dotFile, quilFileName);
    }

    public static void main(String[] args) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        System.out.println(System.getProperty("user.dir"));

        final String file = "repeat-until-success";
        String grammarFileName = resourcePath + "Quil.g4";
        String quilFileName = resourcePath + "Quil/" + file + ".quil";
        String dotFileName = resourcePath + "Quil/" + file + ".dot";
        String graphImageFileName = resourcePath + "Quil/" + file + ".ps";
        String dotFileNameDDG = resourcePath + "Quil/" + file + "ddg.dot";
        String graphImageFileNameDDG = resourcePath + "Quil/" + file + "ddg.ps";

        ParseTree pt = getParseTree(grammarFileName, quilFileName);

        // drawQuilCfg(pt, quilFileName, dotFileName, graphImageFileName);
        drawDataDependencyGraph(pt, quilFileName, dotFileNameDDG, graphImageFileNameDDG);

    }
}
