package de.ghci.dialog.process;

import com.google.inject.Singleton;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * @author Dominik
 */
@Singleton
public class BaseFormMatcher {

    private StanfordCoreNLP stanfordCoreNLP;

    public BaseFormMatcher() {
        initNLP();
    }

    public boolean verbMatches(String keyword, String match) {
        if(keyword == null) {
            return true;
        } else {
            return getVerbBaseForm(match).contains(keyword);
        }
    }

    public boolean matches(String keyword, String match) {
        if(keyword == null) {
            return true;
        } else {
            return getBaseForm(match).contains(keyword);
        }
    }

    public String getVerbBaseForm(String text) {
        initNLP();
        Annotation annotation = stanfordCoreNLP.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                char[] posAsChar = posTag.toCharArray();
                if (posAsChar[0] == 'V') {
                    return lemma;
                }
            }
        }
        return text;
    }

    public String getBaseForm(String text) {
        initNLP();
        Annotation annotation = stanfordCoreNLP.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        String returnText = "";
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                returnText += token.get(CoreAnnotations.LemmaAnnotation.class) + " ";
            }
        }
        return returnText;
    }

    private void initNLP() {
        if(stanfordCoreNLP == null) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
            stanfordCoreNLP = new StanfordCoreNLP(props);
        }
    }

}
