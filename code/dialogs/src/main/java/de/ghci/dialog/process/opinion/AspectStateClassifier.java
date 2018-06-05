package de.ghci.dialog.process.opinion;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;

/**
 * @author Dominik
 */
public interface AspectStateClassifier {

    AspectState getAspectState(Information information, Entity entity, OpinionAspect aspect, OpinionContext context);

}
