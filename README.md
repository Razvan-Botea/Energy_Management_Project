## Distributed Energy Management System

A robust, microservices-based platform designed to manage smart energy metering devices, monitor power consumption in real-time, and provide intelligent customer support.
The system features a containerized architecture using Docker Swarm, orchestrated via Traefik, and utilizes RabbitMQ for asynchronous communication and load balancing.

## Architecture Overview

The system is composed of loosely coupled microservices communicating via REST APIs and RabbitMQ.
Core Components
    Frontend (React): A responsive web interface for Users and Administrators.
    API Gateway (Traefik): Handles routing, load balancing, and reverse proxying.

Microservices Layer:
    User & Device Services: Manage entities and their relationships (CRUD).

Auth Service: Secure JWT-based authentication.
Monitoring Service: Processes energy data, detects overconsumption, and stores historical data.
Chat Service: Provides support via Rule-based logic and AI (Groq/Llama) integration.
WebSocket Service: Pushes real-time alerts and chat messages to the frontend.
Load Balancing Service: Distributes high-volume sensor data across multiple monitoring replicas.
Infrastructure:
    RabbitMQ: Message broker for sensor data, synchronization, and chat events.

PostgreSQL: Dedicated databases for each microservice.
Device Simulator: Python script generating realistic energy data patterns.

## Key Features

1. User & Device Management (Assignment 1)
    Role-Based Access Control: Administrators can manage users and devices; Clients can view their assigned devices.

Secure Authentication: Login/Register flows using JWT tokens.
Device Mapping: Admins can map specific metering devices to user accounts.

2. Monitoring & Visualization (Assignment 2)
    Historical Charts: Users can view energy consumption trends (hourly) on a selected date.

Asynchronous Ingestion: Sensor data is ingested via RabbitMQ to decouple producers from consumers.
Data Synchronization: Users and Devices are synced across microservices using RabbitMQ events.

3. Real-Time & Scalability (Assignment 3)
    Load Balancing: A dedicated service routes incoming sensor data to specific Monitoring Service replicas (Round-Robin strategy) to handle high traffic.

Overconsumption Alerts: If a device exceeds its max_consumption, a red alert is pushed instantly to the user's dashboard via WebSockets.
Intelligent Chat Support:
    Rule-Based: Instant answers for common queries (e.g., "price", "outage").

AI-Driven: Integrated with Groq API (Llama-3) to answer complex questions when rules don't match.
Real-Time Typing: Bidirectional communication via WebSockets.

## Technology Stack

    Backend: Java Spring Boot (REST, AMPQ, WebSocket)
    Frontend: React.js, Tailwind CSS
    Messaging: RabbitMQ
    Database: PostgreSQL
    AI: Groq API (Llama-3 model)
    DevOps: Docker, Docker Compose, Traefik

⚙️ Setup & Installation
Prerequisites
    Docker & Docker Desktop installed.
    Python 3 (for the simulator).
    A CloudAMQP account (or local RabbitMQ).
    Groq API Key (for AI chat).

1. Clone the Repository

`git clone https://gitlab.com/your-username/DS2025_Group_Name_Assignment_3.git
cd DS2025_Group_Name_Assignment_3`

2. Configure Environment

Ensure the .env file or docker-compose.yml environment variables are set, specifically:
    SPRING_RABBITMQ_ADDRESSES: Your AMQP URL.
    GEMINI_API_KEY (in customer-support-microservice): Your Groq/AI API key.

3. Build and Run (Docker Swarm/Compose)
Since the project uses a microservices architecture, build the images and start the stack.

# Build the services

`docker compose build`

# Start the application

`docker compose up -d`

Wait approx. 60 seconds for all databases and services (including the 3 monitoring replicas) to initialize.

How to Test
1. Access the Application
    Open your browser at http://localhost:80 (Traefik Gateway).
    Login: Use an Admin account (to create devices) or a Client account (to view dashboard).

2. Test Real-Time Alerts & Load Balancing
    Open the Browser Console (F12) to see WebSocket connections.
    Open Two Terminals to monitor backend logs:
        Terminal A: docker logs -f load-balancing-service
        Terminal B: docker compose logs -f monitoring-replica-1 monitoring-replica-2 monitoring-replica-3 | grep "Received Message"
    Run the Device Simulator:
    `python3 simulator.py`

    Observe:
        Terminal A: Shows the Load Balancer routing messages (Replica 1 -> 2 -> 3).
        Terminal B: Shows different replicas processing data.
        Frontend: If the value exceeds the limit, a Red Alert notification appears instantly.

3. Test AI Chat
    Go to the Chat page in the frontend.
    Type "Hello" -> Response should be immediate (Rule-based).
    Type "How can I save energy?" -> Response comes from Llama-3 AI.

## Project Structure
Plaintext

├── auth-service/                # Authentication & JWT

├── user-management-service/     # User CRUD

├── device-management-service/   # Device CRUD

├── monitoring-service/          # Data processing & Storage (Replicated)

├── load-balancing-service/      # Routes sensor data to replicas

├── websocket-service/           # Handles real-time push notifications

├── customer-support-service/    # Chat logic (Rules + AI)

├── frontend/                    # React Application

├── simulator/                   # Python Data Generator

├── traefik-config/              # API Gateway Configuration

└── docker-compose.yml           # Deployment Descriptor

## Other useful commands 

- pornire containere docker:
docker compose up -d
docker compose up -d --build

- pentru a vedea log-urile unui container:
docker logs -f monitoring-service

- pentru conectarea la un container:
docker exec -it monitoring-db psql -U postgres -d monitoring_db

- pentru navigarea in baza de date postgres:
"\l" - afisarea bazelor de date
"\c  <database_name>" - conectarea la o baza de date
"dt" - afisarea tabelelor din baza de date
"SELECT * FROM <table_name>" - afisarea datelor din tabel

- POST request pentru crearea primului utilizator:
creare utilizator nou: curl -X POST http://localhost:8000/api/users
-H "Content-Type: application/json"
-d '{ "name": "Test User", "username": "testuser", "password": "password123", "role": "CLIENT" }'

- pentru interfata grafica:
npm start
http://localhost:8000/login

- dashboard traefik:
https://localhost:8080/dashboard/#/

- dashboard CloudAMPQ:
https://customer.cloudamqp.com/instance

- pentru crearea datelor cu script-ul de python:
"CLOUDAMQP_URL=" - se adauga link-ul generat de CloudAMPQ
"DEVICE_ID=" - id-ul device-ului pentru care se genereaza datele, se ia din device-db
"TARGET_DATE_STR = "2025-11-25"" - se adauga data pentru care se doreste generarea datelor

- testare load balancing:
Terminal 1 (Load Balancer):
docker logs -f load-balancing-service
Terminal 2 (Replica 1):
docker logs -f monitoring-replica-1
Terminal 3 (Replica 2):
docker logs -f monitoring-replica-2
Terminal 4 (Simulator): Run your script.
