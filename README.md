# NorthBay RAG Chat Service API

## 🏗️ Architecture Overview

```text
                               ┌────────────────────────────┐
                               │        Docker Engine       │
                               │   (Orchestrates services)  │
                               └────────────┬───────────────┘
                                            │
┌─────────────────────────────────────────────────────────────────────────────┐
│                             Application Layer                               │
│                                                                             │
│  +----------------------+       +----------------------+                     │
│  |  Spring Boot App     | <---->| PostgreSQL Database  |                     │
│  | (rag-chat-service)   | JPA   | (chat data storage)  |                     │
│  |----------------------|-------|----------------------|                     │
│  | - REST API (8081)    |       | - Port 5432          |                     │
│  | - Spring Data JPA    |       | - Flyway migrations  |                     │
│  | - Rate Limiting      |       +----------------------+                     │
│  | - API Key Security   |                                                │
│  | - Logging (SLF4J)    |                                                │
│  +---------┬------------+                                                │
│            │ Structured JSON Logs                                        │
└────────────┼─────────────────────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────┐
│        Promtail           │
│  (Collects container logs)│
│  - Reads Docker logs      │
│  - Labels: service, job   │
└────────────┬──────────────┘
             │ HTTP Push
             ▼
┌───────────────────────────┐
│          Loki             │
│ (Centralized Log Storage) │
│  - Exposes /loki/api/...  │
│  - Port 3100              │
└────────────┬──────────────┘
             │
             ▼
┌───────────────────────────┐
│          Grafana          │
│  (Visualization UI)       │
│  - Queries Loki via LogQL │
│  - Port 3000              │
└───────────────────────────┘
```

---

## ⚙️ Setup & Running Instructions

### Prerequisites
- Docker Desktop (WSL2 or Linux backend)
- Java 17+
- Maven 3.9+

### Run the Full Stack
```bash
docker-compose up --build
```
This will start:
- PostgreSQL (port `5432`)
- pgAdmin (port `8080`)
- App service (port `8081`)
- Loki (port `3100`)
- Promtail (collecting logs)
- Grafana (port `3000`)

Access URLs:
- **App:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Grafana:** http://localhost:3000 (user: `admin`, pass: `admin`)
- **pgAdmin:** http://localhost:8080 (login: `admin@local.com`)

### Environment Configuration
`application.yml`
```yaml
server:
  port: 8081

rate-limit:
  capacity: 5
  refill-tokens: 100
  refill-period-seconds: 60
```

API Key Configuration (in `.env`):
```bash
API_KEYS=key1,key2,key3
```

---

## 📡 API Documentation (Swagger)

You can access live API docs at:
👉 [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

### Key Endpoints
| Method | Endpoint | Description |
|--------|-----------|-------------|
| `GET` | `/health` | Health check of the service |
| `POST` | `/api/sessions` | Create a new chat session |
| `GET` | `/api/sessions` | List all chat sessions |
| `PUT` | `/api/sessions/{id}` | Rename or mark a session favorite |
| `DELETE` | `/api/sessions/{id}` | Delete a chat session |
| `GET` | `/api/sessions/favorites` | List favorite sessions |
| `POST` | `/api/sessions/{id}/messages` | Add message to session |
| `GET` | `/api/sessions/{id}/messages` | Retrieve messages in a session |

All endpoints require a valid API key header:
```
X-API-Key: key1
```

---

## 🧠 Features
- Create, list, rename, favorite, and delete chat sessions.
- Add and fetch messages within sessions.
- Real-time structured logging (via Promtail → Loki → Grafana).
- Rate limiting to prevent API abuse.
- API key authentication for secure access.

---

## 🔐 Security
- **API Key Filter:** Ensures requests contain a valid `X-API-Key` header.
- **RateLimitFilter:** Uses Bucket4j to restrict excessive API calls.
- **CORS:** Configured to allow local development origins.

---

## 🧪 Testing

### Run All Tests
```bash
mvn clean test
```

### Example Tests Included
- `ChatServiceImplTest` — Unit tests for business logic.
- `ChatSessionsControllerTest` — Web layer (MockMVC) tests.
- `ChatMessagesControllerTest` — End-to-end message APIs.

You can adjust rate limit tests by editing:
```yaml
rate-limit:
  capacity: 2
  refill-tokens: 5
  refill-period-seconds: 30
```
Then send multiple requests quickly to verify 429 responses.

---

## 📊 Monitoring in Grafana

### Steps
1. Visit http://localhost:3000 → **Connections → Data Sources → Loki → Connect**
   - URL: `http://loki:3100`
2. Import dashboard → Upload provided JSON (or paste content).
3. Run queries like:
   ```logql
   {project="rag-chat-service", service="app"} | json | line_format "{{.method}} {{.path}} {{.status}}"
   ```
4. To see API duration trends:
   ```logql
   {project="rag-chat-service", service="app"} | json | unwrap duration_ms | avg_over_time(duration_ms[1m])
   ```

---

## 📘 Notes
- Swagger spec: `src/main/resources/openapi/northbay-chat-service-v1.yaml`
- Logs shipped via Promtail → Loki, viewable in Grafana.
- Database migrations handled by Flyway.

---

**© 2025 NorthBay Digital | RAG Chat Service API v1**
