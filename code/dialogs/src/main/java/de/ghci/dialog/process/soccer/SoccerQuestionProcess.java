package de.ghci.dialog.process.soccer;

import com.google.inject.Inject;
import de.ghci.dialog.io.soccer.SoccerDataLoader;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.Sentiment;
import de.ghci.dialog.model.dialog.*;
import de.ghci.dialog.model.soccer.*;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.model.statement.OpinionStatement;
import de.ghci.dialog.process.ContainsMatcher;
import de.ghci.dialog.process.QuestionProcess;
import de.ghci.dialog.process.SentenceFinder;
import de.ghci.dialog.process.SentimentClassifier;
import de.ghci.dialog.process.opinion.*;
import org.nd4j.linalg.io.Assert;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
public class SoccerQuestionProcess implements QuestionProcess {

    @Inject
    private OpinionStatementGenerator opinionGenerator;
    @Inject
    private OpinionProcess opinionProcess;
    @Inject
    private EntityClassifier entityClassifier;
    @Inject
    private StatementParser statementParser;
    @Inject
    private ContainsMatcher containsMatcher;
    @Inject
    private SentimentClassifier sentimentClassifier;
    @Inject
    private SoccerDataLoader soccerDataLoader;
    @Inject
    private SentenceFinder sentenceFinder;

    @Override
    public Utterance handleQuestionAnswer(Question question, String response, Dialog dialog) {
        Assert.isTrue(question instanceof SoccerQuestion);
        SoccerQuestion asked = (SoccerQuestion) question;
        Sentiment sentiment = sentimentClassifier.getSentiment(response);
        Utterance machineAnswer;
        switch (asked) {
            case REFEREE:
                machineAnswer = handleRefereeAnswer(response, sentiment, dialog.getInformation());
                break;
            case FAVORITE_PLAYER:
                machineAnswer = handlePlayerAnswer(response, SoccerContext.SEASON, dialog);
                break;
            case FAVORITE_TEAM:
                machineAnswer = handleTeamAnswer(response, dialog);
                break;
            case BEST_PLAYER:
                machineAnswer = handlePlayerAnswer(response, SoccerContext.MATCH, dialog);
                break;
            case HOW_PLAYED_TEAM:
                machineAnswer = handleHowPlayedTeam(response, SoccerContext.MATCH, dialog, true);
                break;
            case HOW_PLAYED_TEAM_SEASON:
                machineAnswer = handleHowPlayedTeam(response, SoccerContext.SEASON, dialog, true);
                break;
            case CARD_DESERVED:
                machineAnswer = handleCardDeservedAnswer(response, sentiment, dialog);
                break;
            default:
                machineAnswer = machineUtteranceFromText("I forgot what I asked, do you remember?");
        }

        return machineAnswer;
    }

    private Utterance handlePlayerAnswer(String text, OpinionContext context, Dialog dialog) {
        Set<Entity> entities = entityClassifier.getEntities(dialog.getInformation(), text);
        if (entities.isEmpty()) {
            text = capitalizeText(text);
            entities = entityClassifier.getEntities(dialog.getInformation(), capitalizeText(text));
        }

        if (entities.isEmpty()) {
            return machineUtteranceFromText("What do you like about him?");
        }

        if (entities.size() > 1) {
            return machineUtteranceFromText("I asked for only one player!");
        }

        for (Entity entity : entities) { // Always only one player
            OpinionStatement opinionStatement = opinionGenerator.generateStatement(entity, PersonAspect.PERFORMANCE,
                    context, dialog.getInformation());
            return opinionProcess.getOpinionUtterance(opinionStatement, dialog);
        }
        throw new IllegalStateException();
    }

    private Utterance handleTeamAnswer(String text, Dialog dialog) {
        Entity entity = entityClassifier.getEntity(dialog.getInformation(), text);

        if (entity == null || !(entity instanceof Team)) {
            return machineUtteranceFromText("I don't know this team. In which league do they play?");
        }

        OpinionStatement opinionStatement = opinionGenerator.generateStatement(entity, TeamAspect.PERFORMANCE,
                SoccerContext.SEASON, dialog.getInformation());
        return opinionProcess.getOpinionUtterance(opinionStatement, dialog);
    }

