package de.ghci.dialog.model.products;

import com.google.inject.Inject;
import de.ghci.dialog.io.products.ProductReviewLoader;
import de.ghci.dialog.io.products.ProductsLoader;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.Information;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
public class ProductsInformation implements Information{

    private Map<String, Product> products;
    private Map<String, List<ProductReview>> reviews;

    @Inject
    public ProductsInformation(ProductsLoader productsLoader, ProductReviewLoader productReviewLoader)
            throws IOException, ClassNotFoundException {
        products = productsLoader.getProducts();
        reviews = productReviewLoader.getReviews();
    }

    public List<CellPhone> getCellPhones() {
        return products.values().stream()
                .filter(p -> p instanceof CellPhone)
                .map(p -> (CellPhone) p)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Event> getEvents() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Entity> getEntities() {
        Collection<Entity> entities = new ArrayList<>();
        entities.addAll(products.values());
        return entities;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public Map<String, List<ProductReview>> getReviews() {
        return reviews;
    }
}
