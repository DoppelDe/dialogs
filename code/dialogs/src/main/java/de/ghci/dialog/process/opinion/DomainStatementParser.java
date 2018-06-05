package de.ghci.dialog.process.opinion;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;

/**
 * @author Dominik
 */
public interface DomainStatementParser {

    OpinionAspect getOpinionAspect(String text, Entity entity);

    OpinionContext getOpinionContext(String text, Entity entity);

}
