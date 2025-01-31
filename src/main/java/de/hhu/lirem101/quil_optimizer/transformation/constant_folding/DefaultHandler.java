package de.hhu.lirem101.quil_optimizer.transformation.constant_folding;

public class DefaultHandler implements Handler {
    /**
     * Used for instructions that do not have a specific handler.
     * @return false
     */
    @Override
    public boolean propagateConstant() {
        return false;
    }
}
