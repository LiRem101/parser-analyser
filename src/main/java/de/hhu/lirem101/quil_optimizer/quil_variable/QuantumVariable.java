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

package de.hhu.lirem101.quil_optimizer.quil_variable;

import de.hhu.lirem101.quil_optimizer.quantum_gates.QuantumCliffordGate;

public class QuantumVariable implements Variable {
    private final String name;
    private boolean shownToBeDead = false;
    private QuantumCliffordState cliffordStateBeforeGate = null;
    private QuantumCliffordState cliffordStateAfterGate = null;
    private final QuantumUsage usage;

    public QuantumVariable(String name, QuantumUsage usage) {
        this.name = name;
        this.usage = usage;
    }

    public QuantumCliffordState applyGate(QuantumCliffordGate gate) {
        if(cliffordStateAfterGate != null) {
            return cliffordStateAfterGate;
        }
        cliffordStateAfterGate = gate.apply(cliffordStateBeforeGate);
        return cliffordStateAfterGate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isShownToBeDead() {
        return shownToBeDead;
    }

    @Override
    public void setDead() {
        shownToBeDead = true;
    }

    @Override
    public boolean isConstant() {
        return cliffordStateBeforeGate != null;
    }

    public QuantumUsage getUsage() {
        return usage;
    }

    public void setCliffordStateBeforeGate(QuantumCliffordState cliffordStateBeforeGate) {
        this.cliffordStateBeforeGate = cliffordStateBeforeGate;
    }

    public QuantumCliffordState getCliffordStateBeforeGate() {
        return cliffordStateBeforeGate;
    }

    public QuantumCliffordState getCliffordStateAfterGate() {
        return cliffordStateAfterGate;
    }

    public QuantumVariable copyQV() {
        QuantumVariable qv = new QuantumVariable(name, usage);
        qv.cliffordStateBeforeGate = cliffordStateBeforeGate;
        qv.cliffordStateAfterGate = cliffordStateAfterGate;
        return qv;
    }
}
