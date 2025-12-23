# Portfolio Analysis API

Analyze a portfolio of bonds to calculate individual bond metrics and portfolio-level weighted durations.

## Endpoint

```
POST /api/portfolios/analyze
```

## Request Body

An array of bond objects:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `isin` | string | Yes | International Securities Identification Number (12 characters) |
| `issueDate` | string | Yes | Bond issue date in ISO format (`YYYY-MM-DD`) |
| `maturityDate` | string | Yes | Bond maturity date in ISO format (`YYYY-MM-DD`) |
| `couponRate` | integer | Yes | Annual coupon rate in basis points (e.g., `500` = 5.00%) |
| `faceValue` | integer | Yes | Face value in cents (e.g., `100000` = $1,000.00) |
| `marketValue` | integer | Yes | Current market price in cents (e.g., `95000` = $950.00) |
| `paymentTerm` | string | Yes | Payment frequency: `annual`, `semiannual`, `quarterly`, or `monthly` |
| `quantity` | integer | Yes | Number of bonds held (must be positive) |

### Validation Rules

- **ISIN**: Must be a valid 12-character ISIN with correct check digit
- **Dates**: Must be in `YYYY-MM-DD` format; maturity date must be after issue date
- **Numeric values**: Must be whole numbers (no decimals, no commas)
- **couponRate**: Must be non-negative (0 or greater)
- **faceValue/marketValue**: Must be non-negative integers in cents
- **quantity**: Must be a positive integer (1 or greater)

## Sample Request

```json
[
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
]
```

## Sample Response

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "accountId": "default-account",
  "bonds": [
    {
      "isin": "US0378331005",
      "ytm": 569.1284271541838,
      "macaulayDuration": 7.932435678901234,
      "modifiedDuration": 7.713456789012345,
      "maturityDate": "2033-01-15",
      "issueDate": "2023-01-15",
      "couponRate": 500,
      "faceValue": 100000,
      "marketValue": 95000,
      "paymentTerm": "semiannual",
      "quantity": 10,
      "bondWeightInPortfolio": 0.6442307692307693
    },
    {
      "isin": "US5949181045",
      "ytm": 576.2345678901234,
      "macaulayDuration": 6.123456789012345,
      "modifiedDuration": 5.987654321098765,
      "maturityDate": "2032-06-01",
      "issueDate": "2022-06-01",
      "couponRate": 650,
      "faceValue": 100000,
      "marketValue": 105000,
      "paymentTerm": "semiannual",
      "quantity": 5,
      "bondWeightInPortfolio": 0.3557692307692307
    }
  ],
  "weightedMacaulayDuration": 7.289012345678901,
  "weightedModifiedDuration": 7.098765432109876,
  "totalPortfolioValue": 1475000
}
```

### Response Fields

#### Portfolio Level

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique portfolio identifier (UUID) |
| `accountId` | string | Account identifier (default: `default-account`) |
| `bonds` | array | Array of analyzed bonds |
| `weightedMacaulayDuration` | number | Portfolio weighted average Macaulay Duration in years |
| `weightedModifiedDuration` | number | Portfolio weighted average Modified Duration in years |
| `totalPortfolioValue` | integer | Total portfolio market value in cents |

#### Bond Level

| Field | Type | Description |
|-------|------|-------------|
| `isin` | string | Bond identifier |
| `ytm` | number | Yield to Maturity in basis points (e.g., `569.13` = 5.69%) |
| `macaulayDuration` | number | Macaulay Duration in years |
| `modifiedDuration` | number | Modified Duration in years |
| `maturityDate` | string | Bond maturity date |
| `issueDate` | string | Bond issue date |
| `couponRate` | integer | Annual coupon rate in basis points |
| `faceValue` | integer | Face value in cents |
| `marketValue` | integer | Market price in cents |
| `paymentTerm` | string | Payment frequency |
| `quantity` | integer | Number of bonds held |
| `bondWeightInPortfolio` | number | Weight of bond in portfolio (0.0 to 1.0) |

## cURL Example

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

## Error Responses

| Status Code | Description |
|-------------|-------------|
| `400 Bad Request` | Invalid input (validation error) |
| `500 Internal Server Error` | Server error |

### Example Error Response

```
Invalid faceValue: 100,000. Value must not contain commas
```

## Weighted Duration Calculation

Portfolio weighted durations are calculated using market value weighting:

```
Weighted Duration = Σ(Duration_i × MarketValue_i × Quantity_i) / Σ(MarketValue_i × Quantity_i)
```

This provides a measure of the portfolio's overall interest rate sensitivity.

## Additional Examples

### Single Bond Portfolio

```json
[
  {
    "isin": "US0378331005",
    "issueDate": "2023-01-15",
    "maturityDate": "2033-01-15",
    "couponRate": 500,
    "faceValue": 100000,
    "marketValue": 95000,
    "paymentTerm": "semiannual",
    "quantity": 100
  }
]
```

### Diversified Portfolio (Multiple Payment Terms)

```json
[
  {
    "isin": "US0378331005",
    "issueDate": "2023-01-15",
    "maturityDate": "2033-01-15",
    "couponRate": 500,
    "faceValue": 100000,
    "marketValue": 95000,
    "paymentTerm": "semiannual",
    "quantity": 20
  },
  {
    "isin": "GB0002634946",
    "issueDate": "2024-03-15",
    "maturityDate": "2029-03-15",
    "couponRate": 425,
    "faceValue": 100000,
    "marketValue": 98000,
    "paymentTerm": "quarterly",
    "quantity": 15
  },
  {
    "isin": "DE0007164600",
    "issueDate": "2023-07-01",
    "maturityDate": "2030-07-01",
    "couponRate": 375,
    "faceValue": 100000,
    "marketValue": 92000,
    "paymentTerm": "annual",
    "quantity": 25
  }
]
```

### High-Yield Bond Portfolio

```json
[
  {
    "isin": "FR0000120578",
    "issueDate": "2022-09-01",
    "maturityDate": "2027-09-01",
    "couponRate": 750,
    "faceValue": 100000,
    "marketValue": 102000,
    "paymentTerm": "semiannual",
    "quantity": 50
  },
  {
    "isin": "JP3633400001",
    "issueDate": "2023-03-15",
    "maturityDate": "2028-03-15",
    "couponRate": 825,
    "faceValue": 100000,
    "marketValue": 104000,
    "paymentTerm": "semiannual",
    "quantity": 30
  }
]
```

### Empty Portfolio

```json
[]
```

Returns a portfolio with zero bonds, zero total value, and zero weighted durations.

