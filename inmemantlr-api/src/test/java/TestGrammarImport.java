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


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.GenericParser.CaseSensitiveType;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class TestGrammarImport {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestGenericParser.class);

    @Test
    public void testLogic() {

        File [] fls = {
                new File("src/test/resources/inmemantlr/Logic.g4"),
                new File("src/test/resources/inmemantlr/Extlogic.g4")
        };


        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(fls));


        assertDoesNotThrow(gp::compile);

        String toParse = "( XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2  and  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX3 ) " +
                "or ( XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4  and  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX5  " +
                "and  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX6 )";

        assertDoesNotThrow(() -> {
            gp.parse(toParse);
        });
    }

    @Test
    public void testOncrpc() {

        File [] fls = {
                new File("src/test/resources/inmemantlr/xdr.g4"),
                new File("src/test/resources/inmemantlr/oncrpcv2.g4")
        };


        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(fls));

        assertDoesNotThrow(gp::compile);

        assertDoesNotThrow(() -> {
            gp.parse(new File("src/test/resources/inmemantlr/CalculatorService.x"),
                    "oncrpcv2Specification", CaseSensitiveType.NONE);
        });
    }

}
