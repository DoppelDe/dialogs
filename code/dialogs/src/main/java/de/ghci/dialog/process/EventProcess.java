package de.ghci.dialog.process;

import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * @author Dominik
 */
public interface EventProcess {

    Optional<Event> getRelatedEvent(String text, List<Event> mentionedEvents, Information information);
}
