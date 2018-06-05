package de.ghci.dialog.model.products;

import de.ghci.dialog.model.statement.OpinionContext;

/**
 * @author Dominik
 */
public enum ProductContext implements OpinionContext{

    SPECIFIC, GENERAL, UNKNOWN;

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
