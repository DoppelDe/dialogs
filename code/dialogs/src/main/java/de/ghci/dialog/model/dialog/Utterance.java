package de.ghci.dialog.model.dialog;

/**
 * @author Dominik
 */
public class Utterance {

    private String visibleText;
    private final String utterance;
    private final Speaker speaker;
    private final DialogAct dialogAct;

    public Utterance(String utterance, Speaker speaker) {
        this.utterance = utterance;
        this.speaker = speaker;
        this.dialogAct = DialogAct.STATEMENT;
    }

    public Utterance(String utterance, Speaker speaker, DialogAct dialogAct) {
        this.utterance = utterance;
        this.speaker = speaker;
        this.dialogAct = dialogAct;
    }

    public String getUtterance() {
        return utterance;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public DialogAct getDialogAct() {
        return dialogAct;
    }

    public String getVisibleText() {
        if(visibleText == null) {
            return utterance;
        } else {
            return visibleText;
        }
    }

    public void setVisibleText(String visibleText) {
        this.visibleText = visibleText;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "visibleText='" + visibleText + '\'' +
                ", utterance='" + utterance + '\'' +
                ", speaker=" + speaker +
                ", dialogAct=" + dialogAct +
                '}';
    }
}
