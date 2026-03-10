# AI Chatbot Backend (Spring Boot)

Backend service for the AI Chatbot application built with **Java Spring Boot**.  
It provides authentication, real-time chat using **WebSockets**, AI integration, and message persistence.

---

## Table of Contents

- Overview
- Architecture
- Tech Stack
- Features
- Project Structure
- API Endpoints
- WebSocket
- Setup
- Environment Variables
- Future Improvements

---

## Overview

This backend powers a real-time AI chat system.

It provides:

- Secure authentication using **JWT**
- Real-time messaging via **WebSocket + STOMP**
- AI-generated responses via **LLM API**
- Persistent chat history using **PostgreSQL/MySQL**
- REST APIs for user management and conversation history

---

## Architecture


React Client
|
REST + WebSocket
|
Spring Boot Backend
├ Authentication (JWT)
├ WebSocket Messaging (STOMP)
├ AI Service (LLM API Integration)
├ Business Logic
└ Persistence Layer (JPA/Hibernate)
|
Database (PostgreSQL / MySQL)


### Message Flow

1. User sends message from React client
2. Message sent via **WebSocket**
3. Backend:
    - Saves message to DB
    - Broadcasts to subscribers
    - Calls AI service asynchronously
4. AI response returned and broadcast
5. Response stored in database

---

## Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Security + JWT
- Spring WebSocket + STOMP
- Spring Data JPA
- PostgreSQL / MySQL
- Lombok
- Maven / Gradle
- Docker (optional)

---

## Features

- User registration and login
- JWT authentication
- Real-time chat
- AI assistant responses
- Message persistence
- Conversation history API

---

## Project Structure


src/main/java/

config/
SecurityConfig
WebSocketConfig

controller/
AuthController
ChatController
UserController

service/
AuthService
AiService
MessageService

repository/
UserRepository
MessageRepository
ConversationRepository

model/
User
Message
Conversation

dto/
MessageDTO
AuthRequest
AuthResponse


---

## API Endpoints

### Authentication


POST /api/auth/register
POST /api/auth/login


### User


GET /api/users/me


### Conversations


GET /api/conversations/{id}/messages


---

## WebSocket

Endpoint


/ws-chat


Send messages


/app/chat


Subscribe to conversation


/topic/conversations/{conversationId}


---

## Setup

Clone repository

https://github.com/AmanBhatt0910/ai-chatbot-backend

Run backend


mvn spring-boot:run


---

## Environment Variables


OPENAI_API_KEY=your_api_key
JWT_SECRET=your_secret
DB_URL=jdbc:postgresql://localhost:5432/aichatbot
DB_USERNAME=postgres
DB_PASSWORD=password


---

## Future Improvements

- Redis or RabbitMQ for scalable messaging
- AI rate limiting
- Vector database for knowledge-based responses
- Monitoring and logging

---