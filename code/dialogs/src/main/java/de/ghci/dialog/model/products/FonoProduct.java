package de.ghci.dialog.model.products;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Dominik
 */
public class FonoProduct {

    public static final String PREFIX_AVAILABLE = "Available. Released ";
    public static final String PREFIX_COMING = "Coming soon. Exp. release ";
    @SerializedName("DeviceName")
    private String deviceName;
    @SerializedName("Brand")
    private String brand;
    private String announced;
    private String status;

    public FonoProduct() {
    }

    public FonoProduct(String deviceName, String brand, String announced, String status) {
        this.deviceName = deviceName;
        this.brand = brand;
        this.announced = announced;
        this.status = status;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getAnnounced() {
        return announced;
    }

    public void setAnnounced(String announced) {
        this.announced = announced;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate extractDate() {
        if (status.startsWith(PREFIX_AVAILABLE)) {
            return extractDateOf(status.substring(PREFIX_AVAILABLE.length()));
        } else if (status.startsWith(PREFIX_COMING)) {
            return extractDateOf(status.substring(PREFIX_COMING.length()));
        } else if (status.startsWith("Rumored")) {
            return LocalDate.now().plusMonths(3);
        } else if (status.startsWith("Discontinued") || status.startsWith("Cancelled")) {
            return null;
        } else {
            throw new IllegalArgumentException("Could not extract Date from FonoProduct: " + toString());
        }
    }

    private LocalDate extractDateOf(String dateString) {
        int year = Integer.parseInt(dateString.substring(0, 4));
        if (dateString.length() > 4) {
            String month = dateString.substring(6);
            if (month.startsWith("Q")) {
                int quarter = Integer.parseInt(month.substring(1));
                return LocalDate.of(year, 1 + quarter * 3, 1);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy, MMMM d", Locale.ENGLISH);
                return LocalDate.parse(makeStringErrorSave(dateString), formatter);
            }
        } else {
            return LocalDate.of(year, 1, 1);
        }
    }

    private String makeStringErrorSave(String dateString) {
        if(dateString.split(" ").length < 3) {
            dateString = dateString.trim() + " 1";
        }
        return dateString.replaceAll("(?<=\\d)(st|nd|rd|th)", "");
    }

    @Override
    public String toString() {
        return "FonoProduct{" +
                "deviceName='" + deviceName + '\'' +
                ", brand='" + brand + '\'' +
                ", announced='" + announced + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
