package de.ghci.dialog.model.soccer;

/**
 * @author Dominik
 */
public enum Card {

    YELLOW("yellow"), RED("red");

    private String displayText;

    Card(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
