/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-FileCopyrightText: 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-License-Identifier: MIT
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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static de.hhu.lirem101.quil_optimizer.OptimizingQuil.fuzzOptimization;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private JsonObjectBuilder doOptimization(ArrayList<String> optimizationSteps) throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        final String file = "iterative-phase-estimation";
        return doOptimizationOnFile(optimizationSteps, file);
    }

    private JsonObjectBuilder doOptimizationOnFile(ArrayList<String> optimizationSteps, String filename) throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        Set<String> readoutParams = new HashSet<>();
        readoutParams.add("result[0]");

        String grammarFileName = resourcePath + "Quil.g4";
        String quilFileName = resourcePath + "Quil/" + filename + ".quil";

        ParseTree pt = getParseTree(grammarFileName, quilFileName);
        ClassifyLines cl = new ClassifyLines(pt.getRoot());
        Map<Integer, LineType> classes = cl.classifyLines();
        ControlFlowBlock blocks = getControlFlow(pt, classes);

        String[] quilCode = FileUtils.loadFileContent(quilFileName).split("\n");
        OptimizingQuil oQuil = new OptimizingQuil(blocks, classes, pt.getRoot(), readoutParams, quilCode);
        return oQuil.applyOptimizationSteps(optimizationSteps);
    }

    private void doMultipleOptimization(ArrayList<ArrayList<String>> optimizationSteps) throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
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
        fuzzOptimization(optimizationSteps, blocks, classes, pt.getRoot(), readoutParams, quilCode);
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

    @Test
    public void optimize9() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("ConstantPropagation", "QuantumJIT",
                "ConstantFolding", "DeadCodeAnalysis", "ConstantPropagation", "LiveVariableAnalysis", "QuantumJIT",
                "DeadCodeAnalysis", "DeadCodeElimination", "HybridDependencies", "HybridDependencies",
                "ConstantFolding", "DeadCodeElimination", "DeadCodeElimination", "LiveVariableAnalysis", "ReOrdering",
                "LiveVariableAnalysis", "DeadCodeAnalysis", "ConstantFolding", "DeadCodeAnalysis"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize10() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("HybridDependencies", "ReOrdering",
                "DeadCodeElimination", "HybridDependencies", "QuantumJIT", "ReOrdering", "ConstantPropagation",
                "DeadCodeElimination", "ReOrdering", "LiveVariableAnalysis", "ReOrdering", "ConstantPropagation",
                "LiveVariableAnalysis", "QuantumJIT", "HybridDependencies", "DeadCodeAnalysis", "DeadCodeAnalysis",
                "QuantumJIT", "DeadCodeAnalysis", "HybridDependencies"));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize11() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList("HybridDependencies",
                "LiveVariableAnalysis",
                "DeadCodeAnalysis",
                "ReOrdering"));
        JsonObjectBuilder result = doOptimization(optimizationSteps);
        JsonObject json = result.build();
        JsonArray finalInstructions = json.getJsonArray("FinalResult").getJsonArray(0);

        assertEquals("1: DECLARE theta REAL[1]", finalInstructions.getString(0));
        assertEquals("17: MOVE theta[0] 0", finalInstructions.getString(1));
        assertEquals("21: CONTROLLED targetGate_even 0 1 2", finalInstructions.getString(3));
        assertEquals(55, finalInstructions.size());
    }

    @Test
    public void optimize12() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList(
                "ConstantPropagation", "ReOrdering", "ConstantFolding"
        ));
        doOptimization(optimizationSteps);
    }

    @Test
    public void optimize13() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList(
                "LiveVariableAnalysis", "DeadCodeAnalysis", "DeadCodeElimination",
                "HybridDependencies", "ReOrdering"
        ));
        JsonObjectBuilder result = doOptimization(optimizationSteps);
        JsonObject json = result.build();
        JsonArray finalInstructions = json.getJsonArray("FinalResult").getJsonArray(0);

        assertEquals("1: DECLARE theta REAL[1]", finalInstructions.getString(0));
        assertEquals("31: RZ(theta[0]) 0", finalInstructions.getString(16));
        assertEquals("32: H 0", finalInstructions.getString(17));
        assertEquals(53, finalInstructions.size());
    }

    @Test
    public void optimize14() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList(
                "LiveVariableAnalysis", "DeadCodeAnalysis", "DeadCodeElimination",
                "ConstantPropagation", "ConstantFolding",
                "HybridDependencies", "ReOrdering",
                "LiveVariableAnalysis", "DeadCodeAnalysis", "DeadCodeElimination",
                "ConstantPropagation", "ConstantFolding",
                "HybridDependencies", "QuantumJIT"
        ));
        JsonObjectBuilder result = doOptimization(optimizationSteps);
        JsonObject json = result.build();
        JsonArray finalInstructions = json.getJsonArray("FinalResult").getJsonArray(0);

        assertEquals("24: MEASURE 0 lastMeasurement[0]", finalInstructions.getString(11));
        assertEquals(51, finalInstructions.size());
    }

    @Test
    public void optimize15() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps = new ArrayList<>(Arrays.asList(
                "HybridDependencies", "ReOrdering"
        ));
        String filename = "magic-state-distillation";
        JsonObjectBuilder result = doOptimizationOnFile(optimizationSteps, filename);
        JsonObject json = result.build();
        JsonArray finalInstructions = json.getJsonArray("FinalResult").getJsonArray(0);

        assertEquals("29: MEASURE 0 ro[0]", finalInstructions.getString(24));
        assertEquals(67, finalInstructions.size());
    }

    @Test
    public void optimizeMultiple1() throws CompilationException, ParsingException, FileNotFoundException, IllegalWorkflowException {
        ArrayList<String> optimizationSteps0 = new ArrayList<>(Arrays.asList("QuantumJIT", "LiveVariableAnalysis",
                "ConstantPropagation", "DeadCodeAnalysis", "ConstantFolding", "LiveVariableAnalysis",
                "DeadCodeElimination", "ConstantPropagation", "LiveVariableAnalysis", "LiveVariableAnalysis",
                "DeadCodeAnalysis", "DeadCodeAnalysis", "LiveVariableAnalysis", "ReOrdering", "ConstantFolding",
                "LiveVariableAnalysis", "ConstantPropagation", "DeadCodeAnalysis", "QuantumJIT", "ConstantFolding"));
        ArrayList<String> optimizationSteps1 = new ArrayList<>(Arrays.asList("ReOrdering", "DeadCodeAnalysis",
                "QuantumJIT", "ReOrdering", "ConstantPropagation", "ConstantFolding", "QuantumJIT",
                "LiveVariableAnalysis", "QuantumJIT", "LiveVariableAnalysis", "QuantumJIT", "DeadCodeElimination",
                "QuantumJIT", "QuantumJIT", "ConstantPropagation", "QuantumJIT", "DeadCodeAnalysis", "DeadCodeAnalysis",
                "ConstantFolding", "ConstantPropagation"));
        ArrayList<ArrayList<String>> optimizations = new ArrayList<>(Arrays.asList(optimizationSteps0, optimizationSteps1));
        doMultipleOptimization(optimizations);
    }

}
