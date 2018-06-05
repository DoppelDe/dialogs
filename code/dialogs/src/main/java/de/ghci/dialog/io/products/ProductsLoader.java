package de.ghci.dialog.io.products;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.ghci.dialog.model.products.CellPhone;
import de.ghci.dialog.model.products.FonoProduct;
import de.ghci.dialog.model.products.PowerBank;
import de.ghci.dialog.model.products.Product;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Dominik
 */
public class ProductsLoader {

    private static final String PRODUCTS_FILE = "C:\\Users\\Dominik\\Documents\\studium_master\\masterarbeit\\" +
            "workspace\\dialogs\\src\\main\\resources\\products_meta.json";
    private static final String CATEGORY_CELL_PHONE = "Cell Phones";
    private static final String CATEGORY_POWER_BANK = "External Battery Packs";
    private static final String FONO_TOKEN = "fc6c10ab835eec560ef2715b18bcfd22238c78c226f7e6a6";
    public static final String FONO_API_URL_GET_DEVICE = "https://fonoapi.freshpixl.com/v1/getdevice?token="
            + FONO_TOKEN + "&device=";
    public static final String FONO_API_URL_GET_LATEST = "https://fonoapi.freshpixl.com/v1/getlatest?token="
            + FONO_TOKEN;

    public FonoProduct getRandomFonoProduct() {
        String requestUrl = FONO_API_URL_GET_LATEST;
        Optional<String> responseString = getApiResponse(requestUrl);
        if(responseString.isPresent()) {
            FonoProduct[] products = new Gson().fromJson(responseString.get(), FonoProduct[].class);
            return products[new Random().nextInt(products.length)];
        } else {
            return null;
        }
    }

    public List<FonoProduct> getFonoProducts(String deviceName) {
        String requestUrl = FONO_API_URL_GET_DEVICE + deviceName;
        Optional<String> responseString = getApiResponse(requestUrl);
        if(responseString.isPresent()) {
            FonoProduct[] products = new Gson().fromJson(responseString.get(), FonoProduct[].class);
            return Arrays.asList(products);
        } else {
            return new ArrayList<>();
        }
    }

    private Optional<String> getApiResponse(String requestUrl) {
        Optional<String> responseString;HttpURLConnection conn = null;
        try {
            //Create connection
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.addRequestProperty("User-Agent", "Mozilla/4.76");
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            //Get Response
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            responseString = Optional.of(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            responseString = Optional.empty();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return responseString;
    }

    public Map<String, Product> getProducts() throws IOException {
        Map<String, Product> products = new HashMap<>();
        final Gson gson = new Gson();
        try (Stream<String> stream = Files.lines(Paths.get(PRODUCTS_FILE))) {
            stream.forEach(line -> {
                try {
                    JsonProduct product = gson.fromJson(line, JsonProduct.class);
                    products.put(product.getAsin(), createProduct(product));
                } catch (JsonSyntaxException ignored) {
                }
            });
        }
        return products;
    }

    private Product createProduct(JsonProduct product) {
        List<String> categories = new ArrayList<>();
        Arrays.stream(product.getCategories()).forEach(a -> Arrays.stream(a).forEach(categories::add));
        if (categories.contains(CATEGORY_CELL_PHONE)) {
            return new CellPhone(product.getAsin(), product.getTitle(), product.getPrice(), product.getBrand(),
                    categories, product.getDescription());
        } else if (categories.contains(CATEGORY_POWER_BANK)) {
            return new PowerBank(product.getAsin(), product.getTitle(), product.getPrice(), product.getBrand(),
                    categories, product.getDescription());
        } else {
            return new Product(product.getAsin(), product.getTitle(), product.getPrice(), product.getBrand(),
                    categories, product.getDescription());
        }
    }

    private class JsonProduct {
        private String asin;
        private String title;
        private double price;
        private String brand;
        private String[][] categories;
        private String description;

        public String getAsin() {
            return asin;
        }

        public void setAsin(String asin) {
            this.asin = asin;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String[][] getCategories() {
            return categories;
        }

        public void setCategories(String[][] categories) {
            this.categories = categories;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }
}
