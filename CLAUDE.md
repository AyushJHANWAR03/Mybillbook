# AI-Powered Invoice Reconciliation System

## Project Overview
Automated invoice-payment reconciliation system using OpenAI to eliminate manual bookkeeping for small businesses. Built for FloBiz/myBillBook backend developer application.

**COMPLETE FULL-STACK APPLICATION**: Backend (Spring Boot + PostgreSQL) + Frontend (React)

## Problem Statement
Small businesses face challenges reconciling payments to invoices:
- 100 invoices, multiple partial payments per invoice
- Messy payment remarks: "ramesh bill", "inv 101", "INVOICE-101"
- Manual matching is time-consuming and error-prone

## Solution
AI analyzes payment remarks, amounts, customer names and suggests invoice matches with confidence scores and reasoning.

**Demo Frontend**: Interactive React app showcasing AI reconciliation in action!

---

## Technical Stack

### Core
- **Java**: 17+
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 15
- **AI Engine**: OpenAI GPT-4o-mini
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, TestContainers

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **API Docs**: Swagger/OpenAPI 3.0
- **Cache** (Optional): Redis

### Development Approach
- **TDD (Test-Driven Development)**: Write tests first, then implementation
- **Clean Architecture**: Clear separation of concerns
- **RESTful APIs**: Standard HTTP methods and status codes

---

## Database Schema

### 1. users
```sql
id              BIGSERIAL PRIMARY KEY
mobile_number   VARCHAR(10) UNIQUE NOT NULL
name            VARCHAR(100)
business_name   VARCHAR(200)
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 2. invoices
```sql
id              BIGSERIAL PRIMARY KEY
user_id         BIGINT REFERENCES users(id)
invoice_number  VARCHAR(50) UNIQUE NOT NULL
customer_name   VARCHAR(200) NOT NULL
total_amount    DECIMAL(10,2) NOT NULL
pending_amount  DECIMAL(10,2) NOT NULL
status          VARCHAR(20) NOT NULL  -- UNPAID, PARTIALLY_PAID, FULLY_PAID
invoice_date    DATE NOT NULL
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP

INDEX idx_user_status (user_id, status)
INDEX idx_customer_name (customer_name)
```

### 3. payments
```sql
id              BIGSERIAL PRIMARY KEY
user_id         BIGINT REFERENCES users(id)
amount          DECIMAL(10,2) NOT NULL
payment_date    DATE NOT NULL
payment_mode    VARCHAR(20) NOT NULL  -- CASH, UPI, CARD, BANK_TRANSFER
remark          TEXT
status          VARCHAR(20) NOT NULL  -- UNRECONCILED, RECONCILED
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP

INDEX idx_user_status (user_id, status)
```

### 4. reconciliation_suggestions
```sql
id              BIGSERIAL PRIMARY KEY
payment_id      BIGINT REFERENCES payments(id)
invoice_id      BIGINT REFERENCES invoices(id)
confidence      DECIMAL(3,2) NOT NULL  -- 0.00 to 1.00
reasoning       TEXT NOT NULL
status          VARCHAR(20) NOT NULL  -- PENDING, CONFIRMED, REJECTED
ai_model        VARCHAR(50)            -- e.g., "gpt-4o-mini"
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
confirmed_at    TIMESTAMP
confirmed_by    BIGINT REFERENCES users(id)

INDEX idx_payment_status (payment_id, status)
```

---

## API Endpoints

### Authentication
```
POST   /api/auth/login
Body: { "mobileNumber": "9876543210", "name": "Ramesh", "businessName": "Ramesh Traders" }
Response: { "userId": 1, "token": "mock-jwt-token", "message": "Login successful" }
```

### Invoice Management
```
POST   /api/invoices/upload
Body: { "invoices": [ { "invoiceNumber": "INV101", "customerName": "Suresh Traders", "totalAmount": 10000, "invoiceDate": "2025-01-15" } ] }
Response: { "uploaded": 50, "failed": 0 }

