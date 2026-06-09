# FirstClub Membership Service

A backend system for a subscription-based Membership Program built with **Java 17** and **Spring Boot 3**.

---

## Features

- **3 Membership Plans** — Monthly, Quarterly, Yearly with configurable pricing
- **3 Membership Tiers** — Silver, Gold, Platinum with configurable benefits per tier
- **Tier Eligibility Rules** — Users qualify for tiers based on order count, order value, or user cohort (DB-configurable, no code changes needed)
- **Full Subscription Lifecycle** — Subscribe, upgrade tier, downgrade tier, cancel
- **Concurrency Safe** — Optimistic locking via `@Version` prevents race conditions on simultaneous updates
- **Clean Layered Architecture** — Controller → Service → Repository separation with interface-driven design

---

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA + Hibernate
- H2 In-Memory Database (zero setup for demo)
- Maven

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+

### Run the application

```bash
git clone https://github.com/YOUR_USERNAME/firstclub-membership-service.git
cd firstclub-membership-service
mvn spring-boot:run
```

The app starts on **http://localhost:8080**

On startup, the database is automatically seeded with:
- 3 membership plans
- 3 tiers (Silver / Gold / Platinum) with benefits
- Tier eligibility rules
- 2 demo users (IDs: 1 and 2)

---

## API Reference

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users` | Create a user |
| GET | `/api/v1/users/{userId}` | Get user details |

### Plans & Tiers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/membership/plans` | Get all active plans |
| GET | `/api/v1/membership/tiers` | Get all tiers with benefits |

### Subscription
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/membership/subscribe` | Subscribe to a plan + tier |
| GET | `/api/v1/membership/status/{userId}` | Get membership status & expiry |
| PUT | `/api/v1/membership/upgrade/{userId}` | Upgrade tier |
| PUT | `/api/v1/membership/downgrade/{userId}` | Downgrade tier |
| DELETE | `/api/v1/membership/cancel/{userId}` | Cancel membership |

### Orders (for tier evaluation)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Place an order |
| GET | `/api/v1/orders/user/{userId}` | Get all orders for a user |

---

## Sample API Calls

**1. Get all plans**
```
GET http://localhost:8080/api/v1/membership/plans
```

**2. Subscribe user 1 to Monthly plan, Silver tier**
```
POST http://localhost:8080/api/v1/membership/subscribe
Content-Type: application/json

{
  "userId": 1,
  "planId": 1,
  "tierId": 1
}
```

**3. Place an order (feeds into tier evaluation)**
```
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "userId": 1,
  "orderValue": 1500.00
}
```

**4. Upgrade to Gold tier**
```
PUT http://localhost:8080/api/v1/membership/upgrade/1
Content-Type: application/json

{
  "tierId": 2
}
```

**5. Check membership status**
```
GET http://localhost:8080/api/v1/membership/status/1
```

---

## Tier Eligibility Rules

| Tier | Rule |
|------|------|
| Silver | Open to all users |
| Gold | ≥ 3 orders in last 30 days **OR** ≥ ₹3,000 spend in last 30 days |
| Platinum | ≥ 10 orders in last 30 days **OR** ≥ ₹10,000 spend **OR** user cohort = `VIP` |

Rules are stored in the database and are fully configurable without code changes.

---

## Project Structure

```
src/main/java/com/firstclub/membership/
├── controller/         # REST API endpoints
├── service/            # Business logic (interface + implementation)
├── repository/         # Database access layer
├── entity/             # Database table models
├── dto/                # Request / Response objects
├── enums/              # PlanDuration, MembershipStatus, RuleCriteria
├── exception/          # Custom exceptions + global error handler
└── config/             # Demo data seeder
```

---

## Design Highlights

**Extensibility** — Adding a new tier upgrade criteria (e.g., referral count) requires adding one enum value and one `case` in `TierEvaluationService`. No other changes.

**Configurable Benefits** — Tier benefits are stored as key-value pairs in the database (`TierBenefit` table), so they can be updated without redeployment.

**Concurrency** — `UserMembership` uses `@Version` for optimistic locking. Simultaneous upgrade/cancel requests on the same membership are handled safely.

**Transactions** — All write operations use `@Transactional`, ensuring partial failures are rolled back atomically.

---

## Running Tests

```bash
mvn test
```

---

## H2 Database Console

Available at **http://localhost:8080/h2-console** while the app is running.

```
JDBC URL:  jdbc:h2:mem:membershipdb
Username:  sa
Password:  (leave blank)
```
