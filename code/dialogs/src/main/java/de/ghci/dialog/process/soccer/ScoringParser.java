package de.ghci.dialog.process.soccer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.ghci.dialog.model.soccer.ScoringIndicator;
import de.ghci.dialog.model.WordTagPair;
import de.ghci.dialog.model.soccer.Person;
import de.ghci.dialog.model.soccer.ScoreEvent;
import de.ghci.dialog.process.BaseFormMatcher;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * @author Dominik
 */
@Singleton
public class ScoringParser {

    @Inject
    private BaseFormMatcher baseFormMatcher;

    private List<ScoringIndicator> indicators;

    public ScoringParser() {
        indicators = new ArrayList<>();
        indicators.add(new ScoringIndicator(new WordTagPair("V", "score")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", "tap"), new WordTagPair("IN", "in", "past")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", "double", "increase", "give"),
                new WordTagPair("NN", "lead")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", "break"), new WordTagPair("NN", "deadlock")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", "send"), new WordTagPair("NN", "keeper"),
                new WordTagPair("wrong", "JJ")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", "draw"), new WordTagPair("first", "JJ"),
                new WordTagPair("NN", "blood")));
        String[] hitStrings = {"head", "nod", "hit", "slid", "blast", "curl",
                "crash", "power", "sailed", "slot", "fire", "poke"};
        indicators.add(new ScoringIndicator(new WordTagPair("V", hitStrings), new WordTagPair("NN", "home")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", hitStrings), new WordTagPair("NN", "keeper"),
                new WordTagPair("IN", "past")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", hitStrings), new WordTagPair("NN", "net", "corner"),
                new WordTagPair("IN", "into")));
        indicators.add(new ScoringIndicator(new WordTagPair("V", "put"), new WordTagPair("IN", "in"),
                new WordTagPair("NN", "front")));
    }

    private List<Person> getScorerInSubTrees(Tree tree) {
        List<Person> scorer = new ArrayList<>();
        if (tree.value().equals("S")) {
            if (tree.toString().contains(" (S (")) {
                addScorers(scorer, getLeavesNotInSubTree(tree), tree);
            } else {
                addScoresOfLeaves(tree, scorer);
            }
        }
        for (Tree child : tree.children()) {
            scorer.addAll(getScorerInSubTrees(child));
        }
        return scorer;
    }

    public List<ScoreEvent> getScores(String text) {
        List<ScoreEvent> scores = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
        StanfordCoreNLP stanfordCoreNLP = new StanfordCoreNLP(props);

        Annotation annotation = stanfordCoreNLP.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            for(Person p : getScorerInSubTrees(tree)) {
                scores.add(new ScoreEvent(sentence.toString(), p));
            }
        }
        return scores;
    }

    private List<Tree> getLeavesNotInSubTree(Tree tree) {
        List<Tree> leaves = new ArrayList<>();
        for (Tree child : tree.children()) {
            if (!child.value().equals("S")) {
                leaves.addAll(getLeavesNotInSubTree(child));
            }
        }
        if (tree.isLeaf()) {
            leaves.add(tree);
        }
        return leaves;
    }

    private void addScoresOfLeaves(Tree tree, List<Person> scorer) {
        List<Tree> leaves = tree.getLeaves(); //leaves correspond to the tokens
        addScorers(scorer, leaves, tree);
    }

    private void addScorers(List<Person> scorer, List<Tree> nodeList, Tree root) {
        String names = "";
        List<WordTagPair> pairs = new ArrayList<>();
        Set<Person> namedEntities = new HashSet<>();
        for (Tree leaf : nodeList) {
            Tree parent = leaf.parent(root);
            if (parent.value().equals("NNP")) {
                names += leaf.value() + " ";
            } else if(!names.isEmpty()) {
                namedEntities.addAll(new SoccerEntityClassifier().getPersons(names));
                names = "";
            }
            pairs.add(new WordTagPair(parent.value(), leaf.value()));
        }

        for (ScoringIndicator si : indicators) {
            if (indicatorMatches(pairs, si)) {
                for (Person person : namedEntities) {
                    scorer.add(person);
                }
            }
        }
    }

    private boolean indicatorMatches(List<WordTagPair> pairs, ScoringIndicator si) {
        for(WordTagPair pair : si.getPairs()) {
            if(!containsPair(pairs, pair)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsPair(List<WordTagPair> pairs, WordTagPair pair) {
        for (WordTagPair p : pairs) {
            if (p.getTag().startsWith(pair.getTag()) && matchesWord(pair, p)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesWord(WordTagPair pair, WordTagPair p) {
        for(String w : pair.getWords()) {
            if(baseFormMatcher.verbMatches(w, p.getWord())) {
                return true;
            }
        }
        return false;
    }
}
