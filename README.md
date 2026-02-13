# Numbeo API Java Example

A small Java CLI application that queries the Numbeo API to retrieve cost-of-living data for a
given city and prints a formatted price table to the console.
API documentation: [Numbeo API Docs](https://www.numbeo.com/api/doc.jsp)
API keys are obtained from the Numbeo API main page: [Numbeo API](https://www.numbeo.com/common/api.jsp)

## Features

- Fetches the full Numbeo item catalogue (`/api/items`) and city-specific prices (`/api/city_prices`)
- Merges and sorts results by the catalogue's display order
- Prints a formatted ASCII table with columns: **Item**, **Avg Price**, **Currency**, **Data Points**, **Low**, **High**
- Accepts input via command-line arguments or interactive prompts
- Flexible argument parsing (quoted pairs, comma-separated tokens, or bare words)

## Requirements

- **Java 8** or later
- **Maven 3.x**
- A valid Numbeo API key (https://www.numbeo.com/common/api.jsp)

## Project Structure

```
JavaExample/
├── pom.xml                                    # Maven build configuration
├── config.properties                          # API key (not committed)
├── README.md
└── src/main/java/com/numbeo/
    └── NumbeoAPIExample.java                  # Application source
```

## Setup

1. Clone or download this project.

2. Create a `config.properties` file in the project root:

   ```properties
   api_key=YOUR_NUMBEO_API_KEY
   ```

3. Install dependencies and compile:

   ```bash
   mvn -q -DskipTests package
   ```

## Usage

### With command-line arguments

```bash
mvn -q exec:java \
    -Dexec.mainClass=com.numbeo.NumbeoAPIExample \
    -Dexec.args='"San Francisco, CA" "United States"'
```

### Interactive mode

```bash
mvn -q exec:java -Dexec.mainClass=com.numbeo.NumbeoAPIExample
```

You will be prompted to enter the city and country:

```
City (e.g., San Francisco, CA): Belgrade
Country (e.g., United States): Serbia
```

### Example output

```
==================================================
Item                                                                                   Avg Price    Currency    Data Pts         Low        High
------------------------------------------------------------------------------------------------------------------------------------------------
Meal at an Inexpensive Restaurant, Restaurants                                             25.00         USD          25       16.00       45.00
Meal for Two at a Mid-Range Restaurant (Three Courses, Without Drinks), Restaur…          145.00         USD          20      100.00      200.00
Combo Meal at McDonald's (or Equivalent Fast-Food Meal), Restaurants                       15.00         USD          13       11.00       15.00
Domestic Draft Beer (0.5 Liter), Restaurants                                                8.00         USD          23        6.00       12.00
Imported Beer (0.33 Liter Bottle), Restaurants                                              9.00         USD          15        7.00       10.00
Cappuccino (Regular Size), Restaurants                                                      5.68         USD          33        3.72       10.00
Soft Drink (Coca-Cola or Pepsi, 0.33 Liter Bottle), Restaurants                             3.31         USD          24        2.75        4.00
Bottled Water (0.33 Liter), Restaurants                                                     2.88         USD          26        2.00        5.00
Milk (Regular, 1 Liter), Markets                                                            1.58         USD          34        1.06        2.38
Fresh White Bread (500 g Loaf), Markets                                                     5.26         USD          33        3.30       11.02
White Rice (1 kg), Markets                                                                  6.47         USD          21        2.87       13.23
Eggs (12, Large Size), Markets                                                              6.60         USD          23        3.50        9.17
Local Cheese (1 kg), Markets                                                               22.36         USD          26       11.02       44.09
Chicken Fillets (1 kg), Markets                                                            14.22         USD          24        6.61       26.46
Beef Round or Equivalent Back Leg Red Meat (1 kg), Markets                                 23.82         USD          21       15.43       37.46
Apples (1 kg), Markets                                                                      7.04         USD          26        4.14       11.02
```

## How It Works

1. **Reads** the API key from `config.properties`.
2. **Parses** city/country from arguments or prompts the user.
3. **Fetches** the item catalogue and city prices from the Numbeo API (HTTP GET, JSON responses).
4. **Merges** prices with catalogue metadata (name, display order) into a unified list.
5. **Sorts** by display order, then alphabetically by item name.
6. **Prints** the results as a formatted table.

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| [Jackson Databind](https://github.com/FasterXML/jackson-databind) | 2.17.2 | JSON deserialisation |

## Configuration Reference

| Property | Required | Description |
|----------|----------|-------------|
| `api_key` | Yes | Your Numbeo API key |

## Troubleshooting

- **`Missing api_key in config.properties`** -- Ensure the file exists in the working directory and contains a valid `api_key=...` entry.
- **HTTP 4xx/5xx errors** -- Verify your API key is active and the city/country names match Numbeo's expected format (e.g. `"San Francisco, CA"` not `"SF"`).
- **Timeouts** -- The app uses a 15 s connect timeout and 20 s read timeout. Check your network connection if requests fail.
