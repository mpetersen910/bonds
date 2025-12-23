# Bond Analytics API

A Spring Boot REST API for analyzing fixed-income securities. Calculate Yield to Maturity (YTM), Macaulay Duration, and Modified Duration for individual bonds or entire portfolios.

## Features

- **Single Bond Analysis**: Calculate YTM and duration metrics for individual bonds
- **Portfolio Analysis**: Analyze multiple bonds with weighted portfolio metrics
- **ISIN Validation**: Full validation of International Securities Identification Numbers
- **Flexible Payment Terms**: Support for annual, semiannual, quarterly, and monthly payments
- **Input Validation**: Comprehensive validation with clear error messages

## Quick Start

### Prerequisites

- Java 25 or higher
- Maven 3.6+

### Build and Run

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Run Tests

```bash
./mvnw test
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bonds/analyze` | Analyze a single bond |
| POST | `/api/bonds/analyze-from-string` | Analyze a single bond from JSON string |
| POST | `/api/portfolios/analyze` | Analyze a portfolio of bonds |
| POST | `/api/portfolios/analyze-from-string` | Analyze a portfolio from JSON string |

The `-from-string` endpoints accept JSON data serialized as a string, useful for loading data stored or transmitted as string values.

## Input Format

All monetary values use **cents** (e.g., `100000` = $1,000.00) and rates use **basis points** (e.g., `500` = 5.00%).

### Bond Object

```json
{
  "isin": "US0378331005",
  "issueDate": "2023-01-15",
  "maturityDate": "2033-01-15",
  "couponRate": 500,
  "faceValue": 100000,
  "marketValue": 95000,
  "paymentTerm": "semiannual",
  "quantity": 1
}
```

### Field Reference

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `isin` | string | 12-character ISIN | `US0378331005` |
| `issueDate` | string | Issue date (YYYY-MM-DD) | `2023-01-15` |
| `maturityDate` | string | Maturity date (YYYY-MM-DD) | `2033-01-15` |
| `couponRate` | integer | Annual rate in basis points | `500` (5.00%) |
| `faceValue` | integer | Face value in cents | `100000` ($1,000) |
| `marketValue` | integer | Market price in cents | `95000` ($950) |
| `paymentTerm` | string | Payment frequency | `semiannual` |
| `quantity` | integer | Number of bonds | `1` |

### Payment Terms

- `annual` - Once per year
- `semiannual` - Twice per year
- `quarterly` - Four times per year
- `monthly` - Twelve times per year

## Validation Rules

| Field | Rules                                                  |
|-------|--------------------------------------------------------|
| `isin` | Valid 12-character ISIN with correct check digit       |
| `issueDate` / `maturityDate` | YYYY-MM-DD format; maturity must be after issue date   |
| `couponRate` | Non-negative integer (no decimals, no commas)          |
| `faceValue` / `marketValue` | Non-negative integer in cents (no decimals, no commas) |
| `quantity` | Non-negative integer (no decimals, no commas)          |

## Example Usage

### Analyze Single Bond

```bash
curl -X POST http://localhost:8080/api/bonds/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "isin": "US0378331005",
    "issueDate": "2023-01-15",
    "maturityDate": "2033-01-15",
    "couponRate": 500,
    "faceValue": 100000,
    "marketValue": 95000,
    "paymentTerm": "semiannual",
    "quantity": 1
  }'
```

### Analyze Portfolio

```bash
curl -X POST http://localhost:8080/api/portfolios/analyze \
  -H "Content-Type: application/json" \
  -d '[
    {
      "isin": "US0378331005",
      "issueDate": "2023-01-15",
      "maturityDate": "2033-01-15",
      "couponRate": 500,
      "faceValue": 100000,
      "marketValue": 95000,
      "paymentTerm": "semiannual",
      "quantity": 10
    },
    {
      "isin": "US5949181045",
      "issueDate": "2022-06-01",
      "maturityDate": "2032-06-01",
      "couponRate": 650,
      "faceValue": 100000,
      "marketValue": 105000,
      "paymentTerm": "semiannual",
      "quantity": 5
    }
  ]'
```

## Output Metrics

### YTM (Yield to Maturity)

Returned in **basis points**. Represents the total return anticipated if the bond is held until maturity.

### Macaulay Duration

Returned in **years** The weighted average time until cash flows are received. Calculated in days before conversion to years.

### Modified Duration

Returned in **years**. Measures the bond's price sensitivity to interest rate changes. Calculated in days before conversion to years.

### Portfolio Weighted Duration

Calculated as:

```
Weighted Duration = Σ(Duration × MarketValue × Quantity) / Σ(MarketValue × Quantity)
```

## Documentation

- [Bond Controller API](BOND_CONTROLLER_README.md) - Detailed single bond analysis documentation
- [Portfolio Controller API](PORTFOLIO_CONTROLLER_README.md) - Detailed portfolio analysis documentation

## Project Structure

```
src/
├── main/java/com/ice/bonds/
│   ├── controller/     # REST controllers
│   ├── dto/            # Data transfer objects
│   ├── helper/         # Calculation helpers (YTM, Duration, ISIN, Portfolio)
│   ├── model/          # Domain models
│   └── service/        # Business logic
└── test/java/com/ice/bonds/
    └── *Test.java      # Unit and integration tests
```

## TODO
 - Implement additional day count conventions
 - - ENUM for day count types, advanceByPeriod, and reverseByPeriod methods, and integrate into calculations
 - ENUM for payment frequency to replace string literals
 - YTM calculation to return by period and annualized values both
 - Review whether payment is at beginning or end of day and adjust calculations accordingly
## License

This project is provided as-is for educational and demonstration purposes.

