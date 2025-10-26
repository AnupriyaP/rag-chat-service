# NorthBay RAG Chat Service API

## Architecture Overview

```
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
│  | (rag-chat-service)   |  JPA  | (chat data storage)  |                     │
│  |----------------------|-------|----------------------|                     │
│  | - REST API (8081)    |       | - Port 5432          |                     │
│  | - Spring Data JPA    |       | - Flyway migrations  |                     │
│  | - Rate Limiting      |       +----------------------+                     │
│  | - API Key Security   |                                                │
│  | - Structured Logging |                                                │
│  +---------┬------------+                                                │
│            │ JSON Logs                                                   │
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

## Setup and Running Instructions

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
| Service | URL |
|----------|-----|
| App | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Grafana | http://localhost:3000 (user: admin, pass: admin) |
| pgAdmin | http://localhost:8080 (login: admin@local.com) |

---

## Environment Configuration

### application.yml
```yaml
server:
  port: 8081

rate-limit:
  capacity: 5
  refill-tokens: 100
  refill-period-seconds: 60
```

### .env
```bash
API_KEYS=key1,key2,key3
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/rag_chat
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
LOGGING_LEVEL_ROOT=INFO
```

---

## Database Schema

| Table | Columns | Description |
|--------|----------|-------------|
| chat_sessions | id, title, owner, favorite, created_at, updated_at | Stores chat session metadata |
| chat_messages | id, session_id, sender, content, context, created_at, updated_at | Stores messages linked to sessions |

**Relationship:**  
`chat_sessions (1)` — `chat_messages (many)`

---

## API Documentation (Swagger)

Swagger documentation is available at:  
[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

### Key Endpoints

| Method | Endpoint | Description |
|--------|-----------|-------------|
| GET | /health | Health check of the service |
| POST | /api/sessions | Create a new chat session |
| GET | /api/sessions | List all chat sessions |
| PUT | /api/sessions/{id} | Rename or mark a session as favorite |
| DELETE | /api/sessions/{id} | Delete a chat session |
| GET | /api/sessions/favorites | List favorite sessions |
| POST | /api/sessions/{id}/messages | Add a message to a session |
| GET | /api/sessions/{id}/messages | Retrieve messages in a session |

All endpoints require a valid API key header:
```
X-API-Key: demo-key
```

---

## Pagination Support

All list endpoints (`/api/sessions`, `/api/sessions/{id}/messages`) support pagination.

| Parameter | Type | Default | Description |
|------------|------|----------|-------------|
| page | integer | 0 | Zero-based page index |
| size | integer | 10 | Number of records per page |

---

## Error Handling

All API errors follow a consistent structure handled by GlobalExceptionHandler.

Example Response:
```json
{
  "timestamp": "2025-10-26T10:12:34Z",
  "status": 404,
  "error": "Not Found",
  "message": "Chat session not found",
  "path": "/api/sessions/99"
}
```

---

## Security

### API Key Filter
Validates the `X-API-Key` header. Configured via SecurityConfig using ApiKeyFilter.

### Rate Limiting
Implemented using Bucket4j in RateLimitFilter. Limits API calls per key per time window.

### Request Tracking
RequestIdFilter adds a unique request ID (from header or auto-generated UUID) for each request.

---

## Logging and Monitoring

### Request-Response Logging
RequestResponseLoggingFilter captures each request and response with execution time and body snippets.

### Loki and Grafana Integration
Promtail ships logs from Docker containers to Loki. Grafana visualizes them using LogQL queries.

Example LogQL Query:
```logql
{service="rag-chat-service"} | json | line_format "{{.method}} {{.path}} {{.status}}"
```

---

## Testing

### Running Tests
```bash
mvn clean test
```

### Coverage
- Service layer: ChatServiceImplTest
- Controller layer: MockMVC tests
- Validation and exception handling: GlobalExceptionHandler tests

To generate a coverage report:
```bash
mvn clean test jacoco:report
```
Report is available at `target/site/jacoco/index.html`.

---

## Features

- Create, list, rename, favorite, and delete chat sessions.
- Add and fetch messages within sessions.
- Built-in pagination for all list endpoints.
- Rate limiting to prevent abuse.
- API key-based authentication.
- Structured JSON logging and request tracing.
- Centralized log monitoring via Loki and Grafana.

---

## Future Enhancements

### 1. JWT Authentication
Introduce user authentication using JWT tokens.

### 2. WebSocket Real-Time Updates
Enable real-time chat updates for connected clients.

### 3. OpenAI API Integration for Assistant Responses (RAG Readiness)
Integrate OpenAI API to automatically generate assistant responses within chat sessions.

Example service structure:
```java
@Service
public class OpenAiService {
    @Value("${openai.api.key}")
    private String apiKey;

    public String generateReply(String userMessage) {
        // Example pseudo-code for OpenAI call
        return "This is a simulated AI response to: " + userMessage;
    }
}
```

The RAG pipeline would retrieve relevant context from stored messages or external sources, send it to OpenAI, and persist the generated response.

### 4. Redis Caching
Add caching for frequently accessed sessions.

### 5. Health Checks and Alerts
Add container-level health checks and Prometheus alerts.

---

## Notes

- OpenAPI spec: `src/main/resources/openapi/northbay-chat-service-v1.yaml`
- Logs: Shipped via Promtail → Loki → Grafana.
- Database migrations: Handled by Flyway.

---

© 2025 NorthBay Digital | RAG Chat Service API v1
