package de.ghci.dialog.model.products;

import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;

/**
 * @author Dominik
 */
public enum ProductAspect implements OpinionAspect {

    PERFORMANCE, PRICE, UNKNOWN;

    private AspectState state;

    public AspectState getState() {
        return state;
    }

    public void setState(AspectState state) {
        this.state = state;
    }

    @Override
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    @Override
    public String toString() {
        return "OpinionAspect{" +
                "name=" + name() +
                ", state=" + state +
                '}';
    }
}