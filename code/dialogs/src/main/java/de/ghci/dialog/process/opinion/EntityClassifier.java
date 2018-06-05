package de.ghci.dialog.process.opinion;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;

import java.util.Collection;
import java.util.Set;

/**
 * @author Dominik
 */
public interface EntityClassifier {

    Set<Entity> getEntities(Information information, String text);

    Entity getEntity(Information information, String text);

}
