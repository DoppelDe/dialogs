package de.ghci.dialog.model;

/**
 * @author Dominik
 */
public enum Sentiment {

    STRONG_NEGATIVE, WEAK_NEGATIVE, NEUTRAL, WEAK_POSITIVE, STRONG_POSITIVE;

    private String cause;

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public boolean isBetter(Sentiment other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean isWorse(Sentiment other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isPositive() {
        return this == WEAK_POSITIVE || this == STRONG_POSITIVE;
    }

    public boolean isNegative() {
        return this == WEAK_NEGATIVE || this == STRONG_NEGATIVE;
    }

}
