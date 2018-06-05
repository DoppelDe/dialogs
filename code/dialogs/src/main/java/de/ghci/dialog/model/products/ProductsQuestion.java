package de.ghci.dialog.model.products;

import de.ghci.dialog.model.dialog.Question;

/**
 * @author Dominik
 */
public enum ProductsQuestion implements Question {

    PHONE_OWN("What cell phone do you own?"),
    FAVORITE_PHONE("What is your favorite cell phone?"),
    HOW_IS_PRODUCT("How do you like the %s?");

    private String text;

    ProductsQuestion(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

}
