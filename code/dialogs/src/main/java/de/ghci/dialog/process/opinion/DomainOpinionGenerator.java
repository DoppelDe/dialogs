package de.ghci.dialog.process.opinion;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.soccer.SoccerContext;
import de.ghci.dialog.model.statement.OpinionContext;

/**
 * @author Dominik
 */
public interface DomainOpinionGenerator {

    OpinionAspect getRandomAspect(Entity entity);

    OpinionContext getRandomContext(Entity entity);

    Entity getRandomEntity(Entity entity, Information information);
}
