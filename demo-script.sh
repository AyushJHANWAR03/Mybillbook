#!/bin/bash

# AI Invoice Reconciliation Demo Script
# This script demonstrates the complete workflow of the system

set -e

BASE_URL="http://localhost:8080"
USER_ID=""

echo "========================================="
echo "AI Invoice Reconciliation System - Demo"
echo "========================================="
echo ""

# Step 1: Login
echo "Step 1: Logging in..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "9876543210",
    "name": "Ramesh",
    "businessName": "Ramesh Medical Store"
  }')

USER_ID=$(echo "$RESPONSE" | grep -o '"userId":[0-9]*' | grep -o '[0-9]*')
echo "✓ Logged in successfully! User ID: $USER_ID"
echo ""

# Step 2: Upload Invoices
echo "Step 2: Uploading sample invoices..."
curl -s -X POST "${BASE_URL}/api/invoices/upload?userId=${USER_ID}" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "invoiceNumber": "INV101",
      "customerName": "Suresh Traders",
      "totalAmount": 10000,
      "pendingAmount": 10000,
      "invoiceDate": "2025-01-15",
      "status": "UNPAID"
    },
    {
      "invoiceNumber": "INV102",
      "customerName": "Mukesh Pharma",
      "totalAmount": 25000,
      "pendingAmount": 15000,
      "invoiceDate": "2025-01-16",
      "status": "PARTIALLY_PAID"
    },
    {
      "invoiceNumber": "INV103",
      "customerName": "Rajesh Stores",
      "totalAmount": 5000,
      "pendingAmount": 5000,
      "invoiceDate": "2025-01-17",
      "status": "UNPAID"
    },
    {
      "invoiceNumber": "INV104",
      "customerName": "Dinesh Medical",
      "totalAmount": 15000,
      "pendingAmount": 15000,
      "invoiceDate": "2025-01-18",
      "status": "UNPAID"
    },
    {
      "invoiceNumber": "INV105",
      "customerName": "Ramesh Enterprises",
      "totalAmount": 8000,
      "pendingAmount": 8000,
      "invoiceDate": "2025-01-19",
      "status": "UNPAID"
    }
  ]' > /dev/null
echo "✓ 5 sample invoices uploaded successfully"
echo ""

# Step 3: Upload Payments
echo "Step 3: Uploading sample payments..."
curl -s -X POST "${BASE_URL}/api/payments/upload?userId=${USER_ID}" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "amount": 5000,
      "paymentDate": "2025-01-20",
      "paymentMode": "UPI",
      "remark": "INV101 partial payment",
      "status": "UNRECONCILED"
    },
    {
      "amount": 10000,
      "paymentDate": "2025-01-21",
      "paymentMode": "CASH",
      "remark": "mukesh invoice 102 remaining",
      "status": "UNRECONCILED"
    },
    {
      "amount": 5000,
      "paymentDate": "2025-01-22",
      "paymentMode": "BANK_TRANSFER",
      "remark": "rajesh stores bill",
      "status": "UNRECONCILED"
    },
    {
      "amount": 15000,
      "paymentDate": "2025-01-23",
      "paymentMode": "UPI",
      "remark": "dinesh payment",
      "status": "UNRECONCILED"
    },
    {
      "amount": 8000,
      "paymentDate": "2025-01-24",
      "paymentMode": "CASH",
      "remark": "ramesh enterprises full payment",
      "status": "UNRECONCILED"
    }
  ]' > /dev/null
echo "✓ 5 sample payments uploaded successfully"
echo ""

# Step 4: Check unreconciled payments
echo "Step 4: Checking unreconciled payments..."
UNRECONCILED=$(curl -s -X GET "${BASE_URL}/api/payments?userId=${USER_ID}&status=UNRECONCILED")
COUNT=$(echo "$UNRECONCILED" | grep -o '"id":' | wc -l | tr -d ' ')
echo "✓ Found $COUNT unreconciled payments"
echo ""

# Step 5: Run AI Reconciliation
echo "Step 5: Running AI reconciliation..."
echo "⚠️  Note: This requires OPENAI_API_KEY to be set"
echo "If OpenAI is not configured, this step will fail gracefully"
RECON_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/reconciliation/run?userId=${USER_ID}" || echo '{"message":"OpenAI not configured"}')
echo "$RECON_RESPONSE"
echo ""

# Step 6: Get suggestions
echo "Step 6: Fetching AI suggestions..."
SUGGESTIONS=$(curl -s -X GET "${BASE_URL}/api/reconciliation/suggestions?userId=${USER_ID}" || echo '[]')
SUGG_COUNT=$(echo "$SUGGESTIONS" | grep -o '"id":' | wc -l | tr -d ' ')
echo "✓ Found $SUGG_COUNT AI suggestions"
if [ "$SUGG_COUNT" -gt 0 ]; then
  echo ""
  echo "Sample suggestion:"
  echo "$SUGGESTIONS" | head -20
fi
echo ""

# Step 7: Get summary report
echo "Step 7: Generating summary report..."
SUMMARY=$(curl -s -X GET "${BASE_URL}/api/reports/summary?userId=${USER_ID}")
echo "$SUMMARY"
echo ""

echo "========================================="
echo "Demo completed successfully!"
echo "========================================="
echo ""
echo "Next steps:"
echo "1. Open Swagger UI: ${BASE_URL}/swagger-ui.html"
echo "2. Import Postman collection: postman/AI-Reconciliation.postman_collection.json"
echo "3. Review AI suggestions and confirm/reject them"
echo "4. Use bulk operations for high-confidence matches"
echo ""
