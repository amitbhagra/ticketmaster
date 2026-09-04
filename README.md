# TicketMaster Microservices Application

## Project Overview

TicketMaster is a microservices-based event booking platform designed for scalability, resilience, and rapid development. The architecture leverages an API Gateway for unified entry, multiple Spring Boot microservices for business logic, Kafka for event-driven communication, PostgreSQL for persistent storage, and an Angular UI for user interaction. Each microservice is independently deployable and communicates via REST and Kafka, ensuring loose coupling and high availability.

---

## Tech Stack

- **Backend:** Spring Boot, Spring Cloud Gateway, Spring Security, Keycloak (OAuth2), Kafka, PostgreSQL, OpenSearch
- **Frontend:** Angular, Tailwind CSS
- **Infrastructure:** Docker, Docker Compose, Kubernetes (K8s)
- **DevOps:** Maven, JUnit, Actuator, Spring DevTools
- **Authentication:** Keycloak
- **API Documentation:** Swagger/OpenAPI
- **Other:** Redis (caching), Opensearch (search), Kafka Connect

---

## Folder Structure

```
ticketmaster/
│
├── api-gateway/           # API Gateway (Spring Cloud Gateway)
├── booking-service/       # Booking microservice
├── event-service/         # Event creation & management microservice
├── events-list-service/   # Event search/listing microservice
├── payment-service/       # Payment microservice
├── events-ui/             # Angular frontend (event-booking-ui)
├── docker-compose/        # Docker Compose files for local setup
│   ├── kafka/
│   ├── keycloak/
│   ├── opensearch/
│   ├── postgres/
│   └── redis/
├── docs/                  # API documentation (Postman collections, environments)
├── data/                  # Local database files
└── pom.xml                # Parent Maven configuration
```

---

## Setup Instructions

### Prerequisites

- Java 17+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose
- kubectl (for Kubernetes)
- (Optional) Minikube or local K8s cluster

### Clone the Repository

```bash
git clone https://github.com/amitbhagra/ticketmaster.git
cd ticketmaster
```

### Build All Services

```bash
mvn clean install
```

### Start All Services Locally (Docker Compose)

```bash
cd docker-compose
docker-compose -f kafka/docker-compose.yaml up -d
docker-compose -f keycloak/docker-compose.yml up -d
docker-compose -f opensearch/docker-compose.yml up -d
docker-compose -f postgres/docker-compose.yml up -d
docker-compose -f redis/docker-compose.yaml up -d

To run Kafka Connector Download JDBC Source connector from Confluent and place all the jars in kafka/connectors directory
```

Start backend services:

```bash
cd ..
mvn spring-boot:run -pl api-gateway,booking-service,event-service,events-list-service,payment-service
```

Start Angular UI:

```bash
cd events-ui/
npm install
npm run start
```


## Environment Configuration

- **Configuration Files:**  
  Each service has its own `application.yml` and `application-prod.yml` in `src/main/resources`.
- **Environment Variables:**  
  - `SPRING_PROFILES_ACTIVE` (e.g., `dev`, `prod`)
  - `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`
  - `KEYCLOAK_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`
  - `KAFKA_BOOTSTRAP_SERVERS`
  - `OPENSEARCH_HOST`, `OPENSEARCH_PORT`
- **Secrets:**  
  Use `.env` files or K8s secrets for sensitive data.

---

## Kubernetes Deployment

### API Gateway

```bash
docker build -t api-gateway:latest -f api-gateway/Dockerfile .
kind load docker-image api-gateway:latest --name desktop
kubectl apply -f k8s/api-gateway.yaml -n ticketmaster
kubectl delete -f k8s/api-gateway.yaml -n ticketmaster
kubectl -n ticketmaster rollout restart deployment/api-gateway
```

---

### Events List Service

```bash
docker build -t events-list-service:latest -f events-list-service/Dockerfile .
kind load docker-image events-list-service:latest --name desktop
kubectl apply -f k8s/events-list-service.yaml -n ticketmaster
kubectl delete -f k8s/events-list-service.yaml -n ticketmaster
kubectl -n ticketmaster rollout restart deployment/events-list-service
```

---

### Events Service

```bash
docker build -t event-service:latest -f event-service/Dockerfile .
kind load docker-image event-service:latest --name desktop
kubectl apply -f k8s/event-service.yaml -n ticketmaster
kubectl delete -f k8s/event-service.yaml -n ticketmaster
kubectl -n ticketmaster rollout restart deployment/event-service
```

---

### Booking Service

```bash
docker build -t booking-service:latest -f booking-service/Dockerfile .
kind load docker-image booking-service:latest --name desktop
kubectl apply -f k8s/booking-service.yaml -n ticketmaster
kubectl delete -f k8s/booking-service.yaml -n ticketmaster
kubectl -n ticketmaster rollout restart deployment/booking-service
```

---

### Payment Service

```bash
docker build -t payment-service:latest -f payment-service/Dockerfile .
kind load docker-image payment-service:latest --name desktop
kubectl apply -f k8s/payment-service.yaml -n ticketmaster
kubectl delete -f k8s/payment-service.yaml -n ticketmaster
kubectl -n ticketmaster rollout restart deployment/payment-service
```

---

### Events UI

```bash
docker build -t events-ui:latest -f events-ui/Dockerfile .
kind load docker-image events-ui:latest --name desktop
kubectl apply -f k8s/events-ui.yaml -n ticketmaster
kubectl delete -f k8s/events-ui.yaml -n ticketmaster
```





---

## API Documentation

- **Postman Collections:**  
  See  `docs/Ticket Master API Gateway.postman_collection.json`.

---

## UI Access

- **Local:**  
  [http://localhost:4200](http://localhost:4200) (default Angular port)
- **Kubernetes:**  
  Expose via NodePort/Ingress as per your cluster setup.

---

## Troubleshooting

- **Port Conflicts:**  
  Ensure required ports (8080, 5432, 9092, 9200, etc.) are free.
- **Database Connection Issues:**  
  Check Docker containers for Postgres and update `application.yml` accordingly.
- **Keycloak Authentication Errors:**  
  Verify Keycloak is running and credentials are correct.
- **Kafka Not Available:**  
  Ensure Kafka is up and reachable at configured bootstrap servers.
- **Swagger Not Loading:**  
  Confirm service is running and accessible at the expected port.
- **UI Not Loading:**  
  Run `npm install` and `npm start` in the UI folder.

---

## Contributing Guidelines

1. Fork the repository and create a feature branch.
2. Follow code style and naming conventions.
3. Write unit/integration tests for new features.
4. Submit a pull request with a clear description.
5. Ensure all checks pass before merging.

---

## Useful Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Angular Documentation](https://angular.io/docs)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Swagger/OpenAPI](https://swagger.io/docs/)

---

## License

This project is licensed under the MIT License.

---

For further questions or support, please open an issue or contact the maintainers.

