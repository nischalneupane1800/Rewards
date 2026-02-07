# Rewards WEB API

A reactive REST API that calculates reward points for customers based on transaction history. The API uses Spring WebFlux and an in-memory dataset (no database) to keep the solution simple and easy to run.
## Reward Rules

- **2 points** for every dollar spent **over $100**
- **1 point** for every dollar spent **over $50 up to $100**
- Example: **$120** → (2 × 20) + (1 × 50) = **90** points

Notes on boundaries:
- $50.00 → **0** points
- $100.00 → **50** points

## Tech Stack

- Java 17
- Spring Boot (WebFlux)
- Maven
- JUnit 5 + Reactor Test (StepVerifier)

## How to Run

### Prerequisites
- Java 17 installed (`java -version`)
- Maven Wrapper included (`./mvnw`)

### Start the app
From the project root:
```bash
./mvnw spring-boot:run
```

The app runs at: http://localhost:8081 

Port is configured in application.yml.

