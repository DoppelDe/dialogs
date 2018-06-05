package de.ghci.dialog.model.statement;

/**
 * @author Dominik
 */
public interface OpinionAspect {

    AspectState getState();

    void setState(AspectState state);

    boolean isUnknown();

    String toString();
}
