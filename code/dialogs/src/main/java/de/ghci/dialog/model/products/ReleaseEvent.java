package de.ghci.dialog.model.products;

import de.ghci.dialog.model.Event;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * @author Dominik
 */
public class ReleaseEvent extends Event {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private FonoProduct product;
    private LocalDate date;

    public ReleaseEvent(FonoProduct product) {
        super("");
        this.product = product;
        this.date = product.extractDate();
        setDescription(extractDescription(product));
    }

    private String extractDescription(FonoProduct product) {
        if(date == null) {
            return product.getDeviceName();
        }
        if(LocalDate.now().isBefore(date)) {
            return "Have you heard about the " + product.getDeviceName() + ", which should be released around " +
                    date.format(FORMATTER);
        } else if(LocalDate.now().minusYears(1).isBefore(date)){
            return "The " + product.getDeviceName() + " is a quite new model.";
        } else if(LocalDate.now().minusYears(5).isBefore(date)){
            return "Did you know, that the " + product.getDeviceName() + " was released in "
                    + date.format(FORMATTER) + "?";
        } else {
            return "The " + product.getDeviceName() + " is quite an old phone.";
        }
    }

    public FonoProduct getProduct() {
        return product;
    }

    public void setProduct(FonoProduct product) {
        this.product = product;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
