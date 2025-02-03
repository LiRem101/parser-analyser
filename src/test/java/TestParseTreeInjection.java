/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * SPDX-FileCopyrightText: 2016 Julian Thome <julian.thome.de@gmail.com>
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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParserToGo;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;
import org.snt.inmemantlr.utils.InjectionPointDetector;
import org.snt.inmemantlr.utils.ParseTreeManipulator;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;


public class TestParseTreeInjection {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestParseTreeInjection.class);


    @Test
    public void testInjection() {
        File gfile = new File(TestGenericParserToGo.class.getClassLoader()
                .getResource
                        ("inmemantlr/Java.g4").getFile());

        File cfile = new File(TestGenericParserToGo.class.getClassLoader()
                .getResource("inmemantlr/HelloWorld.java").getFile());



        GenericParserToGo g4go = new GenericParserToGo(gfile);
        ParseTree pt = g4go.parse(cfile,"compilationUnit");


        LOGGER.debug(pt.toDot());


        InjectionPointDetector ipdb = new InjectionPointDetector() {
            @Override
            public ParseTreeNode detect(ParseTree t) {
                return t.getNodes().stream().filter(n -> n.getRule().equals
                        ("block")).findFirst().orElse(null);
            }

            @Override
            public Position getPosition() {
                return Position.BEFORE;
            }
        };

        InjectionPointDetector ipda = new InjectionPointDetector() {
            @Override
            public ParseTreeNode detect(ParseTree t) {
                return t.getNodes().stream().filter(n -> n.getRule().equals
                        ("block")).findFirst().orElse(null);
            }

            @Override
            public Position getPosition() {
                return Position.AFTER;
            }
        };

        ParseTree inj = g4go.parse("int x = 0;",
                "blockStatement");


        assertDoesNotThrow(() -> {
            ParseTreeManipulator.INTANCE.inject(pt,ipdb,inj);
            ParseTreeManipulator.INTANCE.inject(pt,ipda,inj);
        });
        
        assertEquals(94, pt.getNodes().size());

    }

}
