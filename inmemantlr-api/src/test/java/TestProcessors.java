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

import org.junit.Before;
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

import static org.junit.Assert.assertTrue;

public class TestProcessors {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProcessors.class);

    String sgrammarcontent = "";
    String s = "";

    @Before
    public void init() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream sgrammar = classLoader.getResourceAsStream("inmemantlr/Java.g4");
             InputStream sfile = classLoader.getResourceAsStream("inmemantlr/HelloWorld.java")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
            s = FileUtils.getStringFromStream(sfile);
        }
    }

    private static Ast ast = null;

    @Before
    public void prepare() {
        GenericParser gp = new GenericParser(sgrammarcontent);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        assertTrue(compile);
        assertTrue(s != null && !s.isEmpty());

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);

        try {
            gp.parse(s);
        } catch (IllegalWorkflowException | ParsingException e) {
            assertTrue(false);
        }

        ast = dlist.getAst();
    }

    @Test
    public void testXmlProcessor() {
        String xml = ast.toXml();
        LOGGER.debug(xml);
        assertTrue(xml.length() > 1);
    }

    @Test
    public void testJsonProcessor() {
        String json = ast.toJson();
        LOGGER.debug(json);
        assertTrue(json.length() > 1);
    }
}
