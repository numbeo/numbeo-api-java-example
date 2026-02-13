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
Bananas (1 kg), Markets                                                                     3.04         USD          28        1.96        8.82
Oranges (1 kg), Markets                                                                     5.43         USD          23        3.31       11.02
Tomatoes (1 kg), Markets                                                                    7.80         USD          26        3.00       13.21
Potatoes (1 kg), Markets                                                                    3.95         USD          21        2.16        8.82
Onions (1 kg), Markets                                                                      4.73         USD          24        3.00        8.82
Lettuce (1 Head), Markets                                                                   3.23         USD          25        2.00        4.00
Bottled Water (1.5 Liter), Markets                                                          2.99         USD          20        1.50        4.00
Bottle of Wine (Mid-Range), Markets                                                        18.00         USD          22       10.00       25.00
Domestic Beer (0.5 Liter Bottle), Markets                                                   3.06         USD          19        1.43        4.29
Imported Beer (0.33 Liter Bottle), Markets                                                  3.77         USD          18        2.50        5.00
Cigarettes (Pack of 20, Marlboro), Markets                                                 14.00         USD          16       11.71       15.00
One-Way Ticket (Local Transport), Transportation                                            3.00         USD          25        2.50        3.50
Monthly Public Transport Pass (Regular Price), Transportation                              87.00         USD          17       81.00      102.00
Taxi Start (Standard Tariff), Transportation                                                4.15         USD          13        4.15        4.15
Taxi 1 km (Standard Tariff), Transportation                                                 2.03         USD          13        2.02        2.49
Taxi 1 Hour Waiting (Standard Tariff), Transportation                                      39.00         USD          12       39.00       39.00
Gasoline (1 Liter), Transportation                                                          1.28         USD          22        1.19        1.43
Volkswagen Golf 1.5 (or Equivalent New Compact Car), Transportation                     36998.00         USD           9    36753.89    37649.85
Toyota Corolla Sedan 1.6 (or Equivalent New Mid-Size Car), Transportation               26147.70         USD          17    25633.55    26897.27
Basic Utilities for 85 m2 Apartment (Electricity, Heating, Cooling, Water, Garb…          229.15         USD          37      134.21      415.88
Mobile Phone Plan (Monthly, with Calls and 10GB+ Data), Utilities (Monthly)                66.71         USD          36       35.00      120.00
Broadband Internet (Unlimited Data, 60 Mbps or Higher), Utilities (Monthly)                68.50         USD          23       35.00      100.00
Monthly Fitness Club Membership, Sports And Leisure                                       115.57         USD          32       36.00      250.00
Tennis Court Rental (1 Hour, Weekend), Sports And Leisure                                  21.88         USD          11        5.00       28.00
Cinema Ticket (International Release), Sports And Leisure                                  18.00         USD          24       15.00       21.00
Private Full-Day Preschool or Kindergarten, Monthly Fee per Child, Childcare             3105.72         USD          24     2362.00     3763.00
International Primary School, Annual Tuition per Child, Childcare                       42000.00         USD           7    42000.00    45000.00
Jeans (Levi's 501 or Similar), Clothing And Shoes                                          74.62         USD          20       42.00       99.00
Summer Dress in a Chain Store (e.g. Zara or H&M), Clothing And Shoes                       48.60         USD          15       32.00       70.00
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
