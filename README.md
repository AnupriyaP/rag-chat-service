
# 🧠 NorthBay RAG Chat Service API

---

## 📐 Architecture Overview

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
│  | - Groq LLM Service   |  ←──── Generates AI responses                   │
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

## ⚙️ Setup and Running Instructions

### **Prerequisites**
- Docker Desktop (WSL2 or Linux backend)
- Java 17+
- Maven 3.9+

### **Run the Full Stack**
```bash
docker-compose up --build
```

This starts:
- PostgreSQL (port `5432`)
- pgAdmin (port `8080`)
- RAG Chat Service (port `8081`)
- Loki (port `3100`)
- Promtail (collecting logs)
- Grafana (port `3000`)

Access URLs:

| Service | URL |
|----------|-----|
| App | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Grafana | http://localhost:3000 (user: admin / pass: admin) |
| pgAdmin | http://localhost:8080 (login: admin@local.com) |

---

## 🌍 Environment Configuration

### `application.yml`
```yaml
server:
  port: 8081

rate-limit:
  capacity: 5
  refill-tokens: 100
  refill-period-seconds: 60

groq:
  api:
    url: https://api.groq.com/openai/v1/chat/completions
    key: ${GROQ_API_KEY}
  model: ${GROQ_MODEL:llama3-70b-8192}
  system-prompt: ${GROQ_SYSTEM_PROMPT:You are a helpful and concise AI assistant.}
```

### `.env`
```bash
API_KEYS=key1,key2,key3
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/rag_chat
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Groq LLM Configuration
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama3-70b-8192
GROQ_SYSTEM_PROMPT=You are a helpful and concise AI assistant.
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
[http://localhost:8081/swagger-ui.html]

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

Workflow:

User sends a message to the chat session.

Backend saves the user message.

GroqLLMServiceImpl calls Groq’s API to generate the assistant’s reply.

Assistant message is saved in the same session and returned in the response.

All endpoints require a valid API key header:
```
X-API-Key: demo-key
```
---

## 🧠 Groq LLM Integration

### Overview
The `GroqLLMServiceImpl` integrates with **Groq’s OpenAI-compatible `/chat/completions` endpoint** to automatically generate **assistant replies** when users post messages.

### Features
- Configurable **model** and **system prompt**
- Retry with exponential backoff
- Timeout and error handling with custom exceptions
- Structured logging of latency and response metadata

### Key Class
`com.northbay.ragchat.service.impl.GroqLLMServiceImpl`

Implements `LLMService` with:
- `generateResponse(String userPrompt)`
- Configurable prompt context from `application.yml`
- Uses `WebClient` with proper headers and retries
- Exception safety via `ApiException` and `GlobalExceptionHandler`

---

## 🧩  Endpoint: POST `/api/sessions/{id}/messages`

### Description
Adds a user message to an existing chat session and triggers an AI assistant reply generated by the Groq model.

### Workflow
1. User sends a message to the session.
2. Backend stores the user message.
3. `ChatServiceImpl` calls `GroqLLMServiceImpl.generateResponse()`.
4. Assistant response is returned and saved in the same session.
---
### Request Example
```json
## 🧩  Endpoint: POST `/api/sessions/1/messages`
        
{
  "sender": "user",
  "content": "Will it rain today in Bonn, Germany?"
}
```
---
### Response Example
```json
{
  "sessionId": 1,
  "messages": [
    {
      "sender": "user",
      "content": "Will it rain today in Bonn, Germany?"
    },
    {
      "sender": "assistant",
      "content": "Light rain expected in Bonn today, around 13°C."
    }
  ]
}
```
---

## Pagination Support

 endpoints ( `/api/sessions/{id}/messages`) support pagination.

| Parameter | Type | Default | Description |
|------------|------|----------|-------------|
| page | integer | 0 | Zero-based page index |
| size | integer | 10 | Number of records per page |


---

## 🧾 Error Handling

### Common Cases
| Scenario | Status | Example |
|-----------|---------|----------|
| Validation failure | 400 | `{"message":"Validation failed: content must not be blank"}` |
| Invalid API key | 401 | `{"message":"Unauthorized: invalid API key"}` |
| Groq API error | 500 | `{"message":"Groq API error: timeout or empty response"}` |

Handled via `GlobalExceptionHandler`, returning a consistent `ErrorResponse`.

---

## Security

### API Key Filter
Validates the `X-API-Key` header. Configured via SecurityConfig using ApiKeyFilter.

### Rate Limiting
Implemented using Bucket4j in RateLimitFilter. Limits API calls per key per time window.

### Request Tracking
RequestIdFilter adds a unique request ID (from header or auto-generated UUID) for each request.

---

---

## 📊 Logging & Monitoring

### What’s Tracked
- Request/Response logs via `RequestResponseLoggingFilter`
- Request IDs (`RequestIdFilter`)
- LLM call duration & status logs in `GroqLLMServiceImpl`
- Application metrics exposed via `/actuator/*`

### Grafana Query Example
```logql
{service="rag-chat-service"} | json | line_format "{{.method}} {{.path}} {{.status}}"
```

---

## 🧪 Testing

```bash
mvn clean test
```

Includes:
- Service layer tests for `ChatServiceImpl`
- Exception and validation test cases

---

## 🚀 Features Summary

- ✅ Chat session CRUD
- ✅ Add and retrieve messages
- ✅ Auto-generated assistant replies using Groq LLM
- ✅ Configurable system prompts and model
- ✅ Structured error handling
- ✅ API key and rate limiting
- ✅ Centralized JSON logging with Loki/Grafana
- ✅ Health & metrics endpoints via Actuator

---

## 🔮 Future Enhancements

1. **Multi-LLM Provider Support**
    - Support for Groq, OpenAI, or Anthropic via config abstraction.
2. **WebSocket Real-Time Chat**
    - Push live assistant updates to users.
3. **Retrieval-Augmented Generation (RAG)**
    - Integrate vector-based context retrieval from chat history or external documents.
4. **JWT Authentication**
    - Replace API key with token-based security.
5. **Enhanced Observability**
    - Add Prometheus metrics for LLM latency and error rates.

---

## 📚 Notes
- OpenAPI Spec: `src/main/resources/openapi/northbay-chat-service-v1.yaml`
- Database migrations via Flyway.
- Logs shipped from Docker → Promtail → Loki → Grafana.

---

© 2025 **NorthBay Digital** | RAG Chat Service API v1.1 (Groq LLM Integration)
