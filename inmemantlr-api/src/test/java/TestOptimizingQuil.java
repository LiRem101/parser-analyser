import de.hhu.lirem101.quil_analyser.*;
import de.hhu.lirem101.quil_optimizer.OptimizingQuil;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TestOptimizingQuil {
    // Check that no errors are thrown for combinations that have thrown errors in the past
    public static final String resourcePath = System.getProperty("user.dir") + "/src/test/resources/";

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

    private void doOptimization(ArrayList<String> optimizationSteps) throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        final String file = "iterative-phase-estimation";
        Set<String> readoutParams = new HashSet<>();
        readoutParams.add("result[0]");

        String grammarFileName = resourcePath + "Quil.g4";
        String quilFileName = resourcePath + "Quil/" + file + ".quil";

        ParseTree pt = getParseTree(grammarFileName, quilFileName);
        ClassifyLines cl = new ClassifyLines(pt.getRoot());
        Map<Integer, LineType> classes = cl.classifyLines();
        ControlFlowBlock blocks = getControlFlow(pt, classes);

        String[] quilCode = FileUtils.loadFileContent(quilFileName).split("\n");
        OptimizingQuil oQuil = new OptimizingQuil(blocks, classes, pt.getRoot(), readoutParams, quilCode);
        oQuil.applyOptimizationSteps(optimizationSteps);
    }

    @Test
    public void optimize1() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("DeadCodeAnalysis", "DeadCodeElimination",
                "DeadCodeElimination", "QuantumJIT", "QuantumJIT", "HybridDependencies", "QuantumJIT",
                "DeadCodeElimination", "LiveVariableAnalysis", "QuantumJIT", "DeadCodeElimination", "ConstantFolding",
                "LiveVariableAnalysis", "DeadCodeElimination", "HybridDependencies", "ConstantPropagation",
                "LiveVariableAnalysis", "ReOrdering", "ConstantFolding", "ConstantPropagation"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize2() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("HybridDependencies",
                "LiveVariableAnalysis", "DeadCodeElimination", "ConstantFolding", "ReOrdering", "QuantumJIT",
                "HybridDependencies", "ConstantFolding", "ReOrdering", "ConstantPropagation", "LiveVariableAnalysis",
                "LiveVariableAnalysis", "ConstantFolding", "QuantumJIT", "ReOrdering", "QuantumJIT", "ReOrdering",
                "ConstantPropagation", "ConstantFolding", "DeadCodeAnalysis"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize3() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("LiveVariableAnalysis", "QuantumJIT",
                "ConstantFolding", "HybridDependencies", "QuantumJIT", "ReOrdering", "HybridDependencies", "QuantumJIT",
                "ConstantFolding", "ConstantPropagation", "LiveVariableAnalysis", "DeadCodeAnalysis",
                "LiveVariableAnalysis", "ConstantFolding", "DeadCodeAnalysis", "ConstantPropagation", "ConstantFolding",
                "LiveVariableAnalysis", "DeadCodeElimination", "QuantumJIT"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize4() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("ConstantPropagation",
                "ConstantPropagation", "ReOrdering", "ConstantFolding", "QuantumJIT", "DeadCodeAnalysis",
                "DeadCodeElimination", "QuantumJIT", "QuantumJIT", "ReOrdering", "ConstantPropagation",
                "ConstantPropagation", "DeadCodeElimination", "QuantumJIT", "ReOrdering", "ReOrdering",
                "HybridDependencies", "LiveVariableAnalysis", "ConstantPropagation", "QuantumJIT"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize5() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("HybridDependencies", "QuantumJIT",
                "ReOrdering", "LiveVariableAnalysis", "HybridDependencies", "HybridDependencies",
                "LiveVariableAnalysis", "QuantumJIT", "DeadCodeElimination", "ConstantFolding", "ReOrdering",
                "ConstantFolding", "QuantumJIT", "ReOrdering", "QuantumJIT", "QuantumJIT", "ConstantFolding",
                "ReOrdering", "ConstantPropagation", "DeadCodeAnalysis"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize6() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("QuantumJIT", "QuantumJIT", "ReOrdering",
                "ConstantPropagation", "HybridDependencies", "QuantumJIT", "QuantumJIT", "ReOrdering", "QuantumJIT",
                "ConstantPropagation", "ConstantFolding", "DeadCodeAnalysis", "HybridDependencies",
                "HybridDependencies", "DeadCodeElimination", "DeadCodeAnalysis", "ConstantFolding", "QuantumJIT",
                "HybridDependencies", "QuantumJIT"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize7() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("DeadCodeAnalysis", "QuantumJIT",
                "ReOrdering", "HybridDependencies", "QuantumJIT", "DeadCodeAnalysis", "QuantumJIT", "DeadCodeAnalysis",
                "ConstantPropagation", "DeadCodeElimination", "ConstantPropagation", "DeadCodeElimination",
                "DeadCodeAnalysis", "ConstantFolding", "HybridDependencies", "LiveVariableAnalysis", "QuantumJIT",
                "HybridDependencies", "DeadCodeAnalysis", "DeadCodeAnalysis"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize8() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("DeadCodeAnalysis", "ReOrdering",
                "ConstantPropagation", "DeadCodeElimination", "ConstantPropagation", "QuantumJIT", "ConstantFolding",
                "QuantumJIT", "QuantumJIT", "QuantumJIT", "QuantumJIT", "LiveVariableAnalysis", "LiveVariableAnalysis",
                "ConstantPropagation", "LiveVariableAnalysis", "ConstantPropagation", "QuantumJIT", "DeadCodeAnalysis",
                "DeadCodeElimination", "QuantumJIT"));
        doOptimization(optimizationSteps);
    }

}
