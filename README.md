# 🏆 TheXuong - Sports E-Commerce Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)](https://openjdk.org/)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js)](https://vuejs.org/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-CC2927?logo=microsoft-sql-server)](https://www.microsoft.com/en-us/sql-server)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](/LICENSE)

A modern, full-stack e-commerce platform built for sports equipment retail. Features Google OAuth2 authentication, advanced loyalty program, voucher system, and soft-delete product management.

![Platform Preview](https://via.placeholder.com/1200x630/0F172A/FFFFFF?text=TheXuong+Sports+ECommerce)

---

## ✨ Key Features

### 🔐 Authentication & Authorization
- **Google OAuth2 Login** - One-click sign-in with Google account
- **Role-based Access Control** - ADMIN and CUSTOMER roles
- **JWT Tokens** - Stateless authentication with secure token management

### 🎁 Loyalty & Rewards System
- **Points Accumulation** - Earn 1 point per 100,000 VND spent (on COMPLETED orders)
- **VIP Tiers** - Automatic tier evaluation (THUONG → VIP)
- **VIP Benefits** - Free shipping + bonus points multiplier
- **Points Redemption** - Exchange points for exclusive vouchers

### 🎫 Voucher Catalog
- **6 Denominations:** 10k/1pt, 20k/2pts, 50k/5pts, 100k/10pts, 200k/20pts, 500k/50pts
- **Unique Codes** - Each user receives personalized TX-XXXXXX codes (30-day validity)
- **Checkout Integration** - Apply vouchers directly at payment

### 📦 Product Management
- **Soft Delete** - Deactivate products without losing order history
- **Variant System** - Size-based inventory management
- **Reviews & Ratings** - Customer feedback system
- **View Count Tracking** - Popular products analytics

### 📊 Admin Dashboard
- **Loyalty Reports** - Track points, vouchers, VIP members
- **Product Management** - Full CRUD with soft-delete support
- **Order Management** - Track and update order statuses
- **User Management** - View and manage customer accounts

### ⚡ Performance & Reliability
- **Cron Jobs** - Automated point expiry, voucher cleanup, tier reevaluation
- **SQL Server** - Robust relational database with optimized queries
- **RESTful API** - Clean, documented endpoints

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.5.9 | Application framework |
| **Spring Security** | 6.5 | Authentication & authorization |
| **Spring Data JPA** | 3.5 | Database ORM |
| **SQL Server** | 2019+ | Primary database |
| **Gradle** | 8.x | Build automation |
| **Java** | 21 | Programming language |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Vue 3** | 3.4+ | Progressive JavaScript framework |
| **TypeScript** | 5.x | Type-safe JavaScript |
| **Pinia** | 2.x | State management |
| **Vue Router** | 4.x | Client-side routing |
| **Tailwind CSS** | 3.x | Utility-first CSS framework |
| **Vite** | 5.x | Build tool & dev server |

---

## 🚀 Quick Start

### Prerequisites
- **JDK 21** or higher
- **SQL Server 2019** or higher
- **Node.js 18** or higher (for frontend)
- **Gradle 8.x** (wrapper included)

### 1️⃣ Clone Repository
```bash
git clone https://github.com/yourusername/TheXuong.git
cd TheXuong
```

### 2️⃣ Database Setup
```sql
-- Execute the schema script
sqlcmd -S localhost -U SA -P your_password -i dbTheXuong.sql
```

Update `src/main/resources/application.yml` with your SQL Server credentials:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=dbTheXuong;encrypt=true;trustServerCertificate=true
    username: sa
    password: your_password
```

### 3️⃣ Configure Google OAuth2
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

Set environment variables:
```bash
export GOOGLE_CLIENT_ID=your_client_id
export GOOGLE_CLIENT_SECRET=your_client_secret
```

Or update `application.yml`:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
```

### 4️⃣ Build & Run
```bash
# Backend
./gradlew bootRun
# Server starts at http://localhost:8080

# Frontend (in another terminal)
cd frontend
npm install
npm run dev
# Dev server at http://localhost:5173
```

### 5️⃣ Access Application
- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **H2 Console (if enabled):** http://localhost:8080/h2-console

---

## 📁 Project Structure

```
TheXuong/
├── src/
│   ├── main/
│   │   ├── java/com/example/thexuong/
│   │   │   ├── config/          # Security, CORS config
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── security/        # OAuth2 & JWT
│   │   │   ├── service/         # Business logic
│   │   │   └── TheXuongApplication.java
│   │   └── resources/
│   │       ├── application.yml  # Configuration
│   │       └── dbTheXuong.sql   # Database schema
│   └── test/                    # Unit & integration tests
├── frontend/
│   ├── src/
│   │   ├── components/          # Vue components
│   │   ├── router/              # Vue Router config
│   │   ├── stores/              # Pinia stores
│   │   ├── types/               # TypeScript definitions
│   │   ├── views/               # Page components
│   │   └── services/api.ts      # API client
│   ├── index.html
│   ├── vite.config.ts
│   └── tsconfig.json
├── dbTheXuong.sql               # Full database schema
├── gradlew                      # Gradle wrapper (Unix)
├── gradlew.bat                  # Gradle wrapper (Windows)
├── build.gradle.kts             # Build configuration
├── settings.gradle.kts
└── README.md
```

---

## 🔌 API Documentation

### Authentication Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/auth/google` | Initiate Google OAuth2 login |
| GET | `/login/oauth2/code/google` | OAuth2 callback |
| POST | `/auth/login` | JWT login (email/password) |
| POST | `/auth/logout` | Invalidate session |
| GET | `/auth/profile` | Get current user profile |
| PUT | `/auth/profile` | Update profile |

### Product Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | List all active products (paginated) |
| GET | `/api/v1/products/{id}` | Get product details |
| GET | `/api/v1/products/search` | Search products |
| GET | `/api/v1/categories` | List all categories |

### Order Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cart` | Get shopping cart |
| POST | `/api/v1/cart/add` | Add item to cart |
| PUT | `/api/v1/cart/update` | Update cart item |
| DELETE | `/api/v1/cart/remove/{id}` | Remove cart item |
| POST | `/api/v1/orders` | Place order |
| GET | `/api/v1/orders` | Get user orders |

### Admin Endpoints (ADMIN role required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/products` | List all products (including inactive) |
| POST | `/api/v1/admin/products` | Create product |
| PUT | `/api/v1/admin/products/{id}` | Update product |
| DELETE | `/api/v1/admin/products/{id}` | Soft delete product |
| GET | `/api/v1/admin/orders` | All orders |
| GET | `/api/v1/admin/loyalty/report` | Loyalty statistics |
| POST | `/api/v1/admin/loyalty/vouchers` | Create voucher |

---

## ⚙️ Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
thexuong:
  jwt:
    secret: ${JWT_SECRET:your-secret-key-here}
    expiration: 86400000  # 24 hours in ms

  loyalty:
    points-per-100k: 1
    vip-threshold: 5000000  # 5 million VND
    vip-period-days: 365

  cron:
    point-expire: "0 0 * * *"      # Daily at midnight
    voucher-expire: "0 30 * * *"   # Daily at 00:30
    tier-reevaluate: "0 0 1 * *"   # Monthly on day 1
```

### Environment Variables
| Variable | Description | Required |
|----------|-------------|----------|
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | Yes |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | Yes |
| `JWT_SECRET` | JWT signing secret | Recommended |
| `DB_URL` | SQL Server connection URL | Yes |
| `DB_USERNAME` | Database username | Yes |
| `DB_PASSWORD` | Database password | Yes |

---

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

---

## 📈 Database Schema

The project uses **SQL Server** with the following key tables:

- `Users` - User accounts with OAuth2 support
- `Products` - Product catalog with soft delete
- `ProductVariants` - Size-based inventory
- `Reviews` - Customer reviews
- `Orders` - Order records with status tracking
- `OrderDetails` - Order line items
- `Cart` - Shopping cart
- `LoyaltyPoints` - Points history
- `Vouchers` - Voucher catalog & user assignments

See [`dbTheXuong.sql`](dbTheXuong.sql) for complete schema.

---

## 🔄 Scheduled Tasks (Cron Jobs)

| Job | Schedule | Description |
|-----|----------|-------------|
| `PointExpireJob` | Daily 00:00 | Expire points older than 12 months |
| `VoucherExpireJob` | Daily 00:30 | Mark expired vouchers as USED |
| `VoucherExpiringSoonJob` | Daily 09:00 | Send email alerts for 3-day expiry |
| `TierReevaluateJob` | 1st of month | Recalculate VIP status |

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request** with detailed description

### Code Style
- **Backend:** Follow Java Spring Boot conventions, use meaningful variable names
- **Frontend:** Use TypeScript strict mode, follow Vue 3 Composition API patterns
- **Commits:** Use conventional commits format: `feat:`, `fix:`, `docs:`, etc.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Vue.js](https://vuejs.org/) - Frontend framework
- [Tailwind CSS](https://tailwindcss.com/) - Styling
- [SQL Server](https://www.microsoft.com/sql-server) - Database

---

## 📞 Contact

For support or inquiries, please open an issue on GitHub.

**Project Link:** https://github.com/yourusername/TheXuong

---

<div align="center">
Made with ❤️ by TheXuong Team
</div>