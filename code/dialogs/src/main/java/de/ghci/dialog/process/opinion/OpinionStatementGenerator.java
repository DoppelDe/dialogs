package de.ghci.dialog.process.opinion;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.model.statement.OpinionStatement;
import de.ghci.dialog.model.dialog.OpinionUtterance;
import de.ghci.dialog.model.dialog.Speaker;

import java.util.*;

/**
 * @author Dominik
 */
public class OpinionStatementGenerator {

    private static final String[] AGREEMENTS = new String[]{"I agree", "Yes", "That's right"};
    private static final String[] CONJUNCTIONS = new String[]{", also ", ". ", ". Besides, "};
    private static final String I_AGREE_YOU_EARLIER = "I agree with what you said earlier. ";
    private static final String LIKE_I_SAID_BEFORE = "Like I said before, ";
    private static final String IMO = "In my opinion, ";
    private static final String PERIOD = ".";
    private static final String SPACE = " ";
    private static final String DISAGREE = "I don't think so! In my opinion, ";
    private static final String BUT = ", but ";
    private static final String ALSO = " also ";

    private final EntityClassifier entityClassifier;
    private final AspectStateClassifier aspectStateClassifier;
    private final DomainTextProvider textProvider;
    private final DomainOpinionGenerator opinionGenerator;

    @Inject
    public OpinionStatementGenerator(EntityClassifier entityClassifier, AspectStateClassifier aspectStateClassifier,
                                     DomainTextProvider textProvider, DomainOpinionGenerator opinionGenerator) {
        this.entityClassifier = entityClassifier;
        this.aspectStateClassifier = aspectStateClassifier;
        this.textProvider = textProvider;
        this.opinionGenerator = opinionGenerator;
    }

    public OpinionStatement generateOpinionStatement(OpinionStatement statement, List<OpinionUtterance> oldStatements,
                                                     Information information) {
        Entity entity = entityClassifier.getEntity(information, statement.getTarget());
        if(entity == null) {
            return null;
        }
        OpinionAspect aspect = statement.getAspect();
        OpinionContext context = statement.getContext();
        AspectState state = aspectStateClassifier.getAspectState(information, entity, aspect, context);
        aspect.setState(state);

        String aspectText = textProvider.getAspectText(aspect, context, state);
        String contextText = textProvider.getContextText(context);
        context.setInformation(contextText);

        String text;
        OpinionUtterance utterance = getOpinionUtterance(oldStatements, entity, statement.getTarget(),
                aspect, context);
        if (utterance != null) {
            if (utterance.getSpeaker() == Speaker.MACHINE) {
                text = LIKE_I_SAID_BEFORE + entity.getLabel() + SPACE
                        + aspectText + SPACE + contextText + PERIOD;
            } else {
                text = I_AGREE_YOU_EARLIER + entity.getLabel() + SPACE
                        + aspectText + SPACE + contextText + PERIOD;
            }
        } else {
            text = IMO + entity.getLabel() + SPACE + aspectText + SPACE + contextText + PERIOD;
        }

        return new OpinionStatement(text, entity.getLabel(), context, aspect);
    }

    public OpinionStatement generateResponseStatement(OpinionStatement statement,
                                                      List<OpinionUtterance> oldStatements,
                                                      Information information) {
        Entity entity = entityClassifier.getEntity(information, statement.getTarget());
        AspectState newState = aspectStateClassifier.getAspectState(information, entity,
                statement.getAspect(), statement.getContext());

        if (statesAreOpposite(newState, statement.getAspect().getState())) {
            OpinionStatement objectionStatement = generateStatement(entity, statement.getAspect(),
                    statement.getContext(), -1, statement.getAspect().getState(), information);
            objectionStatement.setText(DISAGREE + objectionStatement.getText());
            return objectionStatement;
        }
        if (entity != null) {
            return determineStatementToGenerate(statement, entity, information);
        } else {
            return null;
        }
    }

