package de.ghci.dialog.model;

/**
 * @author Dominik
 */
public class NamedEntity {

    private final String type;
    private final String name;

    public NamedEntity(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NamedEntity{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
