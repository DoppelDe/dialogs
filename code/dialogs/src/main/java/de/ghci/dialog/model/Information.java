package de.ghci.dialog.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Dominik
 */
public interface Information extends Serializable {

    Collection<Event> getEvents();

    Collection<Entity> getEntities();

}
