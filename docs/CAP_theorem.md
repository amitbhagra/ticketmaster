
## Documentation Draft: CAP Theorem, PACELC, and Their Application in Ticketmaster

### 1) CAP Theorem (Quick Overview)

CAP theorem states that in a **distributed system**, when a **network partition (P)** happens, the system can provide only one of:

- **Consistency (C)**: every read gets the latest write (or an error).
- **Availability (A)**: every request gets a non-error response (not necessarily latest data).
- **Partition Tolerance (P)**: system continues operating despite dropped/delayed messages between nodes.

#### Practical interpretation
- In real distributed systems, **P is non-negotiable** (partitions happen).
- So under partition, systems effectively choose between **C** and **A**:
    - **CP**: prefer correctness, may reject/timeout requests.
    - **AP**: prefer responsiveness, may serve stale/conflicting data.

---

### 2) PACELC Theorem (More Realistic Tradeoff)

PACELC extends CAP by covering **normal operation** too:

- **If Partition (P) happens**: choose between **Availability (A)** and **Consistency (C)**.
- **Else (E), when no partition**: choose between **Latency (L)** and **Consistency (C)**.

So architectures are often described like:
- **PA/EL**: under partition favor availability, otherwise favor low latency.
- **PC/EC**: under partition favor consistency, otherwise still favor consistency over latency.
- Mixed behavior is common by service/use-case.

---

### 3) Why This Matters in Microservices

Microservices naturally create distributed boundaries:
- separate processes/services
- network calls (timeouts, retries, packet loss)
- independent data stores
- asynchronous messaging

This means CAP/PACELC tradeoffs happen in:
- synchronous API calls
- async event pipelines
- local transactions vs cross-service workflows
- caching and read models

---

### 4) Applying CAP/PACELC to the `ticketmaster` Project

From your workspace, `ticketmaster` has services like:
- `api-gateway`
- `event-service`
- `events-list-service`
- `booking-service`
- `payment-service`
- UI (`events-ui`)
- infra components (`kafka`, `redis`, `postgres`, `opensearch`, `keycloak`)

This is a classic distributed microservices architecture with both sync and async paths.

#### 4.1 Service-to-service API calls (via `api-gateway`)
Likely behavior:
- Gateway calls backend services over HTTP.
- On service outage/partition, you choose:
    - fail fast (more **CP-like** for critical writes),
    - fallback/default response (more **AP-like** for non-critical reads).

**Recommendation**
- For write-critical paths (booking/payment), prefer correctness:
    - return clear error/timeout rather than accept uncertain state.
- For read-heavy paths (event browsing), prefer availability:
    - allow stale data or degraded response.

#### 4.2 Kafka/event-driven flows (`docker-compose/kafka`)
Event-driven parts are usually **AP-leaning + eventual consistency**:
- producer and consumer can continue independently,
- consumers catch up later,
- temporary divergence between services is expected.

**Implication**
- Booking created now may appear in read models (`events-list-service`, search index) slightly later.
- This is acceptable if domain defines consistency windows.

#### 4.3 Per-service databases (seen in `data/*.mv.db`)
Each service owning its data is a microservices best practice, but implies:
- no single ACID transaction across all services,
- cross-service consistency achieved via saga/outbox/idempotency (your `booking-service/docs/saga.md` suggests this pattern).

**CAP/PACELC angle**
- You are intentionally trading strict global consistency for availability/scalability.

#### 4.4 Search/read model (`opensearch`) and cache (`redis`)
Search index/cache are typically eventually consistent projections:
- optimized for **low latency (EL)**,
- may be stale for short intervals after updates.

**Good fit**
- Event discovery UX usually tolerates slight staleness.
- Keep booking/payment confirmation paths backed by source-of-truth services, not cache/index alone.

---

### 5) Suggested Classification by Business Capability

Use this table in your docs:

| Capability | Consistency Need | Availability Need | Typical Choice |
|---|---|---|---|
| Event browsing/listing | Medium | High | AP-ish / PA-EL (stale-tolerant reads) |
| Seat reservation/booking | Very High | High (but below correctness) | CP-ish on writes + compensating workflows |
| Payment processing | Very High | High (controlled) | CP-ish, idempotent operations, explicit failure states |
| Search/autocomplete | Low-Medium | Very High | AP + EL (eventual consistency) |
| Auth (Keycloak integration) | High | High | Usually external CP-leaning core, with local graceful handling |

---

### 6) Design Patterns to Make CAP/PACELC Explicit in Ticketmaster

1. **Sagas for distributed transactions**
    - already aligned with `booking-service/docs/saga.md`.
    - define forward and compensating actions (reserve seat -> charge payment -> confirm; else rollback).

2. **Idempotency keys**
    - for booking/payment APIs and consumers to avoid duplicate effects on retries.

3. **Outbox + reliable event publishing**
    - prevent DB commit/event publish inconsistencies.

4. **Timeout budgets + retries + circuit breakers**
    - bound latency and prevent cascading failures through `api-gateway`.

5. **Read/write model separation**
    - writes in source services, reads in denormalized projections (`events-list-service`, `opensearch`).

6. **Business-level consistency SLAs**
    - e.g., “event list updates visible within 3 seconds”.

---

### 7) Example Failure Scenarios (Ticketmaster Context)

- **Partition between `booking-service` and `payment-service`:**
    - CP-style booking flow: hold booking in `PENDING_PAYMENT`, do not confirm seat until payment acknowledged.
    - AP-style alternative (riskier): accept booking and reconcile later.

- **`events-list-service` lagging Kafka:**
    - users see older availability briefly.
    - booking service remains source of truth for final purchase decision.

- **`opensearch` unavailable:**
    - degrade browse/search features,
    - keep booking/payment transactional core available if possible.

---

### 8) Practical Documentation Statement You Can Reuse

> In `ticketmaster`, CAP is handled per capability rather than globally.  
> For transactional flows (booking/payment), we prioritize consistency and explicit failure over silent divergence.  
> For discovery/read flows (event listing/search), we prioritize availability and latency, accepting bounded staleness through eventual consistency.  
> PACELC explains this dual behavior: under partitions we choose C vs A based on business criticality; otherwise we often choose low latency for user-facing reads and stronger consistency for financial/booking writes.

---

