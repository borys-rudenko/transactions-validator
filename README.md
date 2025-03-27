# 💳 Transaction Validator Service

This Spring Boot application validates bank transaction records (CSV or JSON), checking for:

- ✅ Duplicate transaction references
- ✅ Incorrect end balance calculations

---

## 📆 Features

- Upload transaction files (`.csv` or `.json`)
- Parse and validate using plugable file parsers
- REST API with OpenAPI spec
- Error responses with structured JSON
- Integration and unit test coverage
- CSV & JSON support out of the box

---

## 🚀 Tech Stack

- Java 17+
- Spring Boot
- Apache Commons CSV
- Jackson (JSON)
- Lombok
- OpenAPI Generator
- JUnit 5 + MockMvc

---

## 🛠️ Getting Started

### ✅ Prerequisites

- Java 17+
- Maven

### 📆 Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

---

## 📂 API Endpoint

### `POST /transactions/validate`

Upload a file (`.csv` or `.json`) with transaction records. Returns a list of all transactions with validation info.

#### Request (multipart/form-data):
- `file`: transaction file

#### Sample Response:
```json
[
  {
    "reference": 1001,
    "accountNumber": "NL91ABNA0417164300",
    "description": "Grocery [ERROR: Duplicate transaction reference] [ERROR: Incorrect end balance, expected 80.00]",
    "startBalance": 100.00,
    "mutation": -20.00,
    "endBalance": 90.00
  }
]
```

---

## 📊 Validation Rules

| Rule                         | Description                                  |
|------------------------------|----------------------------------------------|
| Duplicate Reference          | `reference` must be unique                   |
| Incorrect End Balance        | `startBalance + mutation == endBalance`     |

---

## 📁 Supported File Formats

### ✅ CSV Format:
```csv
Reference,AccountNumber,Description,Start Balance,Mutation,End Balance
1001,NL91ABNA0417164300,Grocery,100.00,-20.00,80.00
```

### ✅ JSON Format:
```json
[
  {
    "reference": 1001,
    "accountNumber": "NL91ABNA0417164300",
    "description": "Grocery",
    "startBalance": 100.00,
    "mutation": -20.00,
    "endBalance": 80.00
  }
]
```

---

## ✅ Error Handling

Errors are returned as structured JSON:

```json
{
  "message": "Unsupported file format: .txt",
  "error": "Bad Request",
  "status": 400
}
```

---

## 🧪 Running Tests

```bash
mvn test
```

- Unit tests: Services, mappers, exception handlers
- Integration tests: File upload, parsing, validation, global errors

---

## 📄 OpenAPI Spec

OpenAPI documentation is auto-generated from spec.

To regenerate:
```bash
mvn clean generate-sources
```