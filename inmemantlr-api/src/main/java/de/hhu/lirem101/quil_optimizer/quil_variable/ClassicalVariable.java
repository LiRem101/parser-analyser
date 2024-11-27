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
