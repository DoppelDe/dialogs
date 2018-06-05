package de.ghci.dialog.model.switchboard;

/**
 * @author Dominik
 */
public class SwUtterance {

    private String dialogAct;
    private String text;

    public SwUtterance(String dialogAct, String text) {
        this.dialogAct = dialogAct;
        this.text = text;
    }

    public String getDialogAct() {
        return dialogAct;
    }

    public String getText() {
        return text;
    }
}
