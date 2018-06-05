package de.ghci.dialog.process.products;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.products.*;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.*;
import de.ghci.dialog.process.opinion.AspectStateClassifier;
import org.nd4j.linalg.io.Assert;

import java.util.*;

/**
 * @author Dominik
 */
public class ProductsAspectStateClassifier implements AspectStateClassifier {

    private static final double AVERAGE_GOOD_THRESHOLD = 4;
    private static final double AVERAGE_BAD_THRESHOLD = 3;

    @Inject
    private SentimentClassifier sentimentClassifier;
    @Inject
    private SentenceFinder sentenceFinder;
    @Inject
    private RelationParser relationParser;

    @Override
    public AspectState getAspectState(Information information, Entity entity, OpinionAspect aspect,
                                      OpinionContext context) {
        Assert.isTrue(information instanceof ProductsInformation);
        Assert.isTrue(aspect instanceof ProductAspect || aspect instanceof CellPhoneAspect
                || aspect instanceof PowerBankAspect);
        Assert.isTrue(context instanceof ProductContext);
        Assert.isTrue(entity instanceof Product);
        Assert.isTrue(!aspect.isUnknown());

        if(isOverallPerformanceAspect(aspect)) {
            return getPerformanceOfProduct((ProductsInformation) information, (Product) entity);
        } else if(entity instanceof Product) {
            return getAspectStateOfProduct((ProductsInformation) information, (Product) entity, aspect);
        }
        throw new IllegalStateException();
    }

    protected boolean isOverallPerformanceAspect(OpinionAspect aspect) {
        return aspect == CellPhoneAspect.PERFORMANCE || aspect == ProductAspect.PERFORMANCE
                || aspect == PowerBankAspect.PERFORMANCE;
    }

    @SuppressWarnings("Duplicates")
    private AspectState getPerformanceOfProduct(ProductsInformation information, Product product) {
        List<ProductReview> reviews = information.getReviews().get(product.getAsin());
        double sum = 0.0;
        for(ProductReview review : reviews) {
            sum += review.getOverall();
        }
        double average = sum / reviews.size();

        if(average > AVERAGE_GOOD_THRESHOLD) {
            return AspectState.GOOD;
        } else if (average < AVERAGE_BAD_THRESHOLD) {
            return AspectState.BAD;
        } else {
            return AspectState.AVERAGE;
        }
    }

    private AspectState getAspectStateOfProduct(ProductsInformation information, Product product,
                                                OpinionAspect aspect) {
        List<ProductReview> reviews = information.getReviews().get(product.getAsin());
        float scoreSum = 0.0f;
        int count = 0;
        for(ProductReview review : reviews) {
            List<String> possibleSentences = sentenceFinder.getPossibleSentences(
                    relationParser.parse(review.getReviewText()), getKeyWordsOfAspect(aspect));
            if(!possibleSentences.isEmpty()) {
                scoreSum += sentimentClassifier.getSentimentScoreOfSentences(possibleSentences);
                count++;
            }
        }
        float average = scoreSum / count;
        return sentimentClassifier.getAspectStateOfSentimentScore(average);
    }

    private Collection<String> getKeyWordsOfAspect(OpinionAspect aspect) {
        if(aspect instanceof CellPhoneAspect) {
            return getKeyWordsOfCellPhoneAspect((CellPhoneAspect) aspect);
        } else if(aspect instanceof PowerBankAspect) {
            return getKeyWordsOfPowerBankAspect((PowerBankAspect) aspect);
        } else if(aspect instanceof ProductAspect) {
            return getKeyWordsOfProductAspect((ProductAspect) aspect);
        }
        return null;
    }

    private Collection<String> getKeyWordsOfProductAspect(ProductAspect aspect) {
        ArrayList<String> keywords = new ArrayList<>();
        switch (aspect) {
            case PRICE:
                addPriceKeywords(keywords);
                break;
            case PERFORMANCE:
            case UNKNOWN:
                throw new IllegalArgumentException();
        }
        return keywords;
    }

    private Collection<String> getKeyWordsOfCellPhoneAspect(CellPhoneAspect aspect) {
        ArrayList<String> keywords = new ArrayList<>();
        switch (aspect) {
            case PRICE:
                addPriceKeywords(keywords);
                break;
            case DISPLAY:
                keywords.add("display");
                break;
            case BATTERY:
                keywords.add("battery");
                keywords.add("power");
                keywords.add("life");
                break;
            case PERFORMANCE:
            case UNKNOWN:
                throw new IllegalArgumentException();
        }
        return keywords;
    }

    private Collection<String> getKeyWordsOfPowerBankAspect(PowerBankAspect aspect) {
        ArrayList<String> keywords = new ArrayList<>();
        switch (aspect) {
            case PRICE:
                addPriceKeywords(keywords);
                break;
            case POWER:
                keywords.add("battery");
                keywords.add("power");
                keywords.add("life");
                break;
            case PERFORMANCE:
            case UNKNOWN:
                throw new IllegalArgumentException();
        }
        return keywords;
    }

    private void addPriceKeywords(ArrayList<String> keywords) {
        keywords.add("price");
        keywords.add("cost");
        keywords.add("expensive");
        keywords.add("cheap");
    }

}
