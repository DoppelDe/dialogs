package de.ghci.dialog.model.soccer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Dominik
 */
public class Player extends Person {

    public Player(String firstName, String lastName) {
        super(firstName, lastName);
    }

    public Player(String id) {
        super(id);
    }

    public Player() {
    }

    public static Player createFromName(String name) {
        return createPlayerFromName(name);
    }

}
