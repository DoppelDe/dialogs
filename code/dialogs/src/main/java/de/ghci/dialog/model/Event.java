package de.ghci.dialog.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Dominik
 */
public abstract class Event implements Serializable {

    private static final long serialVersionUID = -7606106304907116992L;

    private String description;

    public Event(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(description, event.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }
}