GET    /api/invoices?status=UNPAID
Response: [ { "id": 1, "invoiceNumber": "INV101", "customerName": "Suresh", "totalAmount": 10000, "pendingAmount": 10000, "status": "UNPAID" } ]
```

### Payment Management
```
POST   /api/payments/upload
Body: { "payments": [ { "amount": 5000, "paymentDate": "2025-01-20", "paymentMode": "UPI", "remark": "INV101 partial" } ] }
Response: { "uploaded": 30, "failed": 0 }

GET    /api/payments?status=UNRECONCILED
Response: [ { "id": 1, "amount": 5000, "paymentDate": "2025-01-20", "remark": "INV101 partial", "status": "UNRECONCILED" } ]
```

### AI Reconciliation
```
POST   /api/reconciliation/run
Response: { "totalPayments": 30, "suggestionsGenerated": 28, "failed": 2, "message": "AI reconciliation completed" }

GET    /api/reconciliation/suggestions
Response: [
  {
    "id": 1,
    "payment": { "id": 1, "amount": 5000, "remark": "INV101 partial" },
    "invoice": { "id": 1, "invoiceNumber": "INV101", "customerName": "Suresh Traders", "pendingAmount": 10000 },
    "confidence": 0.92,
    "reasoning": "Remark mentions INV101 and amount is half of pending",
    "status": "PENDING"
  }
]

POST   /api/reconciliation/confirm/{suggestionId}
Response: { "message": "Reconciliation confirmed", "invoiceUpdated": true, "paymentReconciled": true }

POST   /api/reconciliation/reject/{suggestionId}
Response: { "message": "Suggestion rejected" }

POST   /api/reconciliation/bulk-confirm
Body: { "suggestionIds": [1, 2, 3], "minConfidence": 0.90 }
Response: { "confirmed": 3, "message": "Bulk confirmation completed" }
```

### Reporting
```
GET    /api/reports/summary
Response: {
  "totalInvoices": 100,
  "reconciledInvoices": 45,
  "pendingInvoices": 55,
  "totalPayments": 30,
  "reconciledPayments": 28,
  "unreconciledPayments": 2,
  "aiAccuracy": 0.93,
  "totalRevenue": 450000,
  "pendingRevenue": 550000
}
```

---

## OpenAI Integration

### Prompt Template
```
You are a financial reconciliation assistant. Given payment and invoice data, identify the best matching invoice(s) for the payment.

Payment Details:
- Amount: ₹{amount}
- Date: {date}
- Remark: "{remark}"
- Mode: {mode}

Available Invoices (pending/partially paid):
{invoice_list}

Task: Analyze and return JSON response with:
1. Best matching invoice(s)
2. Confidence score (0.0 to 1.0)
3. Clear reasoning

Response Format (STRICT JSON):
{
  "matches": [
    {
      "invoice_number": "INV101",
      "confidence": 0.92,
      "reason": "Remark mentions INV101 explicitly and amount matches half the pending amount"
    }
  ]
}

