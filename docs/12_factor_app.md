## 12-Factor for `ticketmaster` Microservices

This documentation explains how the **12-Factor App** methodology applies to your Ticketmaster-style microservices system (`api-gateway`, `event-service`, `booking-service`, `payment-service`, `events-list-service`, and `events-ui`).

---

### 1) Codebase — One codebase tracked in version control, many deploys
In `ticketmaster`, all services live in one repository (monorepo), but each deployable has its own module and artifact.

How to apply:
- Keep each service in its own folder (`event-service/`, `booking-service/`, etc.).
- Use branch + PR workflows from one Git repo.
- Enable independent deployments per service/environment (dev/stage/prod).

Microservice note:
- Monorepo is fine if build/deploy boundaries are clear per service.

---

### 2) Dependencies — Explicitly declare and isolate dependencies
Each service should declare dependencies in its own manifest:
- Java services: `pom.xml`
- UI: `events-ui/package.json`

How to apply:
- Avoid relying on globally installed tools/libraries.
- Pin versions (or managed BOM) to keep reproducibility.
- Build images from clean environments (Dockerfiles already present).

---

### 3) Config — Store config in the environment
Any value that changes by environment should be env-based, not hardcoded:
- DB URLs/credentials
- Kafka brokers/topics
- JWT/Keycloak config
- OpenSearch/Redis endpoints
- Feature flags

How to apply:
- Keep defaults minimal in app config; override with env vars.
- Use Kubernetes `ConfigMap` + `Secret` (`k8s/*.yaml`) for runtime config.
- Never commit secrets to Git.

Example envs:
- `SPRING_DATASOURCE_URL`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `KEYCLOAK_ISSUER_URI`

---

### 4) Backing Services — Treat backing services as attached resources
Databases, Kafka, Redis, Keycloak, OpenSearch are external resources.

How to apply:
- Access all via URL/credentials from env.
- Swappable per environment (local docker-compose vs cloud managed services) without code changes.
- Keep service logic independent from specific local setups.

---

### 5) Build, Release, Run — Strictly separate stages
For each Java service:
- **Build**: compile/package jar
- **Release**: combine artifact + config version + image tag
- **Run**: execute container/pod with injected env

How to apply:
- CI builds immutable artifacts/images.
- CD promotes tagged releases.
- Avoid rebuilding in production environments.

---

### 6) Processes — Execute app as one or more stateless processes
Service instances should be stateless and horizontally scalable.

How to apply:
- Keep session/user state out of process memory.
- Use DB/Redis/Kafka for persisted/shared state.
- Any pod/container can handle any request.

Note:
- Booking/payment workflows can remain consistent using DB transactions + event-driven patterns (saga-style approach in `booking-service/docs/saga.md`).

---

### 7) Port Binding — Export services via port binding
Each service should self-host and expose its own port.
- Spring Boot services bind to ports in containers.
- `api-gateway` fronts internal services.
- `events-ui` served by its own container (Nginx/Vite build output).

How to apply:
- Configure `server.port` via env where useful.
- Rely on K8s services/ingress/Istio (`k8s/*.yaml`) for routing.

---

### 8) Concurrency — Scale out via process model
Scale using multiple instances of specific services:
- `events-list-service` may need read-heavy scaling.
- `booking-service`/`payment-service` may scale by throughput profile.

How to apply:
- Define CPU/memory requests and autoscaling rules in K8s.
- Scale independent services, not the whole platform.
- Use queue/topic partitioning strategy for event consumers.

---

### 9) Disposability — Fast startup and graceful shutdown
Containers should start quickly and shut down cleanly.

How to apply:
- Add health/readiness/liveness checks.
- Handle SIGTERM gracefully (finish in-flight work when possible).
- Keep startup deterministic (no manual initialization steps).

For event consumers:
- Commit offsets safely and stop consumption cleanly on shutdown.

---

### 10) Dev/Prod Parity — Keep environments as similar as possible
Avoid “works on my machine” drift.

How to apply:
- Use Docker locally, same runtime style as prod.
- Use `docker-compose/` for local dependencies (Kafka, Redis, Postgres, Keycloak, OpenSearch).
- Keep schema migrations and startup behavior consistent.
- Keep time gap small between commit and deployment via CI/CD.

---

### 11) Logs — Treat logs as event streams
Apps should write structured logs to stdout/stderr; platform handles storage/routing.

How to apply:
- Do not write service logs to local files in containers.
- Emit JSON/structured logs with correlation IDs (e.g., request ID, booking ID).
- Aggregate via centralized tooling (e.g., OpenSearch stack).

---

### 12) Admin Processes — Run admin/ops tasks as one-off processes
Operational tasks (data fixes, replays, migrations) should run as disposable one-offs using same codebase/config.

How to apply:
- Run DB migration jobs or maintenance commands as K8s Jobs/CronJobs.
- Keep same environment variables and image as normal app runtime.
- Record/review admin job output like standard logs.

---

## Ticketmaster-Specific Implementation Checklist

- Externalize all service configs in env/Secrets/ConfigMaps (no hardcoded endpoints/secrets).
- Ensure every service is stateless; move transient/shared state to backing services.
- Standardize health checks and graceful shutdown across all Spring services.
- Emit structured logs with trace/correlation IDs across gateway and downstream services.
- Use immutable versioned Docker images and promote by tag.
- Scale services independently based on traffic pattern (read-heavy vs transaction-heavy).
- Keep local `docker-compose` dependency versions close to production equivalents.
- Run migrations/admin tasks as one-off jobs, not ad-hoc shell commands in running pods.

---

