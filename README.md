# 💸 Expense Tracker — Backend

A production-ready REST API for a personal expense tracking application, built with **Spring Boot 3** and **MongoDB Atlas**. Features include expense management with dynamic filtering and pagination, budget tracking, analytics, CSV export, and an **AI-powered financial analysis** feature using Google Gemini.

---

## 🚀 Features

### 🔐 Authentication
- Signup and Login via username **or** email
- JWT-based authentication (Spring Security + JJWT)
- Password hashing with BCrypt
- Token expiry handling — filter chain returns `401` on expired tokens

### 💰 Expense Management
- Add, update, delete expenses
- Delete all expenses (with confirmation flag)
- Dynamic filtering by category, payment method, amount range, and date range
- Pagination and sorting (by date, amount, or title)
- CSV export with filters applied

### 📊 Analytics
- Total expenses summary (all time, this year, this month, average)
- Category-wise breakdown (filterable by year and/or month)
- Monthly expense totals by year
- Monthly expense breakdown by category

### 🗓️ Budget Management
- Set monthly budget
- View current month's budget vs actual spending
- Returns remaining amount and status (`SAFE` / `EXCEEDED`)

### 🤖 AI Financial Analysis (Gemini)
- User provides monthly income, savings goal, and financial priority
- Backend builds a financial snapshot (spending, category breakdown, savings rate)
- Sends structured prompt to **Gemini 2.5 Flash**
- Returns: financial health score, summary, strengths, risks, recommendations, spending behaviour
- **Smart caching** — re-uses previous report if expenses and inputs haven't changed

### 👤 User Management
- Get profile
- Update username and password
- Delete account (cascades — removes all expenses, budget, and AI reports)

---

## 🧱 Tech Stack

| Layer | Technology |
|-------|------------|
| **Framework** | Spring Boot 3 |
| **Security** | Spring Security, JWT (JJWT) |
| **Database** | MongoDB Atlas, Spring Data MongoDB |
| **AI** | Google Gemini 2.5 Flash API |
| **Deployment** | Render (via Docker) |

---

## 🔗 API Endpoints

### Auth (`/auth`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/auth/health-check` | No | Health check |
| POST | `/auth/signup` | No | Register new user |
| POST | `/auth/login` | No | Login, returns JWT |

### Expenses (`/expenses`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/expenses` | Yes | Get expenses (filtered + paginated) |
| POST | `/expenses` | Yes | Add expense |
| PUT | `/expenses/{id}` | Yes | Update expense |
| DELETE | `/expenses/{id}` | Yes | Delete expense |
| DELETE | `/expenses?confirm=true` | Yes | Delete all expenses |
| GET | `/expenses/export` | Yes | Export as CSV (filters apply) |
| GET | `/expenses/view-budget` | Yes | View current month budget |
| POST | `/expenses/set-budget` | Yes | Set monthly budget |

### Analytics (`/expenses/analytics`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/expenses/analytics/summary` | Yes | Overall expense summary |
| GET | `/expenses/analytics/by-category` | Yes | Category totals (optional year/month) |
| GET | `/expenses/analytics/monthly?year=` | Yes | Monthly totals for a year |
| GET | `/expenses/analytics/monthly-by-category?year=` | Yes | Monthly totals by category |

### AI (`/ai`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/ai/financial-analysis` | Yes | Generate AI financial report |

### User (`/user`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/user/me` | Yes | Get user profile |
| PUT | `/user` | Yes | Update username/password |
| DELETE | `/user` | Yes | Delete account |

All protected endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## ⚙️ Key Architectural Decisions

- **LocalDate stored as String** — custom MongoDB read/write converters prevent UTC/IST timezone corruption
- **BigDecimal → double for MongoDB criteria** — avoids type mismatch in range queries
- **`@CompoundIndex`** — enforces uniqueness at the database level (e.g. one budget per user per month)
- **AI response caching** — Gemini is only called when expenses or user inputs have actually changed
- **Login by username or email** — `UserDetailsService` checks for `@` to decide lookup strategy
- **`@Transactional` on account deletion** — ensures expenses, AI reports, and budgets are all cleaned up atomically

---

## 🔧 Running Locally

### Prerequisites
- Java 21
- Maven
- MongoDB Atlas account
- Google Gemini API key

### Setup

1. Clone the repo
2. Create `src/main/resources/application-local.properties`:

```properties
JWT_SECRET=your_jwt_secret
MONGODB_URI=your_mongodb_connection_string
GEMINI_API_KEY=your_gemini_api_key
```

3. Make sure `application.properties` has:

```properties
spring.profiles.active=local
```

4. Run:

```bash
mvn spring-boot:run
```

API will be available at `http://localhost:8080`

---

## 🌍 Deployment

| Part | Platform | Status |
|------|----------|--------|
| Backend | Render (Docker) | *(add URL after deployment)* |
| Frontend | *(in development)* | — |

Render deployment uses the included `Dockerfile` (multi-stage Maven build). Set `JWT_SECRET`, `MONGODB_URI`, and `GEMINI_API_KEY` as environment variables in Render's dashboard.

---

## 🔒 Security Notes
- No secrets in source code — all sensitive values via environment variables
- JWT expiry enforced at filter level with immediate `401` response
- Ownership verified before update/delete operations
- CORS configured for allowed frontend origins only

---

## 📄 License
Open-source. Free to use for learning or portfolio purposes.