package de.hhu.lirem101.quil_optimizer.quil_variable;

public interface Variable {
    public String getName();
    public boolean isShownToBeDead();
    public void setDead();
    public boolean isConstant();

}
