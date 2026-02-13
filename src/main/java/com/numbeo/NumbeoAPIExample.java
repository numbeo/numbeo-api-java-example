package com.numbeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 * CLI application that queries the Numbeo API to retrieve and display
 * cost-of-living data for a given city.
 *
 * <p>The application calls two Numbeo endpoints:</p>
 * <ul>
 *   <li>{@code /api/items} &ndash; fetches the catalogue of tracked items
 *       (groceries, rent, utilities, etc.) with display ordering</li>
 *   <li>{@code /api/city_prices} &ndash; fetches current crowd-sourced price
 *       data for items in a specific city</li>
 * </ul>
 *
 * <p>Results are merged, sorted by the catalogue display order, and printed
 * as a formatted ASCII table showing average price, currency, data-point
 * count, and low/high range for each item.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   # With arguments
 *   mvn -q exec:java -Dexec.mainClass=com.numbeo.NumbeoAPIExample \
 *       -Dexec.args='"San Francisco, CA" "United States"'
 *
 *   # Interactive (prompts for city and country)
 *   mvn -q exec:java -Dexec.mainClass=com.numbeo.NumbeoAPIExample
 * </pre>
 *
 * <p>Requires a {@code config.properties} file in the working directory
 * containing {@code api_key=YOUR_KEY}.</p>
 */
public class NumbeoAPIExample {
    private static final String CONFIG_FILE = "config.properties";
    private static final String ITEMS_URL = "https://www.numbeo.com/api/items?api_key=%s";
    private static final String CITY_PRICES_URL = "https://www.numbeo.com/api/city_prices?query=%s&api_key=%s";

    /**
     * Entry point. Reads the API key, gathers city/country input, fetches
     * item catalogue and city prices from Numbeo, then prints a merged,
     * sorted price table to stdout.
     *
     * @param args optional &ndash; {@code "City, State" "Country"} or
     *             individual tokens that are parsed heuristically
     */
    public static void main(String[] args) throws Exception {
        String apiKey = readApiKey();
        Input input = readInput(args);
        String query = input.city + "," + input.country;
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        ItemsResponse itemsResponse = fetchJson(
                mapper,
                String.format(Locale.ROOT, ITEMS_URL, apiKey),
                ItemsResponse.class
        );
        CityPricesResponse pricesResponse = fetchJson(
                mapper,
                String.format(Locale.ROOT, CITY_PRICES_URL, encodedQuery, apiKey),
                CityPricesResponse.class
        );

        Map<Integer, Item> itemsById = new HashMap<>();
        if (itemsResponse != null && itemsResponse.items != null) {
            for (Item item : itemsResponse.items) {
                if (item != null && item.item_id != null) {
                    itemsById.put(item.item_id, item);
                }
            }
        }

        List<PriceRow> rows = new ArrayList<>();
        if (pricesResponse != null && pricesResponse.prices != null) {
            for (Price price : pricesResponse.prices) {
                if (price == null || price.item_id == null) {
                    continue;
                }
                Item item = itemsById.get(price.item_id);
                rows.add(new PriceRow(item, price, pricesResponse == null ? null : pricesResponse.currency));
            }
        }

        rows.sort(Comparator
                .comparingInt((PriceRow r) -> r.displayOrder())
                .thenComparing(r -> r.itemName == null ? "" : r.itemName)
        );

        printTable(rows, input);
    }

    /**
     * Obtains city and country input. If at least two command-line arguments
     * are provided they are parsed directly; otherwise the user is prompted
     * interactively via stdin.
     */
    private static Input readInput(String[] args) {
        if (args.length >= 2) {
            return parseArgs(args);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("City (e.g., San Francisco, CA): ");
        String city = scanner.nextLine().trim();
        System.out.print("Country (e.g., United States): ");
        String country = scanner.nextLine().trim();
        return new Input(city, country);
    }

    /**
     * Loads the Numbeo API key from {@value #CONFIG_FILE}.
     *
     * @return the trimmed API key
     * @throws IOException if the file is missing or {@code api_key} is blank
     */
    private static String readApiKey() throws IOException {
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(CONFIG_FILE);
            props.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        String apiKey = props.getProperty("api_key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("Missing api_key in " + CONFIG_FILE);
        }
        return apiKey.trim();
    }

    /**
     * Parses command-line arguments into a city/country pair.
     *
     * <p>Supports several formats:</p>
     * <ul>
     *   <li>Two quoted args: {@code "San Francisco, CA" "United States"}</li>
     *   <li>Unquoted tokens with a comma separating city from country:
     *       {@code San Francisco, CA United States}</li>
     *   <li>Unquoted tokens without a comma &ndash; the last token is
     *       treated as the country</li>
     * </ul>
     */
    private static Input parseArgs(String[] args) {
        if (args.length == 2) {
            return new Input(args[0].trim(), args[1].trim());
        }

        int commaIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains(",")) {
                commaIndex = i;
                break;
            }
        }

        String city;
        String country;

        if (commaIndex >= 0) {
            int cityEnd = commaIndex;
            if (commaIndex + 1 < args.length && args[commaIndex + 1].length() == 2) {
                cityEnd = commaIndex + 1;
            }
            city = joinTokens(args, 0, cityEnd);
            country = joinTokens(args, cityEnd + 1, args.length - 1);
        } else {
            city = joinTokens(args, 0, args.length - 2);
            country = args[args.length - 1];
        }

