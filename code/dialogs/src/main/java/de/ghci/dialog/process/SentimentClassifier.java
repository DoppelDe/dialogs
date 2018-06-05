package de.ghci.dialog.process;

import com.google.inject.Singleton;
import de.ghci.dialog.model.Sentiment;
import de.ghci.dialog.model.statement.AspectState;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * @author Dominik
 */
@Singleton
public class SentimentClassifier {

    public static final double DEFAULT_THRESHOLD_GOOD = 2.2;
    public static final double DEFAULT_THRESHOLD_BAD = 1.8;
    private final StanfordCoreNLP stanfordCoreNLP;

    public SentimentClassifier() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        stanfordCoreNLP = new StanfordCoreNLP(props);
    }

    public Sentiment getSentiment(String inputText) {
        return getSentimentOfScore(getSentimentScore(inputText));
    }

    public Sentiment getSentimentOfScore(float score) {
        return Sentiment.values()[Math.round(score)];
    }

    public float getSentimentScore(String inputText) {
        Annotation annotation = stanfordCoreNLP.process(inputText);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        int i = 0;
        int sentimentSum = 0;
        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            sentimentSum += RNNCoreAnnotations.getPredictedClass(tree);
            i++;
        }
        return 1.0f * sentimentSum / i;
    }

    public float getSentimentScoreOfSentences(List<String> sentences) {
        if (sentences.isEmpty()) {
            return 2;
        } else {
            return getSentimentScore(String.join(" ", sentences));
        }
    }

    public Sentiment getSentimentOfSentences(List<String> sentences) {
        if (sentences.isEmpty()) {
            return Sentiment.NEUTRAL;
        } else {
            Sentiment sentiment = getSentiment(String.join(" ", sentences));
            for (String sentence : sentences) {
                Sentiment sentenceSentiment = getSentiment(sentence);
                if (sentiment == sentenceSentiment
                        || (sentiment.isNegative() && sentenceSentiment.isNegative())
                        || (sentiment.isPositive() && sentenceSentiment.isPositive())) {
                    sentiment.setCause(sentence);
                    return sentiment;
                }
            }
            return sentiment;
        }
    }

    @SuppressWarnings("Duplicates")
    public AspectState getAspectStateOfSentimentScore(float average) {
        if(average > DEFAULT_THRESHOLD_GOOD) {
            return AspectState.GOOD;
        } else if (average < DEFAULT_THRESHOLD_BAD) {
            return AspectState.BAD;
        } else {
            return AspectState.AVERAGE;
        }
    }
}
