package de.ghci.dialog.model.soccer;

import de.ghci.dialog.model.WordTagPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dominik
 */
public class ScoringIndicator {

    private List<WordTagPair> pairs;
    private boolean orderMatters = false;

    public ScoringIndicator(WordTagPair... pairs) {
        this.pairs = new ArrayList<>();
        Collections.addAll(this.pairs, pairs);
    }

    public List<WordTagPair> getPairs() {
        return pairs;
    }

    public boolean isOrderMatters() {
        return orderMatters;
    }

    public void setOrderMatters(boolean orderMatters) {
        this.orderMatters = orderMatters;
    }

}
