/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
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

import org.apache.commons.numbers.complex.Complex;

public class ClassicalVariable implements Variable {
    private final String name;
    private boolean shownToBeDead = false;
    private boolean isConstant = false;
    private final ClassicalUsage usage;
    private Complex value;

    public ClassicalVariable(String name, ClassicalUsage usage) {
        this.name = name;
        this.usage = usage;
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
        return isConstant;
    }

    public ClassicalUsage getUsage() {
        return usage;
    }

    public void setValue(double value) {
        this.value = Complex.ofCartesian(value, 0);
        isConstant = true;
    }

    public void setValue(Complex value) {
        this.value = value;
        isConstant = true;
    }

    public Complex getValue() {
        if(isConstant) {
            return value;
        } else {
            throw new RuntimeException("Value not set for variable " + name);
        }
    }

    public ClassicalVariable copyCV() {
        ClassicalVariable cv = new ClassicalVariable(name, usage);
        cv.shownToBeDead = shownToBeDead;
        cv.isConstant = isConstant;
        cv.value = value;
        return cv;
    }
}
