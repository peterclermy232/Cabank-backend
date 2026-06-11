# CaBank Backend — Spring Boot REST API

A full-featured banking REST API built with Spring Boot 3, JWT authentication, and Neon (serverless PostgreSQL).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | Neon (PostgreSQL) |
| Auth | JWT (jjwt 0.12) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Validation |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/cabank/
├── CaBankApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── Controllers.java        ← All REST controllers
├── dto/
│   ├── request/Requests.java   ← All request DTOs
│   └── response/Responses.java ← All response DTOs
├── entity/
│   ├── BaseEntity.java
│   ├── User.java
│   ├── Account.java
│   ├── Card.java
│   ├── Transaction.java
│   ├── Transfer.java
│   ├── BillPayment.java
│   ├── Savings.java
│   ├── ExchangeRate.java
│   ├── Message.java
│   └── Beneficiary.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
├── repository/
│   └── Repositories.java       ← All JPA repositories
├── security/
│   ├── JwtUtils.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
└── service/impl/
    ├── AuthService.java
    ├── AccountCardService.java
    └── Services.java            ← All other services
```

---

## Setup

### 1. Get your Neon connection string

1. Go to https://neon.tech and create a free project
2. Copy the connection string — it looks like:
   ```
   postgresql://username:password@ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```

### 2. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require
spring.datasource.username=your_username
spring.datasource.password=your_password
```

> Neon requires `?sslmode=require` on the URL — it's already set in the template.

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**

Tables are created automatically via `spring.jpa.hibernate.ddl-auto=update`.

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <your_jwt_token>
```

### Auth — `/api/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | ❌ | Register new user |
| POST | `/api/auth/signin` | ❌ | Login, get JWT |
| GET | `/api/auth/me` | ✅ | Get current user profile |
| PUT | `/api/auth/me` | ✅ | Update profile |
| POST | `/api/auth/change-password` | ✅ | Change password |

**Sign Up**
```json
POST /api/auth/signup
{
  "name": "Peter Atito",
  "email": "peter@cabank.com",
  "password": "Password123!",
  "phone": "+254712345678"
}
```

**Sign In**
```json
POST /api/auth/signin
{
  "email": "peter@cabank.com",
  "password": "Password123!"
}
```
Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "user": { "id": "...", "name": "Peter Atito", "email": "peter@cabank.com" }
  }
}
```

---

### Accounts — `/api/accounts`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/accounts` | Get all user accounts |
| GET | `/api/accounts/{id}` | Get single account |

---

### Cards — `/api/cards`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cards` | Get all user cards |
| GET | `/api/cards/{id}` | Get card detail |
| POST | `/api/cards` | Add a new card |
| DELETE | `/api/cards/{id}` | Delete card |

---

### Transactions — `/api/transactions`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/transactions?page=0&size=20` | Paginated transaction history |
| GET | `/api/transactions/recent` | Last 10 transactions |

---

### Transfers — `/api/transfers`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transfers` | Make a transfer |
| GET | `/api/transfers` | Transfer history |

**Create Transfer**
```json
POST /api/transfers
{
  "fromCardLast4": "9018",
  "toAccountNumber": "1900 8988 1234",
  "beneficiaryName": "Emma",
  "amount": 500.00,
  "note": "Rent payment"
}
```

---

### Bills — `/api/bills`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/bills/pay` | Pay a bill |
| GET | `/api/bills/history` | Bill payment history |

**Pay Bill**
```json
POST /api/bills/pay
{
  "billType": "electric",
  "billCode": "23435643",
  "customerName": "Jackson Maine",
  "customerAddress": "403 East 4th Street, Santa Ana",
  "amount": 470.00
}
```

---

### Savings — `/api/savings`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/savings` | Open a savings account |
| GET | `/api/savings` | Get all savings |

---

### Exchange Rates — `/api/exchange-rates`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/exchange-rates` | ❌ | Get all exchange rates (public) |

---

### Messages — `/api/messages`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/messages` | Get all messages |
| PATCH | `/api/messages/{id}/read` | Mark as read |
| GET | `/api/messages/unread-count` | Get unread count |

---

### Beneficiaries — `/api/beneficiaries`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/beneficiaries` | Get all beneficiaries |
| POST | `/api/beneficiaries` | Add beneficiary |
| DELETE | `/api/beneficiaries/{id}` | Remove beneficiary |

---

## Connecting the React Native App

Replace your mock data calls with real API calls. Example using `fetch`:

```javascript
// api/client.js
const BASE_URL = 'http://10.0.2.2:8080/api'; // Android emulator → localhost

export const apiClient = async (endpoint, options = {}, token = null) => {
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };
  const res = await fetch(`${BASE_URL}${endpoint}`, { ...options, headers });
  return res.json();
};

// Sign in
export const signIn = (email, password) =>
  apiClient('/auth/signin', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });

// Get transactions (authenticated)
export const getTransactions = (token) =>
  apiClient('/transactions/recent', {}, token);
```

> Use `10.0.2.2` instead of `localhost` for Android emulator to reach your Mac's localhost.

---

## Response Format

All endpoints return a consistent envelope:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

Errors:
```json
{
  "success": false,
  "message": "Email is already registered",
  "data": null
}
```
