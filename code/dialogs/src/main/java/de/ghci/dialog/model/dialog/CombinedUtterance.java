package de.ghci.dialog.model.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dominik
 */
public class CombinedUtterance extends Utterance {

    private List<Utterance> utterances;

    public CombinedUtterance(Utterance u1, Utterance... utterances) {
        super(getCombinedText(u1, utterances), getCombinedSpeaker(u1, utterances));
        this.utterances = new ArrayList<>();
        this.utterances.add(u1);
        Collections.addAll(this.utterances, utterances);
    }

    private static Speaker getCombinedSpeaker(Utterance u1, Utterance[] utterances) {
        for(Utterance utterance : utterances) {
            if(utterance.getSpeaker() != u1.getSpeaker()) {
                throw new IllegalArgumentException();
            }
        }
        return u1.getSpeaker();
    }

    private static String getCombinedText(Utterance u1, Utterance[] utterances) {
        String text = u1.getUtterance();
        for(Utterance utterance : utterances) {
            text += " " + utterance.getUtterance();
        }
        return text;
    }

    public List<Utterance> getUtterances() {
        return utterances;
    }
}
