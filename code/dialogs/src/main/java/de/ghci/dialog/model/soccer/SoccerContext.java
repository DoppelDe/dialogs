package de.ghci.dialog.model.soccer;

import de.ghci.dialog.model.statement.OpinionContext;

/**
 * @author Dominik
 */
public enum SoccerContext implements OpinionContext {

    ALL_TIME, SEASONS, SEASON, COMPETITION, MATCHES, MATCH, UNKNOWN;

    private String information;

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    @Override
    public boolean isUnknown() {
        return this == UNKNOWN;
    }
}
