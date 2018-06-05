package de.ghci.dialog.process.soccer;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.Sentiment;
import de.ghci.dialog.model.soccer.Person;
import de.ghci.dialog.model.soccer.PersonAspect;
import de.ghci.dialog.model.soccer.SoccerContext;
import de.ghci.dialog.model.soccer.TeamAspect;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.*;
import de.ghci.dialog.process.opinion.DomainStatementParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Optional;

/**
 * @author Dominik
 */
public class SoccerStatementParser implements DomainStatementParser {

    @Inject
    private StanfordCache stanfordCache;
    @Inject
    private SentimentClassifier sentimentClassifier;
    @Inject
    private SimilarWordFinder similarWordFinder;
    @Inject
    private BaseFormMatcher baseFormMatcher;

    @Override
    public OpinionAspect getOpinionAspect(String text, Entity entity) {
        if (entity instanceof Person) {
            PersonAspect aspect = PersonAspect.PERFORMANCE;
            aspect.setState(getAspectState(text));
            return aspect;
        } else {
            TeamAspect aspect = null;
            Annotation annotation = stanfordCache.process(text);
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap coreMap : sentences) {
                Tree tree = coreMap.get(TreeCoreAnnotations.TreeAnnotation.class);
                aspect = getAspect(tree);
            }
            if (aspect != null) {
                aspect.setState(getAspectState(text, aspect));
            }
            return aspect;
        }
    }

    @Override
    public OpinionContext getOpinionContext(String text, Entity entity) {
        OpinionContext context = null;
        Annotation annotation = stanfordCache.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap coreMap : sentences) {
            Tree tree = coreMap.get(TreeCoreAnnotations.TreeAnnotation.class);
            context = getContext(tree);
        }
        return context;
    }

    private AspectState getAspectState(String sentence) {
        Sentiment sentiment = sentimentClassifier.getSentiment(sentence);
        if (sentiment.isPositive()) {
            return AspectState.GOOD;
        } else if (sentiment.isNegative()) {
            return AspectState.BAD;
        }
        return AspectState.AVERAGE;
    }

    private AspectState getAspectState(String sentence, TeamAspect aspect) {
        Sentiment sentiment = sentimentClassifier.getSentiment(sentence);
        if (sentiment.isPositive()) {
            return AspectState.GOOD;
        } else if (sentiment.isNegative()) {
            return AspectState.BAD;
        }

        if (similarWordFinder.matchesSynonyms("many", sentence)
                || similarWordFinder.matchesSynonyms("a lot", sentence)) {
            if (sentence.contains("n't ") || sentence.contains(" not ")) {
                if (aspect == TeamAspect.FAIRNESS) {
                    return AspectState.GOOD;
                } else {
                    return AspectState.BAD;
                }
            } else if (aspect == TeamAspect.FAIRNESS) {
                return AspectState.BAD;
            } else {
                return AspectState.GOOD;
            }
        }
        return AspectState.AVERAGE;
    }

    private TeamAspect getAspect(Tree tree) {
        TeamAspect aspect = null;
        for (Tree leave : tree.getLeaves()) {
            Tree parent = leave.parent(tree);
            String posTag = parent.label().toString();
            if (posTag.startsWith("VB")) {
                aspect = getAspectFromVerb(aspect, leave);
            } else if (posTag.startsWith("NN")) {
                aspect = getAspectFromNoun(aspect, leave);
            } else if (posTag.startsWith("JJ")) {
                if (similarWordFinder.matchesSynonyms("fair", leave.toString())) {
                    aspect = TeamAspect.FAIRNESS;
                }
            }
        }
        return aspect;
    }

    private TeamAspect getAspectFromNoun(TeamAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("fairness", leave.toString())
                || similarWordFinder.matchesSynonyms("foul", leave.toString())
                || similarWordFinder.matchesSynonyms("card", leave.toString())) {
            aspect = TeamAspect.FAIRNESS;
        } else if (similarWordFinder.matchesSynonyms("goal", leave.toString())) {
            aspect = TeamAspect.GOALS;
        } else if (similarWordFinder.matchesSynonyms("victory", leave.toString())) {
            aspect = TeamAspect.WINS;
        } else if (similarWordFinder.matchesSynonyms("chance", leave.toString())) {
            aspect = TeamAspect.CHANCES;
        }
        return aspect;
    }

    private TeamAspect getAspectFromVerb(TeamAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("play", leave.toString())
                || similarWordFinder.matchesSynonyms("perform", leave.toString())
                || similarWordFinder.matchesSynonyms("be", leave.toString())) {
            aspect = TeamAspect.PERFORMANCE;
        } else if (similarWordFinder.matchesSynonyms("win", leave.toString())
                || similarWordFinder.matchesSynonyms("loose", leave.toString())) {
            aspect = TeamAspect.WINS;
        } else if (similarWordFinder.matchesSynonyms("score", leave.toString())) {
            aspect = TeamAspect.GOALS;
        }
        return aspect;
    }

    private SoccerContext getContext(Tree tree) {
        SoccerContext context = SoccerContext.UNKNOWN;
        for (Tree leave : tree.getLeaves()) {
            Tree parent = leave.parent(tree);
            String posTag = parent.label().toString();
            if (posTag.equals("IN")) {
                SoccerContext c = getInContext(tree, parent);
                if (c != SoccerContext.UNKNOWN) {
                    return c;
                }
            } else if (posTag.equals("NN")) {
                context = getKeywordContextSingular(context, leave, tree);
            } else if (posTag.equals("NNS")) {
                context = getKeywordContextPlural(context, leave, tree);
            } else if (posTag.equals("RB")) {
                context = getKeywordContextAdverb(context, leave);
            }
        }
        return context;
    }

    private SoccerContext getKeywordContextAdverb(SoccerContext context, Tree leave) {
        if (similarWordFinder.matchesSynonyms("ever", leave.toString())) {
            context = SoccerContext.ALL_TIME;
        }
        return context;
    }

    private String getNounPhrase(Tree leave, Tree tree) {
        while (!leave.parent(tree).label().toString().startsWith("NP")) {
            leave = leave.parent(tree);
        }
        return getSubtreeText(tree, leave);
    }

    private SoccerContext getKeywordContextPlural(SoccerContext context, Tree leave, Tree tree) {
        if (similarWordFinder.matchesSynonyms("year", leave.toString())
                || similarWordFinder.matchesSynonyms("season", leave.toString())) {
            context = SoccerContext.SEASONS;
            context.setInformation(getNounPhrase(leave, tree));
        } else if (similarWordFinder.matchesSynonyms("match", leave.toString())
                || similarWordFinder.matchesSynonyms("game", leave.toString())) {
            context = SoccerContext.MATCHES;
            context.setInformation(getNounPhrase(leave, tree));
        } else if (similarWordFinder.matchesSynonyms("time", leave.toString())
                && baseFormMatcher.matches("all", getNounPhrase(leave, tree))) {
            context = SoccerContext.ALL_TIME;
        }
        return context;
    }

    private SoccerContext getKeywordContextSingular(SoccerContext context, Tree leave, Tree tree) {
        if (similarWordFinder.matchesSynonyms("year", leave.toString())
                || similarWordFinder.matchesSynonyms("season", leave.toString())) {
            context = SoccerContext.SEASON;
            context.setInformation(getNounPhrase(leave, tree));
        } else if (similarWordFinder.matchesSynonyms("match", leave.toString())
                || similarWordFinder.matchesSynonyms("game", leave.toString())
                || similarWordFinder.matchesSynonyms("yesterday", leave.toString())
                || similarWordFinder.matchesSynonyms("today", leave.toString())
                || SoccerAspectStateClassifier.DAYS_OF_WEEK.contains(leave.toString())) {
            context = SoccerContext.MATCH;
            context.setInformation(getNounPhrase(leave, tree));
        } else if (similarWordFinder.matchesSynonyms("time", leave.toString())
                && baseFormMatcher.matches("all", getNounPhrase(leave, tree))) {
            context = SoccerContext.ALL_TIME;
        }
        return context;
    }

    private SoccerContext getInContext(Tree root, Tree parent) {
        SoccerContext context = SoccerContext.UNKNOWN;
        String subTreeText = getSubtreeText(root, parent);
        String subTreeLabels = getSubtreeLabels(root, parent);

        List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(subTreeText);
        if (namedEntities.stream()
                .filter(namedEntity -> namedEntity.getType().equals("ORGANIZATION"))
                .count() > 0) {
            context = SoccerContext.MATCH;
            context.setInformation(subTreeText);
            return context;
        } else if (getDayOfText(subTreeText) != null) {
            context = SoccerContext.MATCH;
            context.setInformation("on " + getDayOfText(subTreeText));
        } else if (subTreeLabels.contains("NNP") && namedEntities.size() == 0) {
            context = SoccerContext.COMPETITION;
            context.setInformation(subTreeText);
        }
        return context;
    }

    private String getDayOfText(String subTreeText) {
        for (String day : SoccerAspectStateClassifier.DAYS_OF_WEEK) {
            if (subTreeText.toLowerCase().contains(day)) {
                return day;
            }
        }
        return null;
    }

    private String getSubtreeText(Tree root, Tree subtree) {
        String subTreeText = null;
        Optional<String> optional = subtree.parent(root).getLeaves().stream()
                .map(t -> t.toString())
                .reduce((s1, s2) -> s1 + " " + s2);
        if (optional.isPresent()) {
            subTreeText = optional.get();
        }
        return subTreeText;
    }

    private String getSubtreeLabels(Tree tree, Tree subtree) {
        String subTreeText = null;
        Optional<String> optional = subtree.parent(tree).getLeaves().stream()
                .map(t -> t.parent(tree).label().toString())
                .reduce((s1, s2) -> s1 + " " + s2);
        if (optional.isPresent()) {
            subTreeText = optional.get();
        }
        return subTreeText;
    }
}
