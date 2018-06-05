package de.ghci.dialog.process;

import de.ghci.dialog.model.dialog.AskedQuestion;
import de.ghci.dialog.model.dialog.Dialog;
import de.ghci.dialog.model.dialog.Question;
import de.ghci.dialog.model.dialog.Utterance;

/**
 * @author Dominik
 */
public interface QuestionProcess {

    Utterance handleQuestionAnswer(Question asked, String text, Dialog dialog);

    AskedQuestion askQuestion(Dialog dialog);

}
