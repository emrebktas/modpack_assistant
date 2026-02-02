<p align="center">
  <img src="chatbotmc-frontend/public/bettermc.jpg" alt="BetterMC" width="300" />
</p>
<p align="center"><strong>BetterMC</strong></p>

A full-stack AI chatbot application for Minecraft modpack (BetterMC) support. Users can ask questions and get answers powered by Google Gemini, with RAG over mod documentation. Live at https://modpack-assistant.vercel.app/ 

## Overview

**Backend:** Spring Boot 4 (Java 21), Gemini LLM, RAG over JSON data, PostgreSQL, JWT auth, SendGrid email
**Frontend:** React 19, TypeScript, Material-UI, Vite

## Features

- Chat interface with Gemini-powered responses
- RAG (Retrieval-Augmented Generation) over mod/addon documentation
- User registration with admin approval workflow
- Email notifications (approval requests, approved/rejected)
- Conversation history and sidebar
- JWT authentication and role-based access

## Prerequisites

- **Java 21**
- **Node.js 18+**
- **PostgreSQL**
- **Environment:** Gemini API key, JWT secret, SendGrid (or SMTP), admin email

## Quick Start

### 1. Backend (`chatbotmc`)

```bash
cd chatbotmc
```

Set required environment variables (or use `application-local.properties` for dev):

- `GEMINI_API_KEY` – Google Gemini API key
- `JWT_SECRET` – e.g. `openssl rand -base64 64`
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` – PostgreSQL
- `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL`, `ADMIN_EMAIL` – for approval emails

```bash
./mvnw spring-boot:run
```

Backend runs at `http://localhost:8080`.

### 2. Frontend (`chatbotmc-frontend`)

```bash
cd chatbotmc-frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`. Ensure the backend URL in the app (or env) points to `http://localhost:8080` (or your backend URL).

## Project Structure

```
springbut/
├── chatbotmc/                 # Spring Boot backend
│   ├── src/main/java/...      # Controllers, services, LLM, RAG, auth, email
│   └── src/main/resources/    # application.properties, data.json (RAG source)
├── chatbotmc-frontend/        # React + Vite frontend
│   ├── public/                # Static assets (e.g. bettermc.jpg)
│   └── src/                   # App, components, services
└── README.md
```

## Configuration Highlights

| Purpose        | Config / Env |
|----------------|--------------|
| LLM            | `GEMINI_API_KEY` |
| Database       | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` |
| Auth           | `JWT_SECRET`, `jwt.expiration` |
| Email          | `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL`, `ADMIN_EMAIL` |
| Backend URL    | `APP_BACKEND_URL` (for links in emails) |
| RAG            | `rag.enabled`, `rag.top-k`, `rag.similarity-threshold` |

## License

MIT
