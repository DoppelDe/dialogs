package de.ghci.dialog.process;

import de.ghci.dialog.model.NamedEntity;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.Triple;

import java.util.*;

/**
 * @author Dominik
 */
public class NamedEntityClassifier {

    public static List<NamedEntity> getNamedEntities(String inputText) {
        List<NamedEntity> entities = new ArrayList<>();
        if(inputText == null || inputText.isEmpty()) {
            return entities;
        }
        List<Triple<String, Integer, Integer>> triples = CRFClassifier.getDefaultClassifier()
                .classifyToCharacterOffsets(inputText);
        for(Triple<String, Integer, Integer> triple : triples) {
            entities.add(new NamedEntity(triple.first(), inputText.substring(triple.second(), triple.third())));
        }
        return entities;
    }

}
