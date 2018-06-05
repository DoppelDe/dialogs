package de.ghci.dialog.process.opinion;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.soccer.SoccerContext;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;

/**
 * @author Dominik
 */
public interface DomainTextProvider {

    String getAspectText(OpinionAspect aspect, OpinionContext context, AspectState state);

    String getContextText(OpinionContext context);
}
