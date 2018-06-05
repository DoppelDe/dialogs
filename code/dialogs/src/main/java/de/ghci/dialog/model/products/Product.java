package de.ghci.dialog.model.products;

import de.ghci.dialog.model.Entity;

import java.util.List;
import java.util.Objects;

/**
 * @author Dominik
 */
public class Product implements Entity {

    private String asin;
    private String title;
    private double price;
    private String brand;
    private List<String> categories;
    private String description;

    public Product(String asin, String title, double price, String brand, List<String> categories, String description) {
        this.asin = asin;
        this.title = title;
        this.price = price;
        this.brand = brand;
        this.categories = categories;
        this.description = description;
    }

    public String getAsin() {
        return asin;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public String getBrand() {
        return brand;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(asin, product.asin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asin);
    }

    @Override
    public String toString() {
        return "Product{" +
                "asin='" + asin + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", brand='" + brand + '\'' +
                ", categories=" + categories +
                ", description='" + description + '\'' +
                '}';
    }
}
