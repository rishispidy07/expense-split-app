# SplitEase - Capstone Project

Full-stack Java expense splitting & group wallet application.

## Features
- JWT Authentication (Register/Login/Logout)
- Strong Password Validation (PBKDF2)
- Expense CRUD with Year/Month filtering
- Group Management with Wallet
- Settlements & Balance Tracking
- Charts & Reports (Chart.js)
- Income Tracking
- Responsive UI (Inter font, glass navbar)
- Public URL via Cloudflare Tunnel

## Tech Stack
- **Backend:** Java 21, com.sun.net.httpserver, MySQL 8.0
- **Frontend:** HTML5, CSS3, Vanilla JavaScript, Chart.js
- **Auth:** JWT tokens, PBKDF2 hashing (100K iterations)
- **Database:** MySQL with 8 tables
- **Tunnel:** Cloudflared (public URL)

## How to Run
```bash
# Compile
build.bat

# Run server
run.bat

# Run with public URL
start-public.bat
```

## ER Diagram
```mermaid
erDiagram
    users {
        int id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar phone
        decimal balance
        timestamp created_at
        timestamp updated_at
    }

    expense_groups {
        int id PK
        varchar name
        text description
        int created_by FK
        decimal wallet_balance
        timestamp created_at
        timestamp updated_at
    }

    group_members {
        int id PK
        int group_id FK
        int user_id FK
        enum role
        timestamp joined_at
    }

    expenses {
        int id PK
        int group_id FK
        int paid_by FK
        varchar description
        decimal amount
        varchar category
        enum split_type
        int expense_year
        int expense_month
        timestamp created_at
    }

    expense_splits {
        int id PK
        int expense_id FK
        int user_id FK
        decimal amount
        boolean is_settled
        timestamp settled_at
    }

    settlements {
        int id PK
        int from_user_id FK
        int to_user_id FK
        decimal amount
        int group_id FK
        enum status
        timestamp created_at
        timestamp completed_at
    }

    wallet_transactions {
        int id PK
        int group_id FK
        int user_id FK
        enum type
        decimal amount
        text description
        timestamp created_at
    }

    tokens {
        int id PK
        int user_id FK
        varchar token
        timestamp expires_at
        timestamp created_at
    }

    users ||--o{ expense_groups : creates
    users ||--o{ group_members : joins
    expense_groups ||--o{ group_members : has
    users ||--o{ expenses : pays
    expense_groups ||--o{ expenses : contains
    expenses ||--o{ expense_splits : splits
    users ||--o{ expense_splits : owes
    users ||--o{ settlements : sends
    users ||--o{ settlements : receives
    expense_groups ||--o{ settlements : related
    expense_groups ||--o{ wallet_transactions : tracks
    users ||--o{ wallet_transactions : makes
    users ||--o{ tokens : owns
```

