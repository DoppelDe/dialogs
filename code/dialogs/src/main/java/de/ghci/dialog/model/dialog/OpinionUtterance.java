package de.ghci.dialog.model.dialog;

import de.ghci.dialog.model.statement.OpinionStatement;

/**
 * @author Dominik
 */
public class OpinionUtterance extends Utterance {

    private final OpinionStatement opinionStatement;

    public OpinionUtterance(OpinionStatement opinionStatement, Speaker speaker) {
        super(opinionStatement.getText(), speaker);
        this.opinionStatement = opinionStatement;
    }

    public OpinionStatement getOpinionStatement() {
        return opinionStatement;
    }
}
