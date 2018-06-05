package de.ghci.dialog.model.dialog;


/**
 * @author Dominik
 */
public class AskedQuestion extends Utterance {

    private Question question;
    private Object[] objects;

    public AskedQuestion(Question question) {
        super(question.getText(), Speaker.MACHINE);
        this.question = question;
    }

    public AskedQuestion(Question question, Object... objects) {
        super(getQuestionText(question, objects), Speaker.MACHINE);
        this.question = question;
        this.objects = objects;
    }

    private static String getQuestionText(Question question, Object... objects) {
        return String.format(question.getText(), objects);
    }

    public Question getQuestion() {
        return question;
    }

    public Object[] getObjects() {
        return objects;
    }
}