## Class Diagram
```mermaid
classDiagram
    class App {
        +main(String[] args)
    }

    class AppServer {
        -HttpServer server
        -Map routes
        +start()
        +stop()
        -registerRoutes()
        -getLocalIp()
    }

    class Config {
        +int SERVER_PORT
        +String SERVER_HOST
        +String STATIC_FILES_DIR
        +String DB_URL
        +String DB_USER
        +String DB_PASSWORD
        +String JWT_SECRET
        +int JWT_EXPIRY_HOURS
        +int PASSWORD_ITERATIONS
    }

    class AuthService {
        +register(String username, String email, String password, String fullName, String phone) Map
        +login(String username, String password) Map
        +logout(String token) Map
    }

    class PasswordValidator {
        +validate(String password) ValidationResult
        +hashPassword(String password) String
        +verifyPassword(String password, String storedHash) boolean
    }

    class PasswordValidator.ValidationResult {
        -boolean valid
        -String message
        +isValid() boolean
        +getMessage() String
    }

    class TokenManager {
        -Map tokenStore
        +generateToken(int userId) String
        +validateToken(String token) int
        +invalidateToken(String token)
    }

    class DatabaseManager {
        -Connection connection
        +getConnection() Connection
        +createUser(String username, String email, String hash, String fullName, String phone) int
        +findUserByUsername(String username) Map
        +findUserById(int id) Map
        +getAllUsers() List
        +createGroup(String name, String desc, int createdBy) int
        +getUserGroups(int userId) List
        +addMemberToGroup(int groupId, int userId, String role) boolean
        +getGroupMembers(int groupId) List
        +removeMember(int groupId, int userId) boolean
        +createExpense(Integer groupId, int paidBy, String desc, double amount, String category, String splitType, int year, int month) int
        +getUserExpenses(int userId) List
        +getGroupExpenses(int groupId) List
        +createSettlement(int from, int to, double amount, Integer groupId) int
        +getUserSettlements(int userId) List
        +updateSettlementStatus(int id, String status) boolean
        +updateGroupWallet(int groupId, double amount) boolean
        +addWalletTransaction(int groupId, int userId, String type, double amount, String desc) boolean
        +getWalletTransactions(int groupId) List
        +getCategorySummary(int userId, int year, int month) List
        +getMonthlySummary(int userId, int year) List
        +getMonthlyTotals(int userId, int year, int month) Map
        +getGroupSummary(int userId) List
        +getUserProfile(int userId) Map
    }

    class AuthHandler {
        -String action
        +handle(HttpExchange exchange)
    }

    class ExpenseHandler {
        -String action
        +handle(HttpExchange exchange)
    }

    class GroupHandler {
        +handle(HttpExchange exchange)
    }

    class SettlementHandler {
        +handle(HttpExchange exchange)
    }

    class WalletHandler {
        +handle(HttpExchange exchange)
    }

    class StatsHandler {
        +handle(HttpExchange exchange)
    }

    class UserHandler {
        -String action
        +handle(HttpExchange exchange)
    }

    class StaticFileHandler {
        +handle(HttpExchange exchange)
    }

    class User {
        -int id
        -String username
        -String email
        -String fullName
        -String phone
        -double balance
    }

    class Expense {
        -int id
        -Integer groupId
        -int paidBy
        -String description
        -double amount
        -String category
        -String splitType
        -int expenseYear
        -int expenseMonth
    }

    class Group {
        -int id
        -String name
        -String description
        -int createdBy
        -double walletBalance
    }

    class Settlement {
        -int id
        -int fromUserId
        -int toUserId
        -double amount
        -Integer groupId
        -String status
    }

    class JsonUtil {
        +toJson(Map) String
        +toJsonValue(Object) String
        +parse(String) Map
        +getString(Map, String, String) String
        +getInt(Map, String, int) int
        +getDouble(Map, String, double) double
    }

    class RequestParser {
        +parseQuery(String) Map
        +readBody(HttpExchange) String
        +parseJsonBody(HttpExchange) Map
        +extractToken(HttpExchange) String
        +extractUserId(HttpExchange) int
    }

    App --> AppServer
    AppServer --> Config
    AppServer --> AuthHandler
    AppServer --> ExpenseHandler
    AppServer --> GroupHandler
    AppServer --> SettlementHandler
    AppServer --> WalletHandler
    AppServer --> StatsHandler
    AppServer --> UserHandler
    AppServer --> StaticFileHandler
    AuthHandler --> AuthService
    AuthHandler --> TokenManager
    AuthService --> PasswordValidator
    AuthService --> DatabaseManager
    TokenManager --> DatabaseManager
    ExpenseHandler --> DatabaseManager
    GroupHandler --> DatabaseManager
    SettlementHandler --> DatabaseManager
    WalletHandler --> DatabaseManager
    StatsHandler --> DatabaseManager
    UserHandler --> DatabaseManager
    StaticFileHandler --> Config
    DatabaseManager --> Config
```

## Project Structure
```
expense-split-app/
├── frontend/              # UI files
│   ├── *.html             # 12 pages
│   ├── css/style.css      # Styles
│   └── js/                # 10 scripts
├── backend/               # Java server
│   ├── src/               # Source code
│   ├── bin/               # Compiled classes
│   ├── lib/               # MySQL driver
│   └── sql/schema.sql     # Database schema
├── .vscode/               # VS Code config
├── build.bat              # Compile
├── run.bat                # Run server
└── start-public.bat       # Public URL
```

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Login |
| POST | /api/auth/logout | Logout |
| GET | /api/users/me | Current user |
| GET | /api/users | All users |
| POST | /api/groups | Create group |
| GET | /api/groups | List groups |
| GET | /api/groups/:id | Group details |
| POST | /api/groups/:id/members | Add member |
| GET | /api/groups/:id/members | List members |
| DELETE | /api/groups/:id/members/:uid | Remove member |
| POST | /api/expenses | Create expense |
| GET | /api/expenses | List expenses |
| GET | /api/groups/:id/expenses | Group expenses |
| POST | /api/settlements | Create settlement |
| PUT | /api/settlements/:id/complete | Complete settlement |
| GET | /api/settlements | List settlements |
| POST | /api/wallet/:groupId/deposit | Deposit |
| POST | /api/wallet/:groupId/withdraw | Withdraw |
| GET | /api/wallet/:groupId/transactions | Transactions |
| GET | /api/stats/category | Category summary |
| GET | /api/stats/monthly | Monthly summary |
| GET | /api/stats/summary | Monthly totals |
| GET | /api/stats/groups | Group summary |
| GET | /api/user/profile | User profile |
