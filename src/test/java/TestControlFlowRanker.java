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

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.ControlFlowRanker;
import de.hhu.lirem101.quil_analyser.LineParameter;
import de.hhu.lirem101.quil_analyser.LineType;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestControlFlowRanker {
    @Test
    void oneBlock() {
        ControlFlowBlock block = new ControlFlowBlock("start");

        ControlFlowRanker cfr = new ControlFlowRanker(block, new ArrayList<>());
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(1, ranking.size());
        assertEquals(block, ranking.get(0));
    }

    @Test
    void simpleLinearCode() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle = new ControlFlowBlock("middle");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(middle);
        middle.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root, new ArrayList<>());
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(3, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(middle, ranking.get(1));
        assertEquals(halt, ranking.get(2));
    }

    @Test
    void branchingCode1() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle = new ControlFlowBlock("middle");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(halt);
        root.addBranch(middle);
        middle.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root, new ArrayList<>());
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(3, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(middle, ranking.get(1));
        assertEquals(halt, ranking.get(2));
    }

    @Test
    void branchingCode2() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle1 = new ControlFlowBlock("middle1");
        ControlFlowBlock middle2 = new ControlFlowBlock("middle2");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(halt);
        root.addBranch(middle1);
        root.addBranch(middle2);
        middle1.addBranch(halt);
        middle2.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root, new ArrayList<>());
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(4, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(halt, ranking.get(3));
    }

    @Test
    void bigBranching() {
        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock middle1 = new ControlFlowBlock("middle1");
        ControlFlowBlock middle2 = new ControlFlowBlock("middle2");
        ControlFlowBlock middle3 = new ControlFlowBlock("middle3");
        ControlFlowBlock middle4 = new ControlFlowBlock("middle4");
        ControlFlowBlock middle5 = new ControlFlowBlock("middle5");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(middle1);
        root.addBranch(middle2);
        middle1.addBranch(middle3);
        middle2.addBranch(middle3);
        middle3.addBranch(middle4);
        middle3.addBranch(middle5);
        middle4.addBranch(halt);
        middle5.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root, new ArrayList<>());
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        assertNotNull(ranking);
        assertEquals(7, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(middle3, ranking.get(3));
        assertEquals(halt, ranking.get(6));
    }

    @Test
    void withConditionalBranching() {
        ArrayList<LineParameter> lines = new ArrayList<>();
        lines.add(new LineParameter(1, LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL));

        ControlFlowBlock root = new ControlFlowBlock("start");
        ControlFlowBlock condJump = new ControlFlowBlock("condJump");
        condJump.addCodeline(1);
        ControlFlowBlock middle1 = new ControlFlowBlock("middle1");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        root.addBranch(condJump);
        condJump.addBranch(root);
        condJump.addBranch(middle1);
        middle1.addBranch(halt);

        ControlFlowRanker cfr = new ControlFlowRanker(root, lines);
        ArrayList<ControlFlowBlock> ranking = cfr.getRankedBlocks();
        Set<ControlFlowBlock> startBlocks = ranking
                .stream()
                .filter(block -> block.getName().equals("start"))
                .collect(Collectors.toSet());

        assertNotNull(ranking);
        assertEquals(6, ranking.size());
        assertEquals(root, ranking.get(0));
        assertEquals(2, startBlocks.size());
        assertTrue(condJump.getBranches().isEmpty());
    }
}
