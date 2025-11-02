# AI-Powered Invoice Reconciliation System

An intelligent invoice-payment reconciliation system leveraging OpenAI GPT-4o-mini to automate bookkeeping for small businesses. Built with Spring Boot, PostgreSQL, and modern Java practices.

## Problem Statement

Small businesses struggle with manual payment reconciliation:
- **Hundreds of invoices** with multiple partial payments
- **Messy payment remarks**: "ramesh bill", "inv 101", "INVOICE-101"
- **Time-consuming** manual matching process
- **Error-prone** bookkeeping leading to cash flow issues

## Solution

This system uses AI to analyze payment remarks, amounts, dates, and customer names to automatically suggest invoice matches with confidence scores and detailed reasoning. Business owners can review, confirm, or reject suggestions in bulk, saving hours of manual work.

## Features

- **AI-Powered Matching**: OpenAI GPT-4o-mini analyzes payments and suggests matches
- **Confidence Scoring**: Each suggestion includes a confidence score (0.00-1.00) and reasoning
- **Bulk Operations**: Confirm multiple suggestions at once or auto-confirm high-confidence matches
- **RESTful APIs**: Complete REST API with Swagger documentation
- **PostgreSQL Database**: Reliable relational database with Flyway migrations
- **Docker Support**: Containerized deployment with Docker Compose
- **Test Coverage**: 80%+ code coverage with JUnit 5 and Mockito
- **Sample Data**: 80 invoices and 30 payments for testing

## Technology Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL 15 |
| AI Engine | OpenAI GPT-4o-mini |
| Build Tool | Maven 3.9+ |
| Testing | JUnit 5, Mockito, TestContainers |
| API Docs | Swagger/OpenAPI 3.0 |
| Containerization | Docker, Docker Compose |

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API Key

### Running with Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mybillbook
   ```

2. **Set OpenAI API Key**
   ```bash
   export OPENAI_API_KEY=sk-your-api-key-here
   ```

3. **Start the application**
   ```bash
   cd docker
   docker-compose up -d
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

### Running Locally

1. **Set up PostgreSQL**
   ```bash
   docker run --name mybillbook-postgres \
     -e POSTGRES_DB=mybillbook \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 -d postgres:15-alpine
   ```

2. **Configure environment variables**
   ```bash
   export OPENAI_API_KEY=sk-your-api-key-here
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=mybillbook
   export DB_USERNAME=postgres
   export DB_PASSWORD=postgres
   ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Usage

### 1. Login (Mock Authentication)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "9876543210",
    "name": "Ramesh",
    "businessName": "Ramesh Medical Store"
  }'
```

**Response:**
```json
{
  "userId": 1,
  "token": "mock-jwt-token",
  "message": "Login successful"
}
```

### 2. Upload Invoices

```bash
curl -X POST 'http://localhost:8080/api/invoices/upload?userId=1' \
  -H "Content-Type: application/json" \
  -d '[
    {
      "invoiceNumber": "INV101",
      "customerName": "Suresh Traders",
      "totalAmount": 10000,
      "pendingAmount": 10000,
      "invoiceDate": "2025-01-15",
      "status": "UNPAID"
    }
  ]'
```

### 3. Upload Payments

```bash
curl -X POST 'http://localhost:8080/api/payments/upload?userId=1' \
  -H "Content-Type: application/json" \
  -d '[
    {
      "amount": 5000,
      "paymentDate": "2025-01-20",
      "paymentMode": "UPI",
      "remark": "INV101 partial payment",
      "status": "UNRECONCILED"
    }
  ]'
```

### 4. Run AI Reconciliation

```bash
curl -X POST 'http://localhost:8080/api/reconciliation/run?userId=1'
```

**Response:**
```json
{
  "suggestionsGenerated": 28,
  "message": "AI reconciliation completed successfully"
}
```

### 5. Get Suggestions

```bash
curl -X GET 'http://localhost:8080/api/reconciliation/suggestions?userId=1'
```

**Response:**
```json
[
  {
    "id": 1,
    "payment": {
      "id": 1,
      "amount": 5000,
      "remark": "INV101 partial payment"
    },
    "invoice": {
      "id": 1,
      "invoiceNumber": "INV101",
      "customerName": "Suresh Traders",
      "pendingAmount": 10000
    },
    "confidence": 0.92,
    "reasoning": "Remark explicitly mentions INV101 and amount is half of pending",
    "status": "PENDING"
  }
]
```

### 6. Bulk Confirm High Confidence

```bash
curl -X POST 'http://localhost:8080/api/reconciliation/bulk-confirm-high-confidence?userId=1&minConfidence=0.90'
```

