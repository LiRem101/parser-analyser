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

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParserToGo;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.utils.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class TestProcessors {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProcessors.class);

    private static String javagcontent = "";
    private static String javafcontent = "";

    private static String phplcontent = "";
    private static String phpgcontent = "";
    private static String phpfcontent = "";

    private static ParseTree phpParseTree = null, javaParseTree = null;

    static {
        ClassLoader classLoader = TestProcessors.class.getClassLoader();

        InputStream javasgrammar = classLoader.getResourceAsStream("inmemantlr/Java.g4");
        InputStream javasfile = classLoader.getResourceAsStream
                ("inmemantlr/HelloWorld.java");
        javagcontent = FileUtils.getStringFromStream(javasgrammar);
        javafcontent = FileUtils.getStringFromStream(javasfile);

        InputStream phpslexer = classLoader.getResourceAsStream
                ("inmemantlr/PHPLexer.g4");

        InputStream phpsparser = classLoader.getResourceAsStream
                ("inmemantlr/PHPParser.g4");
        InputStream phpsfile = classLoader.getResourceAsStream
                ("inmemantlr/test.php");


        phplcontent = FileUtils.getStringFromStream(phpslexer);
        phpgcontent = FileUtils.getStringFromStream(phpsparser);
        phpfcontent = FileUtils.getStringFromStream(phpsfile);


        javaParseTree = new GenericParserToGo(javagcontent).parse(javafcontent, "compilationUnit");
        phpParseTree = new GenericParserToGo(phplcontent, phpgcontent).parse
                (phpfcontent, "htmlDocument");


    }


    //@Test
    public void testXmlProcessor() {
        String jxml = javaParseTree.toXml();
        String pxml = phpParseTree.toXml();

        Assertions.assertNotNull(jxml);
        Assertions.assertNotNull(pxml);

        Assertions.assertFalse(jxml.isEmpty());
        Assertions.assertFalse(pxml.isEmpty());

        LOGGER.debug(jxml);
        LOGGER.debug(pxml);

        DocumentBuilder newDocumentBuilder = null;
        try {
            newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            newDocumentBuilder.parse(new ByteArrayInputStream(jxml
                    .getBytes()));
        } catch (SAXException e) {
            Assertions.assertFalse(true);
        } catch (IOException e) {
            Assertions.assertFalse(true);
        }


        try {
            newDocumentBuilder.parse(new ByteArrayInputStream(pxml
                    .getBytes()));
        } catch (SAXException e) {
            Assertions.assertFalse(true);
        } catch (IOException e) {
            Assertions.assertFalse(true);
        }

    }

    //@Test
    public void testJsonProcessor() {
        String jjson = javaParseTree.toJson();
        String pjson = phpParseTree.toJson();

        Assertions.assertNotNull(jjson);
        Assertions.assertNotNull(pjson);

        Assertions.assertFalse(jjson.isEmpty());
        Assertions.assertFalse(pjson.isEmpty());

        LOGGER.debug(jjson);
        LOGGER.debug(pjson);

        try {
            new JsonParser().parse(jjson);
        } catch (JsonIOException | JsonSyntaxException e) {
            Assertions.assertFalse(true);
        }


        try {
            new JsonParser().parse(pjson);
        } catch (JsonIOException | JsonSyntaxException e) {
            Assertions.assertFalse(true);
        }

    }
}
