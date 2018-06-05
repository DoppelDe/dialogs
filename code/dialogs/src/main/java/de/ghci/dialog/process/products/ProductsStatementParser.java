package de.ghci.dialog.process.products;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Sentiment;
import de.ghci.dialog.model.products.*;
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
import org.nd4j.linalg.io.Assert;

import java.util.List;
import java.util.Optional;

/**
 * @author Dominik
 */
public class ProductsStatementParser implements DomainStatementParser {

    @Inject
    private StanfordCache stanfordCache;
    @Inject
    private SentimentClassifier sentimentClassifier;
    @Inject
    private SimilarWordFinder similarWordFinder;

    @Override
    public OpinionAspect getOpinionAspect(String text, Entity entity) {
        Assert.isInstanceOf(Product.class, entity);
        OpinionAspect aspect = null;
        Annotation annotation = stanfordCache.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap coreMap : sentences) {
            Tree tree = coreMap.get(TreeCoreAnnotations.TreeAnnotation.class);
            aspect = getAspectOfProduct(tree, (Product) entity);
        }
        if (aspect != null) {
            aspect.setState(getAspectState(text, aspect));
        }
        return aspect;
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

    private AspectState getAspectState(String sentence, OpinionAspect aspect) {
        Sentiment sentiment = sentimentClassifier.getSentiment(sentence);
        if (sentiment.isPositive()) {
            return AspectState.GOOD;
        } else if (sentiment.isNegative()) {
            return AspectState.BAD;
        }
        if (similarWordFinder.matchesSynonyms("many", sentence)
                || similarWordFinder.matchesSynonyms("a lot", sentence)) {
            if (sentence.contains("n't ") || sentence.contains(" not ")) {
                if (isPriceAspect(aspect)) {
                    return AspectState.GOOD;
                } else {
                    return AspectState.BAD;
                }
            } else if (isPriceAspect(aspect)) {
                return AspectState.BAD;
            } else {
                return AspectState.GOOD;
            }
        }
        return AspectState.AVERAGE;
    }

    private boolean isPriceAspect(OpinionAspect aspect) {
        return aspect == ProductAspect.PRICE || aspect == CellPhoneAspect.PRICE || aspect == PowerBankAspect.PRICE;
    }

    private OpinionAspect getAspectOfProduct(Tree tree, Product product) {
        OpinionAspect aspect = null;
        for (Tree leave : tree.getLeaves()) {
            Tree parent = leave.parent(tree);
            String posTag = parent.label().toString();
            if (posTag.startsWith("VB")) {
                aspect = getAspectFromVerb(aspect, leave, product);
            } else if (posTag.startsWith("NN")) {
                aspect = getAspectFromNoun(aspect, leave, product);
            }
        }
        return aspect;
    }

    private OpinionAspect getCPAspectFromNoun(OpinionAspect aspect, Tree leave) {
       if (similarWordFinder.matchesSynonyms("display", leave.toString())) {
            aspect = CellPhoneAspect.DISPLAY;
        } else if (similarWordFinder.matchesSynonyms("battery", leave.toString())
                || similarWordFinder.matchesSynonyms("power", leave.toString())
                || similarWordFinder.matchesSynonyms("life", leave.toString())) {
            aspect = CellPhoneAspect.BATTERY;
        } else if (similarWordFinder.matchesSynonyms("price", leave.toString())) {
            aspect = CellPhoneAspect.PRICE;
        }
        return aspect;
    }

    private OpinionAspect getCPAspectFromVerb(OpinionAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("perform", leave.toString())
                || similarWordFinder.matchesSynonyms("be", leave.toString())) {
            aspect = CellPhoneAspect.PERFORMANCE;
        }
        return aspect;
    }

    private OpinionAspect getPBAspectFromNoun(OpinionAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("battery", leave.toString())
                || similarWordFinder.matchesSynonyms("power", leave.toString())
                || similarWordFinder.matchesSynonyms("life", leave.toString())) {
            aspect = PowerBankAspect.POWER;
        } else if (similarWordFinder.matchesSynonyms("price", leave.toString())) {
            aspect = PowerBankAspect.PRICE;
        }
        return aspect;
    }

    private OpinionAspect getPBAspectFromVerb(OpinionAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("perform", leave.toString())
                || similarWordFinder.matchesSynonyms("be", leave.toString())) {
            aspect = PowerBankAspect.PERFORMANCE;
        }
        return aspect;
    }

    private OpinionAspect getAspectFromNoun(OpinionAspect aspect, Tree leave, Product product) {
        if (product instanceof CellPhone) {
            aspect = getCPAspectFromNoun(aspect, leave);
        } else if (product instanceof PowerBank) {
            aspect = getPBAspectFromNoun(aspect, leave);
        } else {
            aspect = getProductAspectFromNoun(aspect, leave);
        }
        return aspect;
    }

    private OpinionAspect getProductAspectFromNoun(OpinionAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("performance", leave.toString())) {
            aspect = ProductAspect.PERFORMANCE;
        } else if (similarWordFinder.matchesSynonyms("price", leave.toString())) {
            aspect = ProductAspect.PRICE;
        }
        return aspect;
    }

    private OpinionAspect getAspectFromVerb(OpinionAspect aspect, Tree leave, Product product) {
        if (product instanceof CellPhone) {
            aspect = getCPAspectFromVerb(aspect, leave);
        } else if (product instanceof PowerBank) {
            aspect = getPBAspectFromVerb(aspect, leave);
        } else {
            aspect = getProductAspectFromVerb(aspect, leave);
        }
        return aspect;
    }

    private OpinionAspect getProductAspectFromVerb(OpinionAspect aspect, Tree leave) {
        if (similarWordFinder.matchesSynonyms("play", leave.toString())
                || similarWordFinder.matchesSynonyms("perform", leave.toString())
                || similarWordFinder.matchesSynonyms("be", leave.toString())) {
            aspect = ProductAspect.PERFORMANCE;
        }
        return aspect;
    }

    private ProductContext getContext(Tree tree) {
        ProductContext context = ProductContext.UNKNOWN;
        for (Tree leave : tree.getLeaves()) {
            Tree parent = leave.parent(tree);
            String posTag = parent.label().toString();
            if (posTag.startsWith("NN")) {
                context = getKeywordContextNoun(context, leave, tree);
            }
        }
        return context;
    }

    private String getNounPhrase(Tree leave, Tree tree) {
        while (!leave.parent(tree).label().toString().startsWith("NP")) {
            leave = leave.parent(tree);
        }
        return getSubtreeText(tree, leave);
    }

    private ProductContext getKeywordContextNoun(ProductContext context, Tree leave, Tree tree) {
        if (similarWordFinder.matchesSynonyms("all", leave.toString())) {
            context = ProductContext.GENERAL;
            context.setInformation(getNounPhrase(leave, tree));
        } else if (similarWordFinder.matchesSynonyms("this", leave.toString())) {
            context = ProductContext.SPECIFIC;
            context.setInformation(getNounPhrase(leave, tree));
        }
        return context;
    }

    private String getSubtreeText(Tree root, Tree subtree) {
        String subTreeText = null;
        Optional<String> optional = subtree.parent(root).getLeaves().stream()
                .map(Tree::toString)
                .reduce((s1, s2) -> s1 + " " + s2);
        if (optional.isPresent()) {
            subTreeText = optional.get();
        }
        return subTreeText;
    }
}
