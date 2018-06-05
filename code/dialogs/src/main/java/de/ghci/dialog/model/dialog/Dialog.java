package de.ghci.dialog.model.dialog;

import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.soccer.MatchInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
public class Dialog {

    private Information information;
    private List<Utterance> utterances;
    private List<Event> mentionedEvents;

    public Dialog(Information information) {
        this.information = information;
        utterances = new ArrayList<>();
        mentionedEvents = new ArrayList<>();
    }

    public void addMentionedEvent(Event event) {
        mentionedEvents.add(event);
    }

    public List<Event> getMentionedEvents() {
        return mentionedEvents;
    }

    public Utterance addHumanUtterance(String text) {
        Utterance utterance = new Utterance(text, Speaker.HUMAN);
        utterances.add(utterance);
        return utterance;
    }

    public Utterance addMachineUtterance(String text) {
        Utterance utterance = new Utterance(text, Speaker.MACHINE);
        utterances.add(utterance);
        return utterance;
    }

    public void addUtterance(Utterance utterance) {
        utterances.add(utterance);
    }

    public List<Utterance> getUtterances() {
        return utterances;
    }

    public Information getInformation() {
        return information;
    }

}
