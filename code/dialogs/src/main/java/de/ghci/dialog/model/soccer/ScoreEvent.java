package de.ghci.dialog.model.soccer;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Event;
import org.nd4j.linalg.io.Assert;

/**
 * @author Dominik
 */
public class ScoreEvent extends Event {

    private static final long serialVersionUID = 1348687051822710499L;

    private Person player;

    public ScoreEvent(String description, Person player) {
        super(description);
        this.player = player;
    }

    @Override
    public String toString() {
        return "ScoreEvent{" +
                "player=" + getPlayer() +
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
