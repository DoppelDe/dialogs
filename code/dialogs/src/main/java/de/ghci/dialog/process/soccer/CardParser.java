package de.ghci.dialog.process.soccer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.soccer.SoccerInformation;
import de.ghci.dialog.model.soccer.Card;
import de.ghci.dialog.model.soccer.CardEvent;
import de.ghci.dialog.model.soccer.Person;
import de.ghci.dialog.model.soccer.Player;
import de.ghci.dialog.process.ContainsMatcher;
import de.ghci.dialog.process.NamedEntityClassifier;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;
import org.lambda3.graphene.core.relation_extraction.model.ExElement;
import org.lambda3.graphene.core.relation_extraction.model.ExSentence;

import java.util.*;

/**
 * @author Dominik
 */
@Singleton
public class CardParser {

    @Inject
    private ContainsMatcher containsMatcher;

    public List<CardEvent> getCards(String text, ExContent exContent, SoccerInformation worldInformation) {
        List<CardEvent> cards = new ArrayList<>();
        for (ExSentence sentence : exContent.getSentences()) {
            if (containsMatcher.matches("yellow card", sentence.getOriginalSentence())) {
                Player player = getTarget(text, sentence, worldInformation.getPlayers());
                cards.add(new CardEvent(sentence.toString(), player, Card.YELLOW));
            } else if (containsMatcher.matches("red card", sentence.getOriginalSentence())) {
                Player player = getTarget(text, sentence, worldInformation.getPlayers());
                cards.add(new CardEvent(sentence.toString(), player, Card.RED));
            }
        }
        return cards;
    }

    private Player getTarget(String text, ExSentence sentence, Collection<Player> players) {
        List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(text);
        if (namedEntities.size() == 0) {
            return null;
        } else if (namedEntities.size() == 1) {
            String name = namedEntities.get(0).getName();
            Player fromName = Person.getPersonFromName(players, name);
            if (fromName != null) {
                return fromName;
            } else {
                return Player.createFromName(name);
            }
        } else {
            return getTargetForMultipleEntities(namedEntities, sentence, players);
        }
    }

    private Player getTargetForMultipleEntities(List<NamedEntity> namedEntities, ExSentence sentence,
                                                       Collection<Player> players) {
        List<Player> possibleTargets = getPlayersFromNamedEntities(namedEntities, players);
        if (possibleTargets.size() == 0) {
            return Player.createFromName(namedEntities.get(0).getName());
        } else {
            for (ExElement element : sentence.getElements()) {
                if (element.getSpo().isPresent()) {
                    List<NamedEntity> nes = NamedEntityClassifier.getNamedEntities(element.getSpo().get().getSubject());
                    List<Player> cardPlayers = getPlayersFromNamedEntities(nes, players);
                    if (cardPlayers.size() >= 1) {
                        return cardPlayers.get(0);
                    }
                }
            }
            return possibleTargets.get(0);
        }
    }

    private List<Player> getPlayersFromNamedEntities(List<NamedEntity> namedEntities, Collection<Player> players) {
        List<Player> validPlayers = new ArrayList<>();
        for (NamedEntity ne : namedEntities) {
            Player fromName = Person.getPersonFromName(players, ne.getName());
            if (fromName != null) {
                validPlayers.add(fromName);
            }
        }
        return validPlayers;
    }
}