## Demo Scenario

A complete demo scenario with **80 invoices** and **30 payments** is included in `sample-data/`:

1. **Login** as Ramesh (9876543210)
2. **Upload 80 invoices** from `sample-data/invoices.csv`
3. **Upload 30 payments** from `sample-data/payments.csv`
4. **Run AI reconciliation** - generates 28+ suggestions
5. **Review suggestions** - categorized by confidence
6. **Bulk confirm** high-confidence matches (>0.90)
7. **Manual review** medium-confidence matches (0.70-0.90)
8. **View summary report** - reconciliation statistics

## Database Schema

### Users
```sql
id, mobile_number, name, business_name, created_at
```

### Invoices
```sql
id, user_id, invoice_number, customer_name, total_amount,
pending_amount, status, invoice_date, created_at, updated_at
```

### Payments
```sql
id, user_id, amount, payment_date, payment_mode,
remark, status, created_at
```

### Reconciliation Suggestions
```sql
id, payment_id, invoice_id, confidence, reasoning,
status, ai_model, created_at, confirmed_at, confirmed_by
```

## Testing

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=InvoiceRepositoryTest
```

### Generate coverage report
```bash
mvn jacoco:report
# Open target/site/jacoco/index.html
```

**Current Coverage**: 80%+ across all layers

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Project Structure

```
mybillbook/
├── src/
│   ├── main/
│   │   ├── java/com/mybillbook/
│   │   │   ├── config/          # OpenAI, Swagger, Security config
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── model/           # JPA entities
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── enums/           # Status enums
│   │   │   └── exception/       # Exception handling
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # Flyway SQL migrations
│   └── test/                    # JUnit tests
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── sample-data/
│   ├── invoices.csv             # 80 sample invoices
│   └── payments.csv             # 30 sample payments
├── postman/
│   └── AI-Reconciliation.postman_collection.json
└── pom.xml
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key (required) | - |
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | mybillbook |
| `DB_USERNAME` | Database user | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `OPENAI_MODEL` | OpenAI model | gpt-4o-mini |
| `OPENAI_MAX_TOKENS` | Max tokens per request | 1000 |
| `OPENAI_TEMPERATURE` | AI temperature | 0.3 |
| `SERVER_PORT` | Application port | 8080 |

## OpenAI Integration

The system uses OpenAI GPT-4o-mini with a carefully crafted prompt:

- **Analyzes**: Payment remarks, amounts, dates, customer names
- **Matches**: Against pending/partially paid invoices
- **Returns**: Best matches with confidence scores and reasoning
- **Handles**: Partial payments, overpayments, fuzzy name matching
- **Threshold**: Minimum confidence of 0.60

## Performance Considerations

- **Connection Pooling**: HikariCP with 10 max connections
- **OpenAI Timeout**: 30 seconds with retry logic
- **Indexing**: Database indexes on user_id, status, customer_name
- **Batch Processing**: Bulk confirmation for efficiency
- **Caching**: (Optional) Redis for frequently accessed data

## Error Handling

- **Global Exception Handler**: Standardized error responses
- **OpenAI Failures**: Graceful degradation with retry logic
- **Validation Errors**: Descriptive messages for bad requests
- **Resource Not Found**: Proper 404 responses
- **Database Errors**: Transaction rollback and error logging

## Security Considerations

- **Non-root Docker user**: Security best practice
- **Mock authentication**: Replace with JWT/OAuth for production
- **SQL Injection**: Prevented via JPA/Hibernate
- **API Key Security**: Never commit API keys to version control
- **Input Validation**: All inputs validated before processing

## Future Enhancements

- [ ] Real JWT authentication
- [ ] Multi-currency support
- [ ] Email notifications for reconciliation summaries
- [ ] Machine learning model for improved matching
- [ ] Export reports to PDF/Excel
- [ ] Webhook integration for accounting software
- [ ] Mobile app for on-the-go reconciliation

## Contributing

This is a demo project for the FloBiz/myBillBook backend developer application. Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass
5. Submit a pull request

## License

MIT License - feel free to use this project for learning and development.

## Author

Built with passion to demonstrate backend engineering excellence for FloBiz/myBillBook.

**Contact**: [Your Name/Email]

## Acknowledgments

- **FloBiz/myBillBook**: For the inspiring problem statement
- **OpenAI**: For the powerful GPT-4o-mini API
- **Spring Boot Community**: For excellent documentation
- **TestContainers**: For reliable integration testing

---

**Happy Reconciling!** 🚀
