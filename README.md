E-commerce Backend — Transaction-Safe Domain System

Backend-only e-commerce system focused on transactional integrity, SQL-first design, and realistic enterprise domain logic.
Built to demonstrate robust backend and database engineering without relying on a frontend UI.

Tech Stack

Java 17 · Spring Boot · PostgreSQL 16 · Flyway · JPA (Hibernate) · REST

Core Concepts

SQL-first schema design with Flyway migrations and strong integrity constraints (FK, UNIQUE, CHECK)

Transaction-safe checkout and inventory consistency using pessimistic locking

Idempotent payment processing backed by database-level guarantees

Append-only audit trails (order status history, inventory movement ledger)

Read-only audit endpoints for full traceability

Architecture

Database → Domain Model → Service Layer (transactions) → REST API
No UI by design (backend/database focus).

Demo Flow (Postman / curl)

Base URL:
http://localhost:8081

1) Create product

POST /api/products

Request body:
{
"sku": "SKU-001",
"name": "T-Shirt"
}

2) Create customer

POST /api/customers

Request body:
{
"email": "test+<timestamp>@example.com"
}

3) Create cart

POST /api/carts

Request body:
{
"customerId": 1
}

4) Add item to cart

POST /api/carts/{cartId}/items

Request body:
{
"productId": 1,
"quantity": 2
}

5) Checkout cart

POST /api/carts/{cartId}/checkout

6) Process payment (idempotent)

POST /api/orders/{orderId}/payments

Request body:
{
"idempotencyKey": "pay-{orderId}-001",
"outcome": "SUCCESS"
}

Replaying the same request with the same idempotencyKey will return the same result, demonstrating idempotent behavior.

Audit & Traceability

Order history: GET /api/orders/{orderId}/history

Inventory movements: GET /api/products/{productId}/movements

Run Locally
Prerequisites

Java 17

PostgreSQL 16

Database setup (example local development)

Database: ecommerce_db

User: ecommerce_app

Port: 5432

Build

./mvnw clean package

Run

./mvnw spring-boot:run

Application runs at:
http://localhost:8081