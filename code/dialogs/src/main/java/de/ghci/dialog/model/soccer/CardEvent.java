package de.ghci.dialog.model.soccer;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Event;
import org.nd4j.linalg.io.Assert;

/**
 * @author Dominik
 */
public class CardEvent extends Event {

    private static final long serialVersionUID = 930008202748143603L;

    private Person player;
    private Card type;

    public CardEvent(String description, Person player, Card type) {
        super(description);
        this.player = player;
        this.type = type;
    }

    public Card getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CardEvent{" +
                "player=" + getPlayer()+
                ", type=" + type +
                ", description=" + getDescription() +
                '}';
    }

    public Person getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
