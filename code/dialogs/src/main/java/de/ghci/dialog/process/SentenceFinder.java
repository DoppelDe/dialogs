package de.ghci.dialog.process;

import com.google.inject.Inject;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;
import org.lambda3.graphene.core.relation_extraction.model.ExElement;
import org.lambda3.graphene.core.relation_extraction.model.ExSentence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dominik
 */
public class SentenceFinder {

    @Inject
    private ContainsMatcher containsMatcher;

    public List<String> getPossibleSentences(ExContent exContent, Collection<String> keywords) {
        List<String> possibleSentences = new ArrayList<>();
        for (String keyword : keywords) {
            possibleSentences.addAll(getSubjectSentences(exContent, keyword));
        }
        if (possibleSentences.isEmpty()) {
            for (String keyword : keywords) {
                possibleSentences.addAll(getObjectSentences(exContent, keyword));
            }
        }
        if (possibleSentences.isEmpty()) {
            for (String keyword : keywords) {
                possibleSentences.addAll(getContainingSentences(exContent, keyword));
            }
        }
        return possibleSentences;
    }

    public List<String> getPossibleSentences(ExContent exContent, String keyword) {
        List<String> keywords = new ArrayList<>();
        keywords.add(keyword);
        return getPossibleSentences(exContent, keywords);
    }

    private List<String> getContainingSentences(ExContent exContent, String keyword) {
        List<String> possibleSentences = new ArrayList<>();
        for (ExSentence sentence : exContent.getSentences()) {
            if (containsMatcher.matches(keyword, sentence.getOriginalSentence())) {
                possibleSentences.add(sentence.getOriginalSentence());
            }
        }
        return possibleSentences;
    }

    private List<String> getSubjectSentences(ExContent exContent, String keyword) {
        List<String> possibleSentences = new ArrayList<>();
        for (ExElement element : exContent.getElements()) {
            if (element.getSpo().isPresent()) {
                if (containsMatcher.matches(keyword, element.getSpo().get().getSubject())) {
                    possibleSentences.add(element.getNotSimplifiedText());
                }
            }
        }
        return possibleSentences;
    }

    private List<String> getObjectSentences(ExContent exContent, String keyword) {
        List<String> possibleSentences = new ArrayList<>();
        for (ExElement element : exContent.getElements()) {
            if (element.getSpo().isPresent()) {
                if (containsMatcher.matches(keyword, element.getSpo().get().getObject())) {
                    possibleSentences.add(element.getNotSimplifiedText());
                }
            }
        }
        return possibleSentences;
    }
}
