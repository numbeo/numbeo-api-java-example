# Numbeo API Java Example

A small Java CLI application that queries the [Numbeo API](https://www.numbeo.com/api/) to
retrieve cost-of-living data for a given city and prints a formatted price table to the console.
The documentation for the API is here: https://www.numbeo.com/api/doc.jsp
While API key can be obtained here: https://www.numbeo.com/common/api.jsp this is also the main page for Numbeo API

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
Numbeo Prices for Belgrade, Serbia
===================================
Item                                      Avg Price        Currency    Data Pts         Low        High
---------------------------------------------------------------------------------------------------------
Meal, Inexpensive Restaurant                  600.00           RSD         115      400.00      800.00
...
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
