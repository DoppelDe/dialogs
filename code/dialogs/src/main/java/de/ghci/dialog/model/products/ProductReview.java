package de.ghci.dialog.model.products;

import de.ghci.dialog.model.Entity;

import java.util.List;
import java.util.Objects;

/**
 * @author Dominik
 */
public class ProductReview {

    private String asin;
    private String reviewerID;
    private double overall;
    private String reviewText;
    private String summary;

    public ProductReview(String asin, String reviewerID, double overall, String reviewText, String summary) {
        this.asin = asin;
        this.reviewerID = reviewerID;
        this.overall = overall;
        this.reviewText = reviewText;
        this.summary = summary;
    }

    public String getAsin() {
        return asin;
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public double getOverall() {
        return overall;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductReview that = (ProductReview) o;
        return Objects.equals(asin, that.asin) &&
                Objects.equals(reviewerID, that.reviewerID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asin, reviewerID);
    }

    @Override
    public String toString() {
        return "ProductReview{" +
                "asin='" + asin + '\'' +
                ", reviewerID='" + reviewerID + '\'' +
                ", overall=" + overall +
                ", reviewText='" + reviewText + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }
}
