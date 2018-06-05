package de.ghci.dialog.process;

import com.google.inject.Singleton;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Dominik
 */
@Singleton
public class StanfordCache {

    private StanfordCoreNLP stanfordCoreNLP;
    private Map<String, Annotation> cache = new HashMap<>();

    public StanfordCache() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
        stanfordCoreNLP = new StanfordCoreNLP(props);
    }

    public Annotation process(String text) {
        if (cache.containsKey(text)) {
            return cache.get(text);
        } else {
            Annotation annotation = stanfordCoreNLP.process(text);
            cache.put(text, annotation);
            return annotation;
        }
    }
}
