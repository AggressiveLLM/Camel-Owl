Spring Boot (three-layer) backend for tasks demo.

Run:
- JDK 17 required.
- mvn clean spring-boot:run

API:
- GET  /api/tasks  -> list tasks
- POST /api/tasks  -> create task, body: { "title": "..." }