    private String capitalizeText(String text) {
        StringBuilder builder = new StringBuilder();
        String[] tokens = text.split(" ");
        for (String token : tokens) {
            if (token.length() > 0) {
                builder.append(token.substring(0, 1).toUpperCase());
                builder.append(token.substring(1, token.length()));
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    private Utterance machineUtteranceFromText(String text) {
        return new Utterance(text, Speaker.MACHINE);
    }


    private Utterance handleRefereeAnswer(String text, Sentiment sentiment, Information information) {
        MatchInformation matchInformation = ((SoccerInformation) information).getCurrentMatch();
        Sentiment refSentiment = getRefereeSentiment(matchInformation);
        String name = "the referee";
        if (matchInformation.getReferee() != null) {
            name = matchInformation.getReferee().getName();
        }
        if (sentiment.isPositive() || containsMatcher.matchesWord("yes", text)) {
            if (refSentiment.isNegative()) {
                return addCause("I don't think so, " + name + " made a lot of mistakes.", refSentiment);
            } else {
                return addCause("I also think " + name + " was good.", refSentiment);
            }
        } else if (sentiment.isNegative() || containsMatcher.matchesWord("no", text)) {
            if (refSentiment.isPositive()) {
                return addCause("Don't blame " + name + ", he did fine!", refSentiment);
            } else {
                return addCause("I agree, " + name + " was bad.", refSentiment);
            }
        } else {
            return machineUtteranceFromText("I don't understand you, did you understand my question?");
        }
    }

    private Utterance handleCardDeservedAnswer(String response, Sentiment sentiment, Dialog dialog) {
        SoccerInformation information = (SoccerInformation) dialog.getInformation();
        MatchInformation matchInformation = information.getCurrentMatch();
        Sentiment refSentiment = getRefereeSentiment(matchInformation);
        String name = "the referee";
        if (matchInformation.getReferee() != null) {
            name = matchInformation.getReferee().getName();
        }
        if (sentiment.isPositive() || containsMatcher.matchesWord("yes", response)) {
            if (refSentiment.isNegative()) {
                return addCause("I also think so, but " + name + " made a lot of mistakes in general.", refSentiment);
            } else {
                name = capitalizeFirstLetter(name);
                return addCause("I agree. " + name + " did very well overall.", refSentiment);
            }
        } else if (sentiment.isNegative() || containsMatcher.matchesWord("no", response)) {
            if (refSentiment.isPositive()) {
                return addCause("Yes, but i think that was the only mistake of " + name + ".", refSentiment);
            } else {
                name = capitalizeFirstLetter(name);
                return addCause("Yes. " + name + " was very bad in general.", refSentiment);
            }
        } else {
            return machineUtteranceFromText("I don't understand you, did you understand my question?");
        }
    }

    private String capitalizeFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private Sentiment getRefereeSentiment(MatchInformation matchInformation) {
        List<String> sentences = sentenceFinder.getPossibleSentences(matchInformation.getExContent(), "referee");
        Referee referee = matchInformation.getReferee();
        if (referee != null) {
            sentences.addAll(sentenceFinder.getPossibleSentences(matchInformation.getExContent(),
                    referee.getLastName()));
        }
        return sentimentClassifier.getSentimentOfSentences(sentences);
    }

    private Utterance addCause(String answer, Sentiment sentiment) {
        if (sentiment.getCause() != null) {
            return machineUtteranceFromText(answer + " " + sentiment.getCause());
        } else {
            return machineUtteranceFromText(answer);
        }
    }

    private Utterance handleHowPlayedTeam(String text, SoccerContext context, Dialog dialog, boolean firstTime) {
        Assert.isTrue(dialog.getInformation() instanceof SoccerInformation);
        SoccerInformation information = (SoccerInformation) dialog.getInformation();
        OpinionStatement statement = statementParser.parse(text, dialog.getInformation());

        if (opinionProcess.isOpinionStatement(statement)) {
            if (statement.getContext().equals(SoccerContext.MATCH) && context == SoccerContext.MATCH) {
                statement.getContext().setInformation(information.getCurrentMatch()
                        .getMatchDate().format(SoccerAspectStateClassifier.DATE_PATTERN));
            } else if (statement.getContext().isUnknown()) {
                statement.setContext(context);
            }
            return opinionProcess.handleOpinionStatement(statement, dialog);
        } else if (firstTime) {
            System.out.println(information.getAskedTarget());
            text = information.getAskedTarget().getName() + text;
            return handleHowPlayedTeam(text, context, dialog, false);
        } else {
            return null;
        }
    }


    @Override
    public AskedQuestion askQuestion(Dialog dialog) {
        Assert.isTrue(dialog.getInformation() instanceof SoccerInformation);

        Set<SoccerQuestion> availableQuestions = getAvailableQuestions(dialog);
        if (availableQuestions.size() > 0) {
            SoccerQuestion question = availableQuestions
                    .toArray(new SoccerQuestion[]{})[new Random().nextInt(availableQuestions.size())];
            return createQuestion(dialog, question);
        } else if (getAvailableTeams((SoccerInformation) dialog.getInformation()).size() > 0) {
            SoccerQuestion question = SoccerQuestion.HOW_PLAYED_TEAM_SEASON;
            return createQuestion(dialog, question);
        } else {
            return null;
        }
    }

    private Team getNewRandomTeam(SoccerInformation information) {
        List<Team> availableTeams = getAvailableTeams(information);
        if (availableTeams.size() > 0) {
            Team team = availableTeams.get(new Random().nextInt(availableTeams.size()));
            information.addHandledTeam(team);
            return team;
        } else {
            return null;
        }
    }

    private CardEvent getNewRandomCardEvent(Dialog dialog) {
        List<CardEvent> availableEvents = getAvailableCardEvents(dialog);
        if (availableEvents.size() > 0) {
            return getRandomElement(availableEvents);
        } else {
            return null;
        }
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    private List<CardEvent> getAvailableCardEvents(Dialog dialog) {
        return getAvailableEvents(dialog).stream()
                .filter(e -> (e instanceof CardEvent))
                .map(e -> (CardEvent) e)
                .collect(Collectors.toList());
    }

    public List<Event> getAvailableEvents(Dialog dialog) {
        return dialog.getInformation().getEvents()
                .stream().filter(e -> !dialog.getMentionedEvents().contains(e))
                .collect(Collectors.toList());
    }

    private List<Team> getAvailableTeams(SoccerInformation information) {
        try {
            return soccerDataLoader.getCurrentBundesligaData().getTeams().stream()
                    .filter(t -> !information.getHandledTeams().contains(t)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Set<SoccerQuestion> getAvailableQuestions(Dialog dialog) {
        Set<SoccerQuestion> availableQuestions = new HashSet<>();
        Collections.addAll(availableQuestions, SoccerQuestion.values());
        for (Utterance utterance : dialog.getUtterances()) {
            removeQuestion(availableQuestions, utterance);
        }
        if (getAvailableCardEvents(dialog).size() == 0) {
            availableQuestions.remove(SoccerQuestion.CARD_DESERVED);
        }
        if(((SoccerInformation) dialog.getInformation()).getCurrentMatch().getAwayTeam() == null
                || ((SoccerInformation) dialog.getInformation()).getCurrentMatch().getHomeTeam() == null) {
            availableQuestions.remove(SoccerQuestion.REFEREE);
            availableQuestions.remove(SoccerQuestion.BEST_PLAYER);
        }
        return availableQuestions;
    }

    private void removeQuestion(Set<SoccerQuestion> availableQuestions, Utterance utterance) {
        if (utterance instanceof AskedQuestion) {
            availableQuestions.remove(((AskedQuestion) utterance).getQuestion());
        } else if (utterance instanceof CombinedUtterance) {
            for (Utterance ut : ((CombinedUtterance) utterance).getUtterances()) {
                removeQuestion(availableQuestions, ut);
            }
        }
    }

    private AskedQuestion createQuestion(Dialog dialog, SoccerQuestion asked) {
        Assert.isTrue(dialog.getInformation() instanceof SoccerInformation);
        SoccerInformation information = (SoccerInformation) dialog.getInformation();
        if (asked == SoccerQuestion.BEST_PLAYER || asked == SoccerQuestion.REFEREE) {
            return new AskedQuestion(asked, information.getCurrentMatch().getHomeTeam().getLabel(),
                    information.getCurrentMatch().getAwayTeam().getLabel());
        } else if (asked == SoccerQuestion.HOW_PLAYED_TEAM_SEASON) {
            Team randomTeam = getNewRandomTeam(information);
            information.setAskedTarget(randomTeam);
            return new AskedQuestion(asked, randomTeam.getLabel());
        } else if (asked == SoccerQuestion.CARD_DESERVED) {
            return createCardQuestion(dialog, asked);
        } else if (asked == SoccerQuestion.HOW_PLAYED_TEAM) {
            Team team = getHomeOrAwayTeam(information);
            information.setAskedTarget(team);
            return new AskedQuestion(asked, team.getLabel());
        } else {
            return new AskedQuestion(asked);
        }
    }

    private Team getHomeOrAwayTeam(SoccerInformation information) {
        Team team;
        if (new Random().nextBoolean()) {
            team = information.getCurrentMatch().getHomeTeam();
        } else {
            team = information.getCurrentMatch().getAwayTeam();
        }
        return team;
    }

    private AskedQuestion createCardQuestion(Dialog dialog, SoccerQuestion asked) {
        CardEvent cardEvent = getNewRandomCardEvent(dialog);
        return new AskedQuestion(asked, cardEvent.getPlayer().getLabel(),
                cardEvent.getType().getDisplayText());
    }
}
