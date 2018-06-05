package de.ghci.dialog.process.products;

import com.google.inject.Inject;
import de.ghci.dialog.model.products.Product;
import de.ghci.dialog.process.ContainsMatcher;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Dominik
 */
public class ProductsHelper {

    @Inject
    private ContainsMatcher containsMatcher;

    public <T extends Product> T getProduct(Collection<T> products, String text) {
        products.stream()
                .filter(p1 -> containsMatcher.matches(text, p1.getTitle()))
                .forEach(p -> System.out.println(p.getTitle()));
        Optional<T> product = products.stream()
                .filter(p -> containsMatcher.matches(text, p.getTitle()))
                .sorted(Comparator.comparingInt(o -> o.getTitle().length()))
                .findFirst();
        if(product.isPresent()) {
            return product.get();
        } else {
            return null;
        }
    }
}
