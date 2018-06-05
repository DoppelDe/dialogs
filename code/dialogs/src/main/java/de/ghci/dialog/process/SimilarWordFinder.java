package de.ghci.dialog.process;

import com.fasterxml.jackson.databind.deser.Deserializers;
import com.google.inject.Inject;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominik
 */
public class SimilarWordFinder {

    @Inject
    private BaseFormMatcher baseFormMatcher;

    private static Word2Vec vec = null;

    public boolean matchesSynonyms(String keyWord, String toMatch) {
        for(String synonym : getSynonyms(keyWord)) {
            if(baseFormMatcher.matches(synonym, toMatch)) {
                return true;
            }
        }
        return false;
    }

    private Collection<String> getSynonyms(String word) {
        File f = new File("lib\\WordNet\\2.1\\dict");
        System.setProperty("wordnet.database.dir", f.toString());

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(word);

        Set<String> strings = new HashSet<>();
        if (synsets.length > 0) {
            for (int i = 0; i < synsets.length; i++) {
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    strings.add(wordForms[j]);
                }
            }
        } else {
            strings.add(word);
        }
        return strings;
    }

}
