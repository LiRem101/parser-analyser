import de.hhu.lirem101.quil_analyser.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

import static de.hhu.lirem101.quil_optimizer.OptimizingQuil.fuzzOptimization;

public class Main {
    public static final String resourcePath = System.getProperty("user.dir") + "/src/main/resources/";

    private static ParseTree getParseTree(String grammarFileName, String quilFileName) throws FileNotFoundException, CompilationException, IllegalWorkflowException, ParsingException {
        File grammarFile = new File(grammarFileName);
        GenericParser gp = new GenericParser(grammarFile);
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

    private static void optimizeQuil(ParseTree pt, String quilFileName, String resultFileName, Set<String> readoutParams, int iterations, int numberOfOptimizations) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        ClassifyLines cl = new ClassifyLines(pt.getRoot());
        Map<Integer, LineType> classes = cl.classifyLines();
        ControlFlowBlock blocks = getControlFlow(pt, classes);

        String[] quilCode = FileUtils.loadFileContent(quilFileName).split("\n");
        fuzzOptimization(resultFileName, iterations, numberOfOptimizations, blocks, classes, pt.getRoot(), readoutParams, quilCode);
    }

    public static void main(String[] args) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        if(args.length == 0 || !args[0].endsWith(".quil")){
            System.err.println("Quil filename to process needed. Stop.");
            return;
        }

        int iterations = 500;
        int numbersOfOptimizations = 50;

        boolean cfg = Arrays.asList(args).contains("-cfg");
        boolean ddg = Arrays.asList(args).contains("-ddg");
        boolean optimize = Arrays.asList(args).contains("-optimize");
        boolean manIterations = Arrays.asList(args).contains("-iterations");
        boolean manNumbersOfOptimizations = Arrays.asList(args).contains("-nOptimizations");
        if(manIterations){
            int indexManIts = ArrayUtils.indexOf(args, "-iterations");
            try {
                iterations = Integer.parseInt(args[indexManIts+1]);
            } catch(Exception e) {
                System.err.println("No valid iteration number given.");
                return;
            }
        }

        if(manNumbersOfOptimizations){
            int indexNOpts = ArrayUtils.indexOf(args, "-nOptimizations");
            try {
                numbersOfOptimizations = Integer.parseInt(args[indexNOpts+1]);
            } catch(Exception e) {
                System.err.println("No valid optimization number given.");
                return;
            }
        }

        String file = args[0];
        file = StringUtils.removeEnd(file, ".quil");

        String grammarFileName = resourcePath + "Quil.g4";
        String quilFileName = file + ".quil";
        String dotFileName = file + "cfg.dot";
        String graphImageFileName = file + "cfg.ps";
        String dotFileNameDDG = file + "ddg.dot";
        String graphImageFileNameDDG = file + "ddg.ps";
        String resultFileName = file + "_optimization_fuzzing.json";

        ParseTree pt = getParseTree(grammarFileName, quilFileName);

        if(cfg) {
            System.out.println("Starting CFG creation...");
            drawQuilCfg(pt, quilFileName, dotFileName, graphImageFileName);
        }
        if(ddg){
            System.out.println("Starting DDG creation...");
            drawDataDependencyGraph(pt, quilFileName, dotFileNameDDG, graphImageFileNameDDG);
        }
        if(optimize){
            System.out.println("Starting optimization...");
            int indexOptimize = ArrayUtils.indexOf(args, "-optimize");
            Set<String> readoutParams = new HashSet<>();
            for(int i = indexOptimize + 1; i < args.length; i++) {
                String current = args[i];
                if(current.equals("-iterations") || current.equals("-nOptimizations")) {
                    break;
                }
                readoutParams.add(current);
            }
            optimizeQuil(pt, quilFileName, resultFileName, readoutParams, iterations, numbersOfOptimizations);
        }
    }
}