Rules:
- If amount > invoice pending, flag as overpayment
- Match customer names using fuzzy logic
- Consider invoice number mentions in remarks
- If multiple strong matches exist, return all with confidence scores
- Minimum confidence threshold: 0.60
```

### Response Handling
```java
{
  "matches": [
    {
      "invoice_number": "INV101",
      "confidence": 0.92,
      "reason": "..."
    },
    {
      "invoice_number": "INV103",
      "confidence": 0.75,
      "reason": "..."
    }
  ]
}
```

---

## TDD Approach

### Testing Layers

#### 1. Unit Tests
- **Service Layer**: Mock external dependencies (OpenAI, repositories)
- **Repository Layer**: Use H2 in-memory DB or Testcontainers
- **Utility Classes**: Pure function testing

#### 2. Integration Tests
- **API Tests**: Test full request-response cycle with TestRestTemplate
- **Database Tests**: Use Testcontainers PostgreSQL
- **OpenAI Integration**: Mock OpenAI responses

#### 3. Test Coverage Goals
- Minimum 80% code coverage
- All business logic 100% covered
- Edge cases and error scenarios tested

### TDD Workflow
```
1. Write failing test
2. Write minimal code to pass test
3. Refactor
4. Repeat
```

### Key Test Scenarios

**Invoice Tests**:
- Upload valid invoices → Success
- Upload duplicate invoice → Conflict error
- Fetch invoices by status → Correct filtering
- Update invoice status after reconciliation → Status changes correctly

**Payment Tests**:
- Upload valid payments → Success
- Fetch unreconciled payments → Correct list
- Mark payment as reconciled → Status updates

**Reconciliation Tests**:
- AI returns high confidence match → Suggestion created
- AI returns multiple matches → All suggestions stored
- Confirm suggestion → Invoice + payment updated
- Reject suggestion → No changes to invoice/payment
- Bulk confirm with confidence threshold → Only high-confidence confirmed
- Overpayment scenario → Flagged in reasoning

**OpenAI Service Tests**:
- Successful API response → Parsed correctly
- API timeout → Retry mechanism works
- Invalid JSON response → Error handled gracefully
- Rate limit error → Backoff strategy applied

---

## Project Structure

```
mybillbook/
├── src/
│   ├── main/
│   │   ├── java/com/mybillbook/
│   │   │   ├── MybillbookApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenAIConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── InvoiceController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── ReconciliationController.java
│   │   │   │   └── ReportController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── InvoiceService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── ReconciliationService.java
│   │   │   │   ├── OpenAIService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── InvoiceRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   └── ReconciliationSuggestionRepository.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Invoice.java
│   │   │   │   ├── Payment.java
│   │   │   │   └── ReconciliationSuggestion.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── InvoiceUploadRequest.java
│   │   │   │   ├── PaymentUploadRequest.java
│   │   │   │   ├── ReconciliationResponse.java
│   │   │   │   └── SummaryReportResponse.java
│   │   │   ├── enums/
│   │   │   │   ├── InvoiceStatus.java
│   │   │   │   ├── PaymentStatus.java
│   │   │   │   ├── PaymentMode.java
│   │   │   │   └── SuggestionStatus.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── OpenAIServiceException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── db/migration/
│   │           ├── V1__create_users_table.sql
│   │           ├── V2__create_invoices_table.sql
│   │           ├── V3__create_payments_table.sql
│   │           └── V4__create_reconciliation_suggestions_table.sql
│   └── test/
│       └── java/com/mybillbook/
│           ├── controller/
│           ├── service/
│           ├── repository/
│           └── integration/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── postman/
│   └── AI-Reconciliation.postman_collection.json
├── sample-data/
│   ├── invoices.csv
│   └── payments.csv
├── pom.xml
├── README.md
└── CLAUDE.md (this file)
```

---

## User Flow (Demo Scenario)

### Scenario: Ramesh Traders Business

**Setup**:
- Business: Ramesh Medical Store
- 80 pending invoices from various customers
- 25 new payments received this week

**Step-by-Step Flow**:

1. **Login** (POST /api/auth/login)
   ```
   Mobile: 9876543210
   Name: Ramesh
   Business: Ramesh Medical Store
   ```

2. **Upload Invoices** (POST /api/invoices/upload)
   ```
   80 invoices uploaded
   Customers: Suresh Traders, Mukesh Pharma, Rajesh Stores, etc.
   Amounts: ₹2,000 to ₹50,000
   ```

3. **Upload Payments** (POST /api/payments/upload)
   ```
   25 payments uploaded
   Remarks: "suresh bill", "INV234 payment", "mukesh partial", etc.
   ```

4. **Run AI Reconciliation** (POST /api/reconciliation/run)
   ```
   AI processes 25 payments against 80 invoices
   Generates 23 suggestions (2 payments too ambiguous)
   ```

5. **Review Suggestions** (GET /api/reconciliation/suggestions)
   ```
   High confidence (>0.90): 15 suggestions
   Medium confidence (0.70-0.90): 6 suggestions
   Low confidence (0.60-0.70): 2 suggestions
   ```

6. **Bulk Confirm High Confidence** (POST /api/reconciliation/bulk-confirm)
   ```
   Confirm 15 suggestions with confidence > 0.90
   15 invoices updated
   15 payments marked as reconciled
   ```

7. **Manual Review Medium Confidence**
   ```
   Review 6 suggestions manually
   Confirm: 5
   Reject: 1
   ```

8. **View Summary Report** (GET /api/reports/summary)
   ```
   Total Invoices: 80
   Reconciled: 20
   Pending: 60
   AI Accuracy: 95% (19/20 confirmed)
   ```

---

## Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mybillbook
DB_USERNAME=postgres
DB_PASSWORD=postgres

# OpenAI
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o-mini
OPENAI_MAX_TOKENS=1000
OPENAI_TEMPERATURE=0.3

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Redis (Optional)
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## Docker Setup

### docker-compose.yml
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mybillbook
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_HOST: postgres
      OPENAI_API_KEY: ${OPENAI_API_KEY}
    depends_on:
      - postgres

volumes:
  postgres_data:
```

