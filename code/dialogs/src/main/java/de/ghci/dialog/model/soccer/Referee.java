package de.ghci.dialog.model.soccer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Dominik
 */
public class Referee extends Person {

    public Referee(String firstName, String lastName) {
        super(firstName, lastName);
    }

    public Referee(String id) {
        super(id);
    }

    public Referee() {

    }

    public Referee(Referee referee) {
        setId(referee.getId());
        setFirstName(referee.getFirstName());
        setLastName(referee.getLastName());
        setBirthDate(referee.getBirthDate());
        setNationality(referee.getNationality());
        setBirthPlaceIfNull(referee.getBirthPlace());
    }
}
