package de.ghci.dialog.process.products;

import com.google.inject.Inject;
import de.ghci.dialog.io.products.ProductsLoader;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.dialog.*;
import de.ghci.dialog.model.products.CellPhoneAspect;
import de.ghci.dialog.model.products.ProductContext;
import de.ghci.dialog.model.products.ProductsQuestion;
import de.ghci.dialog.model.products.ProductsInformation;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.model.statement.OpinionStatement;
import de.ghci.dialog.process.QuestionProcess;
import de.ghci.dialog.process.opinion.EntityClassifier;
import de.ghci.dialog.process.opinion.OpinionProcess;
import de.ghci.dialog.process.opinion.OpinionStatementGenerator;
import de.ghci.dialog.process.opinion.StatementParser;
import org.nd4j.linalg.io.Assert;

import java.util.*;

/**
 * @author Dominik
 */
public class ProductsQuestionProcess implements QuestionProcess {

    private OpinionStatementGenerator opinionGenerator;
    private OpinionProcess opinionProcess;
    private EntityClassifier entityClassifier;
    private StatementParser statementParser;
    private ProductsLoader productsLoader;

    @Inject
    public ProductsQuestionProcess(OpinionStatementGenerator opinionGenerator, OpinionProcess opinionProcess,
                                   EntityClassifier entityClassifier, StatementParser statementParser,
                                   ProductsLoader productsLoader) {
        this.opinionGenerator = opinionGenerator;
        this.opinionProcess = opinionProcess;
        this.entityClassifier = entityClassifier;
        this.statementParser = statementParser;
        this.productsLoader = productsLoader;
    }

    @Override
    public Utterance handleQuestionAnswer(Question question, String response, Dialog dialog) {
        Assert.isTrue(question instanceof ProductsQuestion);
        ProductsQuestion asked = (ProductsQuestion) question;
        Utterance machineAnswer;
        switch (asked) {
            case FAVORITE_PHONE:
                machineAnswer = handlePhoneAnswer(response, ProductContext.SPECIFIC, dialog);
                break;
            case PHONE_OWN:
                machineAnswer = handlePhoneAnswer(response, ProductContext.SPECIFIC, dialog);
                break;
            case HOW_IS_PRODUCT:
                machineAnswer = handleHowIsProduct(response, dialog);
                break;
            default:
                machineAnswer = machineUtteranceFromText("I forgot what I asked, do you remember?");
        }

        if (machineAnswer != null) {
            return machineAnswer;
        } else {
            Utterance utterance = askQuestion(dialog);
            return new CombinedUtterance(machineUtteranceFromText("Interesting."), utterance);
        }
    }

    private Utterance handlePhoneAnswer(String text, OpinionContext context, Dialog dialog) {
        Set<Entity> entities = entityClassifier.getEntities(dialog.getInformation(), text);

        if (entities.isEmpty()) {
            return machineUtteranceFromText("What do you like about it?");
        }

        if (entities.size() > 1) {
            return machineUtteranceFromText("I asked for only one cell phone!");
        }

        for (Entity entity : entities) { // Always only one cell phone
            Entity en = entityClassifier.getEntity(dialog.getInformation(), entity.getLabel());
            OpinionStatement opinionStatement = opinionGenerator.generateStatement(en, CellPhoneAspect.PERFORMANCE,
                    context, dialog.getInformation());
            return opinionProcess.getOpinionUtterance(opinionStatement, dialog);
        }
        throw new IllegalStateException();
    }

    private Utterance machineUtteranceFromText(String text) {
        return new Utterance(text, Speaker.MACHINE);
    }

    private Utterance handleHowIsProduct(String text, Dialog dialog) {
        OpinionStatement statement = statementParser.parse(text, dialog.getInformation());
        if (opinionProcess.isOpinionStatement(statement)) {
            if (statement.getContext().isUnknown()) {
                statement.setContext(ProductContext.GENERAL);
            }
            return opinionProcess.handleOpinionStatement(statement, dialog);
        } else {
            return null;
        }
    }


    @Override
    public AskedQuestion askQuestion(Dialog dialog) {
        Assert.isTrue(dialog.getInformation() instanceof ProductsInformation);

        Set<ProductsQuestion> availableQuestions = getAvailableQuestions(dialog);
        if (availableQuestions.size() > 0) {
            ProductsQuestion question = availableQuestions
                    .toArray(new ProductsQuestion[]{})[new Random().nextInt(availableQuestions.size())];
            return createQuestion(question);
        } else {
            return null;
        }
    }

    private Set<ProductsQuestion> getAvailableQuestions(Dialog dialog) {
        Set<ProductsQuestion> availableQuestions = new HashSet<>();
        Collections.addAll(availableQuestions, ProductsQuestion.values());
        for (Utterance utterance : dialog.getUtterances()) {
            if (utterance instanceof AskedQuestion) {
                availableQuestions.remove(((AskedQuestion) utterance).getQuestion());
            }
        }
        return availableQuestions;
    }

    private AskedQuestion createQuestion(ProductsQuestion asked) {
        if (asked == ProductsQuestion.HOW_IS_PRODUCT) {
            return new AskedQuestion(asked, productsLoader.getRandomFonoProduct().getDeviceName());
        } else {
            return new AskedQuestion(asked);
        }
    }
}