---

## Sample Data

### Invoices (50-100 samples)
```csv
invoice_number,customer_name,total_amount,pending_amount,invoice_date,status
INV001,Suresh Traders,10000,10000,2025-01-10,UNPAID
INV002,Mukesh Pharma,25000,15000,2025-01-11,PARTIALLY_PAID
INV003,Rajesh Stores,5000,5000,2025-01-12,UNPAID
...
```

### Payments (30-50 samples)
```csv
amount,payment_date,payment_mode,remark
5000,2025-01-20,UPI,suresh bill payment
10000,2025-01-21,CASH,mukesh partial INV002
5000,2025-01-22,BANK_TRANSFER,rajesh stores
...
```

---

## Success Metrics

### Technical
- All tests passing (100%)
- Code coverage > 80%
- API response time < 500ms
- OpenAI integration working with retry logic

### Functional
- 90%+ AI accuracy on sample data
- Handles partial payments correctly
- Flags overpayments
- Bulk confirmation works smoothly

### Demo Impact
- Clean, well-documented code
- Professional README
- Working Postman collection
- Dockerized deployment
- Swagger docs accessible

---

## Development Roadmap

### Phase 1: Foundation (TDD Setup)
- [x] Project structure
- [ ] Database schema + migrations
- [ ] Entity models
- [ ] Repository layer tests + implementation
- [ ] Service layer tests + implementation

### Phase 2: Core Features
- [ ] Authentication (mock)
- [ ] Invoice CRUD
- [ ] Payment CRUD
- [ ] OpenAI integration
- [ ] Reconciliation engine

### Phase 3: Advanced Features
- [ ] Bulk confirmation
- [ ] Reporting endpoints
- [ ] Error handling
- [ ] API documentation

### Phase 4: Deployment
- [ ] Docker setup
- [ ] Sample data generation
- [ ] Postman collection
- [ ] README and demo script

---

## Why This Will Stand Out

1. **Production-Ready Code**: TDD, clean architecture, proper error handling
2. **AI Integration**: Practical use of OpenAI for business automation
3. **Domain Understanding**: Shows understanding of SMB pain points
4. **Complete Solution**: End-to-end working system with demo data
5. **DevOps Practices**: Dockerized, documented, ready to deploy
6. **Attention to Detail**: Edge cases handled, bulk operations, reporting

---

## Next Steps

1. Initialize Spring Boot project with Maven
2. Set up PostgreSQL with Flyway migrations
3. Start with TDD: Write first test for User entity
4. Implement layer by layer (Repository → Service → Controller)
5. Integrate OpenAI
6. Create sample data
7. Package with Docker
8. Polish documentation

---

**Built with passion to demonstrate backend engineering excellence for FloBiz**
