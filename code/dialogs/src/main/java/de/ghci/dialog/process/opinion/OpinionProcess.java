package de.ghci.dialog.process.opinion;

import com.google.inject.Inject;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.model.statement.OpinionStatement;
import de.ghci.dialog.model.soccer.SoccerContext;
import de.ghci.dialog.model.dialog.Dialog;
import de.ghci.dialog.model.dialog.OpinionUtterance;
import de.ghci.dialog.model.dialog.Speaker;
import de.ghci.dialog.model.dialog.Utterance;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
public class OpinionProcess {

    @Inject
    private OpinionStatementGenerator statementGenerator;

    public boolean isOpinionStatement(OpinionStatement statement) {
        return statement != null && statement.getAspect() != null && !statement.getAspect().isUnknown();
    }

    public Utterance getOpinionUtterance(OpinionStatement opinionStatement, Dialog dialog) {
        return getOpinionUtterance(opinionStatement, dialog, SoccerContext.MATCH);
    }

    public Utterance getOpinionUtterance(OpinionStatement opinionStatement, Dialog dialog,
                                         SoccerContext defaultContext) {
        OpinionContext context = opinionStatement.getContext();
        for (int i = dialog.getUtterances().size() - 1; i >= 0 && context == SoccerContext.UNKNOWN; i--) {
            Utterance utterance = dialog.getUtterances().get(i);
            if (utterance instanceof OpinionUtterance) {
                context = ((OpinionUtterance) utterance).getOpinionStatement().getContext();
            }
        }
        if (context == SoccerContext.UNKNOWN) {
            context = defaultContext;
        }
        opinionStatement.setContext(context);
        return new OpinionUtterance(statementGenerator.generateOpinionStatement(opinionStatement,
                getOpinionUtterances(dialog), dialog.getInformation()), Speaker.MACHINE);
    }

    public Utterance handleOpinionStatement(OpinionStatement statement, Dialog dialog) {
        List<OpinionUtterance> opinionStatements = getOpinionUtterances(dialog);
        int numberOfStatements = 0;
        OpinionStatement newStatement = statementGenerator.generateResponseStatement(statement, opinionStatements,
                dialog.getInformation());
        if(newStatement == null) {
            return null;
        }
        while (newStatement.getAspect().getState() == AspectState.AVERAGE && numberOfStatements < 5) {
            newStatement = statementGenerator.generateResponseStatement(statement, opinionStatements,
                    dialog.getInformation());
            numberOfStatements++;
        }
        return new OpinionUtterance(newStatement, Speaker.MACHINE);
    }

    private List<OpinionUtterance> getOpinionUtterances(Dialog dialog) {
        return dialog.getUtterances().stream()
                .filter(u -> u instanceof OpinionUtterance)
                .map(u -> ((OpinionUtterance) u))
                .collect(Collectors.toList());
    }
}
