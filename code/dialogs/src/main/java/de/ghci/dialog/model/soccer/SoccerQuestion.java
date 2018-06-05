package de.ghci.dialog.model.soccer;

import de.ghci.dialog.model.dialog.Question;

/**
 * @author Dominik
 */
public enum SoccerQuestion implements Question {

    REFEREE("What's your opinion of the referee in the game %s against %s?"),
    FAVORITE_PLAYER("Who is your favorite player?"),
    FAVORITE_TEAM("What is your favorite team?"),
    BEST_PLAYER("Who do you think was the best player in the game %s against %s?"),
    HOW_PLAYED_TEAM_SEASON("What do you say to the performance of %s this year?"),
    HOW_PLAYED_TEAM("What do you say to the performance of %s last game?"),
    CARD_DESERVED("Do you think, %s did earn the %s card?");


    private String text;

    SoccerQuestion(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

}
