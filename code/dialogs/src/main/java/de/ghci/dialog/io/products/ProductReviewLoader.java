package de.ghci.dialog.io.products;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.ghci.dialog.model.products.CellPhone;
import de.ghci.dialog.model.products.Product;
import de.ghci.dialog.model.products.ProductReview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Dominik
 */
public class ProductReviewLoader {

    private static final String REVIEWS_FILE = "C:\\Users\\Dominik\\Documents\\studium_master\\masterarbeit\\" +
            "workspace\\dialogs\\src\\main\\resources\\products_reviews.json";
    private static final String CATEGORY_CELL_PHONE = "Cell Phones";

    public Map<String, List<ProductReview>> getReviews() throws IOException {
        Map<String, List<ProductReview>> productReviews = new HashMap<>();
        final Gson gson = new Gson();
        try (Stream<String> stream = Files.lines(Paths.get(REVIEWS_FILE))) {
            stream.forEach(line -> {
                try {
                    ProductReview review = gson.fromJson(line, ProductReview.class);
                    List<ProductReview> reviewList = productReviews.get(review.getAsin());
                    if(reviewList == null) {
                        reviewList = new ArrayList<>();
                    }
                    reviewList.add(review);
                    productReviews.put(review.getAsin(), reviewList);
                } catch (JsonSyntaxException ignored) {
                }
            });
        }
        return productReviews;
    }
}