        return new Input(city.trim(), country.trim());
    }

    /** Joins {@code args[start..end]} (inclusive) with spaces. */
    private static String joinTokens(String[] args, int start, int end) {
        if (start > end || start < 0 || end >= args.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (i > start) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /**
     * Performs an HTTP GET request and deserialises the JSON response.
     *
     * @param mapper Jackson object mapper
     * @param url    fully-qualified API URL including query parameters
     * @param type   target class for JSON deserialisation
     * @param <T>    response type
     * @return the deserialised response object
     * @throws IOException on network errors or non-200 status codes
     */
    private static <T> T fetchJson(ObjectMapper mapper, String url, Class<T> type)
            throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAll(stream);

            if (status != 200) {
                throw new IOException("Request failed: " + status + " -> " + url + " Body: " + body);
            }
            return mapper.readValue(body, type);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Prints a formatted ASCII table of price rows to stdout.
     * Columns: Item, Avg Price, Currency, Data Points, Low, High.
     * The item-name column width adapts to the data (clamped 40-80 chars).
     */
    private static void printTable(List<PriceRow> rows, Input input) {
        String title = "Numbeo Prices for " + input.city + ", " + input.country;
        System.out.println(title);
        System.out.println(repeat("=", title.length()));

        int itemWidth = computeItemWidth(rows);
        String format = "%-" + itemWidth + "s  %14s  %10s  %10s  %10s  %10s%n";
        System.out.printf(format, "Item", "Avg Price", "Currency", "Data Pts", "Low", "High");
        System.out.println(repeat("-", itemWidth + 2 + 14 + 2 + 10 + 2 + 10 + 2 + 10 + 2 + 10));

        DecimalFormat priceFormat = new DecimalFormat("0.00");
        for (PriceRow row : rows) {
            String avg = row.averagePrice == null ? "" : priceFormat.format(row.averagePrice);
            String low = row.lowestPrice == null ? "" : priceFormat.format(row.lowestPrice);
            String high = row.highestPrice == null ? "" : priceFormat.format(row.highestPrice);
            String name = row.itemName == null ? ("Item " + row.itemId) : row.itemName;
            if (name.length() > itemWidth) {
                name = name.substring(0, Math.max(0, itemWidth - 1)) + "…";
            }
            System.out.printf(
                    format,
                    name,
                    avg,
                    row.currency == null ? "" : row.currency,
                    row.dataPoints == null ? "" : row.dataPoints,
                    low,
                    high
            );
        }
    }

    /** Computes the item-name column width, clamped between 40 and 80 characters. */
    private static int computeItemWidth(List<PriceRow> rows) {
        int width = 4; // "Item"
        for (PriceRow row : rows) {
            String name = row.itemName == null ? ("Item " + row.itemId) : row.itemName;
            if (name != null && name.length() > width) {
                width = name.length();
            }
        }
        int min = 40;
        int max = 80;
        if (width < min) {
            return min;
        }
        if (width > max) {
            return max;
        }
        return width;
    }

    /** Simple value object holding the user-supplied city and country. */
    private static class Input {
        final String city;
        final String country;

        Input(String city, String country) {
            this.city = city == null ? "" : city.trim();
            this.country = country == null ? "" : country.trim();
        }
    }

    /** Reads the entire contents of an {@link InputStream} into a String (UTF-8). */
    private static String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /** Repeats string {@code s} {@code count} times (Java 8 compatible). */
    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    /** JSON model for the {@code /api/items} response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ItemsResponse {
        public List<Item> items;
    }

    /** A single catalogue item (e.g. "Milk (1 liter)", "Apartment 1 bedroom"). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Item {
        public Integer item_id;
        public String item_name;
        public String item_name_en;
        public Integer display_order;
    }

    /** JSON model for the {@code /api/city_prices} response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CityPricesResponse {
        public List<Price> prices;
        public String currency;
    }

    /** Price data for a single item in the queried city. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Price {
        public Integer item_id;
        public Double average_price;
        public Double lowest_price;
        public Double highest_price;
        public Integer data_points;
        public String item_name;
        public String currency;
    }

    /**
     * Merged view of an {@link Item} and its {@link Price} for table display.
     * Combines catalogue metadata (name, display order) with price statistics.
     */
    private static class PriceRow {
        final Integer itemId;
        final String itemName;
        final Integer displayOrder;
        final Double averagePrice;
        final Double lowestPrice;
        final Double highestPrice;
        final Integer dataPoints;
        final String currency;

        PriceRow(Item item, Price price, String fallbackCurrency) {
            this.itemId = price.item_id;
            this.itemName = resolveItemName(item, price);
            this.displayOrder = item == null ? null : item.display_order;
            this.averagePrice = price.average_price;
            this.lowestPrice = price.lowest_price;
            this.highestPrice = price.highest_price;
            this.dataPoints = price.data_points;
            this.currency = price.currency == null ? fallbackCurrency : price.currency;
        }

        int displayOrder() {
            return displayOrder == null ? Integer.MAX_VALUE : displayOrder;
        }
    }

    /**
     * Resolves the best available display name for an item.
     * Prefers {@code item.item_name}, falls back to {@code item.item_name_en},
     * then to {@code price.item_name}.
     */
    private static String resolveItemName(Item item, Price price) {
        if (item != null) {
            if (item.item_name != null && !item.item_name.trim().isEmpty()) {
                return item.item_name;
            }
            if (item.item_name_en != null && !item.item_name_en.trim().isEmpty()) {
                return item.item_name_en;
            }
        }
        if (price != null && price.item_name != null && !price.item_name.trim().isEmpty()) {
            return price.item_name;
        }
        return null;
    }
}
