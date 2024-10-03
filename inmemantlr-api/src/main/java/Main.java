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
import java.util.Map;

public class Main {
    public static final String resourcePath = System.getProperty("user.dir") + "/inmemantlr-api/src/main/resources/";

    private static void drawQuilCfg(String grammarFileName, String quilFileName, String graphImageFileName) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
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

        ParseTree pt = dlist.getParseTree();

//        String dot = pt.toDot();
//
//        Files.write(new File(quilFileName + ".dot").toPath(), dot.getBytes());

        ClassifyLines cl = new ClassifyLines(pt.getRoot());
        Map<Integer, LineType> classes = cl.classifyLines();

        File graphic = new File(graphImageFileName);
        OneLevelCodeBlock codeBlock = new OneLevelCodeBlock(pt.getRoot());
        ControlFlowCreator cfc = new ControlFlowCreator(codeBlock);
        ControlFlowBlock cfb = cfc.createControlFlowBlock();
        ControlFlowDrawer cfd = new ControlFlowDrawer(cfb, classes);
        cfd.drawControlFlowGraph(graphic, quilFileName);
    }

    public static void main(String[] args) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        System.out.println(System.getProperty("user.dir"));

        final String file = "teleport-by-quil";
        String grammarFileName = resourcePath + "Quil.g4";
        String quilFileName = resourcePath + "Quil/" + file + ".quil";
        String graphImageFileName = resourcePath + "Quil/" + file + ".ps";

        drawQuilCfg(grammarFileName, quilFileName, graphImageFileName);

    }
}
