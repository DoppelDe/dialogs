package de.ghci.dialog.process;

import com.google.inject.Singleton;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Dominik
 */
@Singleton
public class CoReferenceResolution {

    private StanfordCoreNLP pipeline;

    public CoReferenceResolution() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
        pipeline = new StanfordCoreNLP(props);
    }

    public String doCoreference(String input) {
        Annotation doc = new Annotation(input);
        pipeline.annotate(doc);
        Map<Integer, CorefChain> corefs = doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> resolved = new ArrayList<>();

        for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);

                CorefChain chain = corefs.get(corefClustId);

                if (chain == null) {
                    resolved.add(token.word());
                } else {
                    int sentINdx = chain.getRepresentativeMention().sentNum - 1;
                    CoreMap corefSentence = sentences.get(sentINdx);
                    List<CoreLabel> corefSentenceTokens = corefSentence.get(CoreAnnotations.TokensAnnotation.class);

                    CorefChain.CorefMention reprMent = chain.getRepresentativeMention();
                    if (token.index() < reprMent.startIndex || token.index() > reprMent.endIndex) {
                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                            resolved.add(matchedLabel.word());
                        }
                    } else {
                        resolved.add(token.word());
                    }
                }
            }
        }


        String resolvedStr = "";
        for (String str : resolved) {
            resolvedStr += str + " ";
        }
        return resolvedStr;
    }


}
