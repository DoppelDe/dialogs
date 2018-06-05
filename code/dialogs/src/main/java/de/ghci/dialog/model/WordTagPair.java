package de.ghci.dialog.model;

/**
 * @author Dominik
 */
public class WordTagPair {

    private final String[] words;
    private final String tag;

    public WordTagPair(String tag, String... words) {
        this.tag = tag;
        this.words = words;
    }

    public String[] getWords() {
        return words;
    }

    public String getWord() {
        return words[0];
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "(" + words + "," + tag + ")";
    }
}
