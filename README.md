# ⚡ GridWeaver - Virtual Thread IoT Microgrid State Engine

GridWeaver is a Java-based IoT Microgrid State Engine developed using Spring Boot and Java 21 Virtual Threads. It simulates IoT devices, monitors power generation, and provides REST APIs for real-time microgrid monitoring. The project also includes a React frontend dashboard for displaying device information.

---

## 🚀 Features

- Java 21 Virtual Threads
- Spring Boot REST API
- IoT Device Simulation
- Real-Time Power Monitoring
- Concurrent Device Processing
- React Frontend Dashboard
- Maven Build System
- Scalable Microgrid Architecture

---

## 🛠 Technologies Used

### Backend
- Java 21
- Spring Boot
- Maven
- REST API
- Virtual Threads (Project Loom)

### Frontend
- React
- Vite
- HTML
- CSS
- JavaScript

---

## 📁 Project Structure

```
GridWeaver/
│
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/gridweaver/
│           │       ├── GridWeaverApplication.java
│           │       ├── controller/
│           │       ├── model/
│           │       └── service/
│           └── resources/
│               └── application.properties
│
└── frontend/
    ├── public/
    ├── src/
    ├── package.json
    └── vite.config.js
```

---

## ⚙️ Prerequisites

- Java 21
- Maven
- Node.js
- VS Code

---

## ▶️ Run the Backend

```bash
cd backend
mvn spring-boot:run
```

Backend URL

```
http://localhost:8080
```

API Endpoint

```
http://localhost:8080/devices
```

---

## ▶️ Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL

```
http://localhost:5173
```

---

## 📌 Sample API Response

```json
[
  {
    "id": "DEV-1",
    "type": "Solar Panel",
    "status": "ACTIVE",
    "power": 52.4
  },
  {
    "id": "DEV-2",
    "type": "Battery",
    "status": "ACTIVE",
    "power": 67.8
  }
]
```

---

## 🔄 Workflow

1. Start the Spring Boot backend.
2. Simulate IoT devices.
3. Monitor device power using Virtual Threads.
4. Access REST API.
5. Display device information on the React dashboard.

---

## 🎯 Future Enhancements

- Apache Kafka Integration
- Spring State Machine
- WebSocket Support
- Leaflet GIS Dashboard
- Database Integration
- Authentication & Authorization
- Real-Time Alerts

---


## 📄 License

This project was developed as part of an internship for learning and demonstration purposes.
