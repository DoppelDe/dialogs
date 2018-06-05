package de.ghci.dialog.process.soccer;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.soccer.CardEvent;
import de.ghci.dialog.model.soccer.ScoreEvent;
import de.ghci.dialog.process.EventProcess;
import de.ghci.dialog.process.NamedEntityClassifier;
import de.ghci.dialog.process.opinion.EntityClassifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
public class SoccerEventProcess implements EventProcess {

    private EntityClassifier entityClassifier;

    @Inject
    public SoccerEventProcess(EntityClassifier entityClassifier) {
        this.entityClassifier = entityClassifier;
    }

    @Override
    public Optional<Event> getRelatedEvent(String text, List<Event> mentionedEvents, Information information) {
        List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(text);
        List<Entity> entities = namedEntities.stream()
                .filter(ne -> entityClassifier.getEntity(information, ne.getName()) != null)
                .map(ne -> entityClassifier.getEntity(information, ne.getName()))
                .collect(Collectors.toList());
        if (!entities.isEmpty()) {
            return getMatchingEvent(getAvailableEvents(mentionedEvents, information), entities);
        }
        return Optional.empty();
    }

    private List<Event> getAvailableEvents(List<Event> mentionedEvents, Information information) {
        return information.getEvents()
                .stream().filter(e -> !mentionedEvents.contains(e))
                .collect(Collectors.toList());
    }

    private Optional<Event> getMatchingEvent(List<Event> events, List<Entity> entities) {
        ArrayList<Event> randomisedEvents = new ArrayList<>(events);
        Collections.shuffle(randomisedEvents);
        for (Event event : randomisedEvents) {
            if (entities.contains(getEntity(event))) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    private Entity getEntity(Event event) {
        if(event instanceof CardEvent) {
            return ((CardEvent) event).getPlayer();
        } else if(event instanceof ScoreEvent) {
            return ((ScoreEvent) event).getPlayer();
        } else {
            return null;
        }
    }
}
