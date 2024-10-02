import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.ControlFlowCreator;
import de.hhu.lirem101.quil_analyser.ControlFlowDrawer;
import de.hhu.lirem101.quil_analyser.OneLevelCodeBlock;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class Main {
    public static final String resourcePath = System.getProperty("user.dir") + "/inmemantlr-api/src/main/resources/";

    public static void main(String[] args) throws IOException, CompilationException, ParsingException, IllegalWorkflowException {
        System.out.println(System.getProperty("user.dir"));

        final String file = "simple";

        File f = new File(resourcePath + "Quil.g4");
        GenericParser gp = new GenericParser(f);
        // 2. load file content into string
        String s = FileUtils.loadFileContent(resourcePath + "Quil/" + file + ".quil");
        // 3. set listener for checking parse tree elements. Here you could use any ParseTreeListener implementation. The default listener is used per default
        // this listener will create a parse tree from the java file
        DefaultTreeListener dlist = new DefaultTreeListener();
        gp.setListener(dlist);
        // 4. compile Lexer and parser in-memory
        gp.compile();
        // 5. parse the string
        gp.parse(s, null, GenericParser.CaseSensitiveType.NONE);

        ParseTree pt = dlist.getParseTree();

        File graphic = new File(resourcePath + "Quil/" + file + ".png");
        OneLevelCodeBlock codeBlock = new OneLevelCodeBlock(pt.getRoot());
        ControlFlowCreator cfc = new ControlFlowCreator(codeBlock);
        ControlFlowBlock cfb = cfc.createControlFlowBlock();
        ControlFlowDrawer cfd = new ControlFlowDrawer(cfb);
        cfd.drawControlFlowGraph(graphic);

//        String xml = pt.toXml();
//        String json = pt.toJson();
//        String dot = pt.toDot();
//
//        System.out.println(xml);
//        System.out.println(json);
//        System.out.println(dot);

        // Save .dot file
//        Path path = Paths.get(resourcePath + "Quil/" + file + ".dot");
//        Files.write(path, dot.getBytes());

    }
}
