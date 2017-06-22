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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.Ast;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSimple {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSimple.class);

    String sgrammarcontent = "";

    @Test
    public void testInterpreter() throws IOException {

        try (InputStream sgrammar = getClass().getClassLoader().getResourceAsStream("inmemantlr/Simple.g4")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        }

        GenericParser gp = new GenericParser(sgrammarcontent);
        DefaultTreeListener t = new DefaultTreeListener();

        gp.setListener(t);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        assertTrue(compile);

        // this example shows you how one could use inmemantlr for incremental parsing
        try {
            Ast ast;
            gp.parse("PRINT a+b");
            ast = t.getAst();
            LOGGER.debug(ast.toDot());
            assertEquals(ast.getNodes().size(), 6);
            gp.parse("PRINT \"test\"");
            ast = t.getAst();
            LOGGER.debug(ast.toDot());
            assertEquals(ast.getNodes().size(), 4);
        } catch (IllegalWorkflowException | ParsingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testOclStringParsing() throws IOException {

        try (InputStream sgrammar = getClass().getClassLoader()
                .getResourceAsStream("inmemantlr/DeepOcl.g4")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        }

        String toParse = "context Dependent inv inv54: (self.birthyear " +
                ">=2012 and self.allowances->size()=1) or (self.birthyear < " +
                "2012 and self.birthyear >= 1996)";


        GenericParser gp = new GenericParser(sgrammarcontent);
        DefaultTreeListener t = new DefaultTreeListener();

        gp.setListener(t);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
            LOGGER.debug(e.getMessage());
        }

        assertTrue(compile);

        try {
            gp.parse(toParse);
        } catch (IllegalWorkflowException | ParsingException e) {
            assertTrue(false);
        }

        Ast a = t.getAst();

    }



    @Test
    public void testLogic() throws IOException {

        try (InputStream sgrammar = getClass().getClassLoader()
                .getResourceAsStream("inmemantlr/Logic.g4")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        }

        String toParse = "( XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2  and  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX3 ) " +
                "or ( XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4  and  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX5  " +
                "and  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX6 )";


        GenericParser gp = new GenericParser(sgrammarcontent);
        DefaultTreeListener t = new DefaultTreeListener();

        gp.setListener(t);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
            LOGGER.debug(e.getMessage());
        }

        assertTrue(compile);

        try {
            gp.parse(toParse);
        } catch (IllegalWorkflowException | ParsingException e) {
            assert false;
        }

        Ast a = t.getAst();

        assertEquals(a.getNodes().size(), 22);
    }

}