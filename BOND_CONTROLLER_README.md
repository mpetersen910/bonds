# Bond Analysis API

Analyze individual bonds to calculate Yield to Maturity (YTM), Macaulay Duration, and Modified Duration.

## Endpoints

### 1. Analyze Bond (JSON Object)

```
POST /api/bonds/analyze
```

### 2. Analyze Bond from JSON String

```
POST /api/bonds/analyze-from-string
```

Accepts JSON serialized as a string (useful when bond data is stored or transmitted as a string value).

## Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `isin` | string | Yes | International Securities Identification Number (12 characters) |
| `issueDate` | string | Yes | Bond issue date in ISO format (`YYYY-MM-DD`) |
| `maturityDate` | string | Yes | Bond maturity date in ISO format (`YYYY-MM-DD`) |
| `couponRate` | integer | Yes | Annual coupon rate in basis points (e.g., `500` = 5.00%) |
| `faceValue` | integer | Yes | Face value in cents (e.g., `100000` = $1,000.00) |
| `marketValue` | integer | Yes | Current market price in cents (e.g., `95000` = $950.00) |
| `paymentTerm` | string | Yes | Payment frequency: `annual`, `semiannual`, `quarterly`, or `monthly` |
| `quantity` | integer | Yes | Number of bonds (must be positive) |

### Validation Rules

- **ISIN**: Must be a valid 12-character ISIN with correct check digit
- **Dates**: Must be in `YYYY-MM-DD` format; maturity date must be after issue date
- **Numeric values**: Must be whole numbers (no decimals, no commas)
- **couponRate**: Must be non-negative (0 or greater)
- **faceValue/marketValue**: Must be non-negative integers in cents
- **quantity**: Must be a positive integer (1 or greater)

## Sample Request

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

## Sample Response

```json
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
  "quantity": 1
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `isin` | string | Bond identifier from the request |
| `ytm` | number | Yield to Maturity in basis points (e.g., `569.13` = 5.69%) |
| `macaulayDuration` | number | Macaulay Duration in years |
| `modifiedDuration` | number | Modified Duration in years |
| `maturityDate` | string | Bond maturity date |
| `issueDate` | string | Bond issue date |
| `couponRate` | integer | Annual coupon rate in basis points |
| `faceValue` | integer | Face value in cents |
| `marketValue` | integer | Market price in cents |
| `paymentTerm` | string | Payment frequency |
| `quantity` | integer | Number of bonds |

## cURL Example

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

## Analyze from JSON String Endpoint

The `/api/bonds/analyze-from-string` endpoint accepts a JSON-serialized string. This is useful when bond data has been serialized to a string for storage or transmission.

### Request Format

The request body should be a JSON string value containing the escaped bond JSON:

```json
"{\"isin\": \"US0378331005\", \"issueDate\": \"2023-01-15\", \"maturityDate\": \"2033-01-15\", \"couponRate\": \"500\", \"faceValue\": \"100000\", \"marketValue\": \"95000\", \"paymentTerm\": \"semiannual\", \"quantity\": \"1\"}"
```

### CURL Example (JSON String)

```bash
curl -X POST http://localhost:8080/api/bonds/analyze-from-string \
  -H "Content-Type: application/json" \
  -d '"{\"isin\": \"US0378331005\", \"issueDate\": \"2023-01-15\", \"maturityDate\": \"2033-01-15\", \"couponRate\": \"500\", \"faceValue\": \"100000\", \"marketValue\": \"95000\", \"paymentTerm\": \"semiannual\", \"quantity\": \"1\"}"'
```

### Sample Response

The response format is identical to the `/api/bonds/analyze` endpoint:

```json
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
  "quantity": 1
}
```

### Use Cases

- Loading bond data stored as serialized JSON strings in databases
- Processing bond data received from message queues where JSON is string-encoded
- Integrating with systems that transmit JSON as escaped string values

## Error Responses

| Status Code | Description |
|-------------|-------------|
| `400 Bad Request` | Invalid input (validation error) |
| `500 Internal Server Error` | Server error |

### Example Error Response

```
Invalid couponRate: 500.5. Coupon rate must be a whole number without decimals
```

## Additional Examples

### Premium Bond (Market Value > Face Value)

```json
{
  "isin": "US5949181045",
  "issueDate": "2022-06-01",
  "maturityDate": "2032-06-01",
  "couponRate": 650,
  "faceValue": 100000,
  "marketValue": 105000,
  "paymentTerm": "semiannual",
  "quantity": 1
}
```

### Zero-Coupon Bond

```json
{
  "isin": "US0231351067",
  "issueDate": "2023-01-15",
  "maturityDate": "2033-01-15",
  "couponRate": 0,
  "faceValue": 100000,
  "marketValue": 75000,
  "paymentTerm": "semiannual",
  "quantity": 1
}
```

### Quarterly Payment Bond

```json
{
  "isin": "GB0002634946",
  "issueDate": "2024-03-15",
  "maturityDate": "2029-03-15",
  "couponRate": 425,
  "faceValue": 100000,
  "marketValue": 98000,
  "paymentTerm": "quarterly",
  "quantity": 1
}
```

### Annual Payment Bond

```json
{
  "isin": "DE0007164600",
  "issueDate": "2023-07-01",
  "maturityDate": "2030-07-01",
  "couponRate": 375,
  "faceValue": 100000,
  "marketValue": 92000,
  "paymentTerm": "annual",
  "quantity": 1
}
```


