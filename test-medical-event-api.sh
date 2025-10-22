#!/bin/bash

# Test script to verify medical events API

echo "=== Testing Medical Events API ==="
echo ""

# First, login to get a token
echo "1. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "Password123!"
  }')

echo "Login response: $LOGIN_RESPONSE"
echo ""

# Extract token (assuming jq is available, otherwise we'll need to parse manually)
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "ERROR: Failed to get token"
  exit 1
fi

echo "Token obtained: ${TOKEN:0:20}..."
echo ""

# Create a medical event
echo "2. Creating medical event..."
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/medical-events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "eventTime": "2025-10-05T20:00:00",
    "title": "Test Seizure Event",
    "description": "Testing medical event creation",
    "severity": "MODERATE",
    "category": "SYMPTOM"
  }')

echo "Create response: $CREATE_RESPONSE"
echo ""

# Get all medical events using POST /search
echo "3. Fetching all medical events..."
GET_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/medical-events/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "patientId": null,
    "page": 0,
    "size": 10,
    "sortBy": "eventTime",
    "sortDirection": "DESC"
  }')

echo "Get response: $GET_RESPONSE"
echo ""

echo "=== Test Complete ==="
