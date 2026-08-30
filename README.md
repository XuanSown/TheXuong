<div align="center">

# TheXuong

Full-stack e-commerce platform for sports equipment, built with Spring Boot and Vue 3.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)](https://openjdk.org/)
[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.3-3178C6?logo=typescript)](https://www.typescriptlang.org/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-CC2927?logo=microsoftsqlserver)](https://www.microsoft.com/en-us/sql-server)

</div>

---

## Features

- Google OAuth2 & JWT authentication with role-based access (Admin / Customer)
- Size-based product variant inventory with soft-delete support
- Loyalty points, VIP tiers, and personalized voucher system
- Admin dashboard with sales analytics, order management, and user management
- Customer reviews, ratings, and chatbot support

## Tech Stack

| Layer | Stack |
|-------|-------|
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, Gradle |
| **Frontend** | Vue 3, TypeScript, Pinia, Vue Router, Tailwind CSS, Vite |
| **Database** | SQL Server 2019 |
| **Chatbot** | Telegram Bot, N8N, Ollama (AI Local) |

## Getting Started

### Prerequisites

- JDK 21+
- SQL Server 2019+
- Node.js 18+

### Setup

```bash
git clone https://github.com/XuanSown/TheXuong.git
cd TheXuong
```

**Database** — import the schema:

```bash
sqlcmd -S localhost -U SA -P your_password -i dbTheXuong.sql
```

Update `src/main/resources/application.properties` with your SQL Server credentials.

**Backend:**

```bash
./gradlew bootRun
# runs at http://localhost:8080
```

**Frontend:**

```bash
cd frontend
npm install
npm run dev
# runs at http://localhost:5173
```

## Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit (`git commit -m 'feat: add your feature'`)
4. Push and open a PR

## License

MIT
