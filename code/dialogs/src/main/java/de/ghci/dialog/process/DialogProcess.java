package de.ghci.dialog.process;

import com.google.inject.Inject;
import de.ghci.dialog.model.*;
import de.ghci.dialog.model.statement.OpinionStatement;
import de.ghci.dialog.model.dialog.*;
import de.ghci.dialog.process.opinion.EntityClassifier;
import de.ghci.dialog.process.opinion.OpinionProcess;
import de.ghci.dialog.process.opinion.StatementParser;
import org.apache.commons.lang.StringUtils;
import org.lambda3.text.simplification.sentence.segmentation.SentenceSeparator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
public class DialogProcess extends Observable {

    private static final int MAX_SMALL_TALK = 2;
    private Dialog dialog;
    private Question asked;
    private final Information information;
    private OpinionProcess opinionProcess;
    private QuestionProcess questionProcess;
    private StatementParser statementParser;
    private CoReferenceResolution coReferenceResolution;
    private EventProcess eventProcess;
    private int smallTalkCounter;

    @Inject
    public DialogProcess(Information information, OpinionProcess opinionProcess,
                         QuestionProcess questionProcess, StatementParser statementParser,
                         CoReferenceResolution coReferenceResolution, EventProcess eventProcess) {
        this.dialog = new Dialog(information);
        this.information = information;
        this.opinionProcess = opinionProcess;
        this.questionProcess = questionProcess;
        this.statementParser = statementParser;
        this.coReferenceResolution = coReferenceResolution;
        this.eventProcess = eventProcess;
    }

    public void startDialog() {
        if (dialog.getUtterances().isEmpty()) {
            dialog.addMachineUtterance(getSmallTalkText("Hi"));
            smallTalkCounter = 0;
            setChangedAndNotify();
        }
    }

    public void newHumanUtterance(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        System.out.println("CO before: " + text);
        String utt = doDialogCoReference(text);
        System.out.println("CO after : " + utt);
        Utterance humanUtterance = parseHumanUtterance(utt);
        humanUtterance.setVisibleText(text);
        dialog.addUtterance(humanUtterance);

        Utterance machineAnswer = createMachineReply(humanUtterance);
        if (machineAnswer instanceof SmallTalkUtterance) {
            smallTalkCounter++;
        } else {
            smallTalkCounter = 0;
        }
        System.out.println("Schiri: " + machineAnswer.getUtterance());
        dialog.addUtterance(machineAnswer);
        setChangedAndNotify();
    }

    private void setChangedAndNotify() {
        setChanged();
        notifyObservers();
    }

    public Utterance askQuestion() {
        AskedQuestion utterance = questionProcess.askQuestion(dialog);
        asked = utterance.getQuestion();
        return utterance;
    }

    private String doDialogCoReference(String text) {
        if (dialog.getUtterances().size() == 0) {
            return text;
        } else {
            // Add a period to the end of the last utterances, then do coreference
            // afterwards, use periods to split where the current utterance started
            String utterance = getTextOfLastUtterances();
            boolean added = false;
            if (utterance.charAt(utterance.length() - 1) != '.') {
                utterance += ".";
                added = true;
            }
            int periods = StringUtils.countMatches(utterance, ".");
            String replace = coReferenceResolution.doCoreference(utterance + " " + text);
            String[] sentences = replace.split("\\.");
            String newText = "";
            if (sentences.length < periods) {
                throw new IllegalArgumentException("To few sentences: " + periods + " # " + utterance + " ### " + text);
            }
            for (int i = periods; i < sentences.length; i++) {
                if (!sentences[i].isEmpty()) {
                    newText += sentences[i] + ".";
                }
            }
            if (added) {
                return newText.substring(0, newText.length() - 1);
            } else {
                return newText;
            }
        }
    }

    private String getTextOfLastUtterances() {
        String text = "";
        // 500: random number of last characters where references might be to resolve
        for (int i = dialog.getUtterances().size() - 1; i >= 0 && text.length() < 500; i--) {
            Utterance utt = dialog.getUtterances().get(i);
            text = utt.getUtterance() + " " + text;
        }
        return text;
    }

    private Utterance parseHumanUtterance(String text) {
        List<String> sentences = SentenceSeparator.splitIntoSentences(text);
        DialogAct dialogAct;
        if (!sentences.isEmpty()) {
            dialogAct = DialogActClassifier.classify(sentences.get(sentences.size() - 1));
        } else {
            dialogAct = DialogActClassifier.classify(text);
        }
        return new Utterance(text, Speaker.HUMAN, dialogAct);
    }

    private Utterance createMachineReply(Utterance humanUtterance) {
        String text = humanUtterance.getUtterance();
        if (asked != null) {
            return handleQuestionAnswer(text);
        } else if (humanUtterance.getDialogAct() == DialogAct.QUESTION) {
            return handleQuestion(text);
        } else if (humanUtterance.getDialogAct() == DialogAct.STATEMENT) {
            Utterance utterance = handleStatement(text);
            if(utterance != null) {
                return utterance;
            }
        }
        return makeSmallTalk(text);
    }

    private Utterance handleQuestionAnswer(String text) {
        Utterance utterance = questionProcess.handleQuestionAnswer(asked, text, dialog);
        if (utterance == null) {
            return new CombinedUtterance(machineUtteranceFromText("Interesting."), askQuestion());
        } else {
            asked = null;
            return utterance;
        }
    }

    private Utterance handleStatement(String text) {
        OpinionStatement statement = statementParser.parse(text, dialog.getInformation());
        if (opinionProcess.isOpinionStatement(statement)) {
            System.out.println("is OPINION");
            return opinionProcess.handleOpinionStatement(statement, dialog);
        } else {
            System.out.println("is NON-OPINION");
            return handleNonOpinionStatement(text);
        }
    }

    private Utterance machineUtteranceFromText(String text) {
        return new Utterance(text, Speaker.MACHINE);
    }

    private Utterance makeSmallTalk(String text) {
        if (smallTalkCounter >= MAX_SMALL_TALK) {
            Utterance question = askQuestion();
            if (question != null) {
                if (text.contains("?")) {
                    return new CombinedUtterance(getSmallTalkAndCreateUtterance(text), question);
                }
                return question;
            }
        }

        return getSmallTalkAndCreateUtterance(text);
    }

    private SmallTalkUtterance getSmallTalkAndCreateUtterance(String text) {
        String smallTalkText = getSmallTalkText(text);
        return new SmallTalkUtterance(smallTalkText);
    }


    private String getSmallTalkText(String text) {
        return SmallTalkProcess.generateReply(text).replaceAll("\\s+", " ");
    }

    private Utterance handleNonOpinionStatement(String text) {
        Optional<Event> event = eventProcess.getRelatedEvent(text, dialog.getMentionedEvents(), information);
        if (event.isPresent()) {
            dialog.addMentionedEvent(event.get());
            return machineUtteranceFromText(event.get().getDescription());
        } else {
            return makeSmallTalk(text);
        }
    }

    private Utterance handleQuestion(String text) {
        if (text.toLowerCase().contains("how")) {
            OpinionStatement opinionStatement = statementParser.parse(text, dialog.getInformation());
            if (opinionProcess.isOpinionStatement(opinionStatement)) {
                return opinionProcess.getOpinionUtterance(opinionStatement, dialog);
            }
        }
        return makeSmallTalk(text);
    }

    public Dialog getDialog() {
        return dialog;
    }
}