    public OpinionStatement generateStatement(Entity entity, OpinionAspect aspect, OpinionContext context,
                                              Information information) {
        return generateStatement(entity, aspect, context, -1, AspectState.AVERAGE, information);
    }

    private OpinionUtterance getOpinionUtterance(List<OpinionUtterance> oldStatements, Entity entity,
                                                 String target, OpinionAspect aspect, OpinionContext context) {
        for (OpinionUtterance opinionUtterance : oldStatements) {
            OpinionStatement statement = opinionUtterance.getOpinionStatement();
            if ((statement.getTarget().equals(target)
                    || statement.getTarget().equals(entity.getLabel()))
                    && statement.getAspect().equals(aspect) && statement.getContext().equals(context)) {
                return opinionUtterance;
            }
        }
        return null;
    }

    private OpinionStatement determineStatementToGenerate(OpinionStatement statement, Entity entity,
                                                          Information information) {
        OpinionContext context = statement.getContext();
        OpinionAspect aspect = statement.getAspect();
        Entity newEntity = entity;
        int facetToChange = -1;
        while(aspect == statement.getAspect() && context == statement.getContext()
                && newEntity.equals(entity)) {
            facetToChange = getFacetToChange(statement);
            if (facetToChange == 0) {
                newEntity = opinionGenerator.getRandomEntity(entity, information);
            } else if (facetToChange == 1) {
                aspect = opinionGenerator.getRandomAspect(entity);
            } else {
                context = opinionGenerator.getRandomContext(entity);
            }
        }
        return generateStatement(entity, aspect, context, facetToChange,
                statement.getAspect().getState(), information);
    }

    private int getFacetToChange(OpinionStatement statement) {
        int randomInt;
        if (statement.getContext().isUnknown()) {
            randomInt = 1;
        } else {
            randomInt = new Random().nextInt(3);
        }
        return randomInt;
    }

    private boolean statesAreOpposite(AspectState s1, AspectState s2) {
        return (s1 == AspectState.GOOD && s2 == AspectState.BAD)
                || (s1 == AspectState.BAD && s2 == AspectState.GOOD);
    }

    private OpinionStatement generateStatement(Entity entity, OpinionAspect aspect, OpinionContext context,
                                               int changedFacet, AspectState oldState, Information information) {
        AspectState newState = aspectStateClassifier.getAspectState(information, entity, aspect, context);
        aspect.setState(newState);
        String target = entity.getLabel();

        return generateOpinionStatementText(target, aspect, context, changedFacet, oldState, newState);
    }

    private OpinionStatement generateOpinionStatementText(String target, OpinionAspect aspect,
                                                          OpinionContext context, int changedFacet,
                                                          AspectState oldState, AspectState newState) {
        String aspectText = textProvider.getAspectText(aspect, context, newState);
        String contextText = textProvider.getContextText(context);
        context.setInformation(contextText);

        String text;
        String bridge = getRandomElement(AGREEMENTS) + getRandomElement(CONJUNCTIONS);
        if (changedFacet == 0) {
            if (statesAreOpposite(oldState, newState)) {
                text = getRandomElement(AGREEMENTS) + BUT + target + SPACE + aspectText + SPACE + contextText + PERIOD;
            } else {
                text = bridge + target + ALSO + aspectText + SPACE + contextText + PERIOD;
            }
        } else if (changedFacet == 1 || changedFacet == 2) {
            if (statesAreOpposite(oldState, newState)) {
                text = getRandomElement(AGREEMENTS) + BUT + target + SPACE + aspectText + SPACE + contextText + PERIOD;
            } else {
                text = bridge + target + SPACE + aspectText + SPACE + contextText + PERIOD;
            }
        } else {
            text = target + SPACE + aspectText + SPACE + contextText + PERIOD;
        }
        return new OpinionStatement(text, target, context, aspect);
    }

    private <T> T getRandomElement(T[] array) {
        return array[new Random().nextInt(array.length)];
    }
}
