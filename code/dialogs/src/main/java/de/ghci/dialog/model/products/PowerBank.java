package de.ghci.dialog.model.products;

import java.util.List;

/**
 * @author Dominik
 */
public class PowerBank extends Product {

    public PowerBank(String asin, String title, double price, String brand, List<String> categories,
                     String description) {
        super(asin, title, price, brand, categories, description);
    }

    @Override
    public String toString() {
        return "PowerBank{" +
                "asin='" + getAsin() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", price=" + getPrice() +
                ", brand='" + getBrand() + '\'' +
                ", categories=" + getCategories() +
                ", description='" + getDescription() + '\'' +
                '}';
    }


}
