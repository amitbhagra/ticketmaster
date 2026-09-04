
## Ticketmaster SAGA — End-to-End Trace

This is a **Orchestration-based SAGA** using **Spring State Machine** (for state management in booking-service) and **Apache Kafka** (as the async message bus between services). BookingSagaManagerImpl is  central coordinator — each service reacts to events independently.

---

### Components Involved

| Component | Role |
|---|---|
| `BookingServiceImpl` | Entry point, creates booking |
| `BookingSagaManagerImpl` | SAGA orchestrator within booking-service |
| `BookingStateMachineConfig` | Defines state transitions |
| `BookingStateChangeInterceptor` | Persists state on every transition |
| `InitiatePaymentRequestAction` | Publishes to `payment_request` Kafka topic |
| `PaymentRequestListener` (payment-service) | Receives payment request from Kafka |
| `PaymentServiceImpl` | Creates pending payment, validates OTP |
| `PaymentResponseListener` (booking-service) | Receives payment result from Kafka |

---

### State Machine Definition

**States:** `NEW` → `PAYMENT_PENDING` → `CONFIRMED` or `FAILED`

**Terminal states:** `CONFIRMED`, `CANCELLED`, `FAILED`, `REFUNDED`

**Events:** `PAYMENT_INITIATED`, `PAYMENT_COMPLETED`, `PAYMENT_FAILED`

**Transitions:**
```
NEW  --[PAYMENT_INITIATED]--> PAYMENT_PENDING  (executes InitiatePaymentRequestAction)
PAYMENT_PENDING  --[PAYMENT_COMPLETED]--> CONFIRMED
PAYMENT_PENDING  --[PAYMENT_FAILED]-->    FAILED
```

---

### ✅ Happy Path (Payment Approved)

```
[Client] POST /bookings
      │
      ▼
BookingServiceImpl.create(BookingDTO)
  1. Maps DTO → Booking domain object
  2. Sets userId from JWT (SecurityContextHolder)
  3. Calls bookingOperationOrchestrator.newBooking(booking)
      │
      ▼
BookingSagaManagerImpl.newBooking(booking)
  4. Sets status = NEW, id = null
  5. Persists booking to DB (status=NEW)
  6. Calls sendEvent(savedBooking, PAYMENT_INITIATED)
      │
      ▼
BookingSagaManagerImpl.sendEvent(booking, PAYMENT_INITIATED)
  7. Builds StateMachine from factory (keyed by bookingId)
  8. Resets SM to current DB state (NEW) via DefaultStateMachineContext
  9. Registers BookingStateChangeInterceptor (persists state changes)
 10. Sends Message<BookingEvent.PAYMENT_INITIATED> with header BOOKING_ID_HEADER
      │
      ▼
StateMachine transition: NEW → PAYMENT_PENDING
  11. BookingStateChangeInterceptor.preStateChange():
        - Reads bookingId from message header
        - Updates booking.status = PAYMENT_PENDING and saves to DB
  12. Executes InitiatePaymentRequestAction:
        - Looks up Booking from DB by bookingId
        - Builds InitiatePaymentRequest{bookingId, amount}
        - Publishes to Kafka topic: "payment_request"
      │
      ▼ (async, cross-service)
[Kafka topic: payment_request]
      │
      ▼
PaymentRequestListener.listenWithHeaders() [payment-service]
  13. Receives InitiatePaymentRequest
  14. Calls paymentService.initiatePayment(paymentRequestDTO)
      │
      ▼
PaymentServiceImpl.initiatePayment(PaymentRequestDTO)
  15. Creates Payment record {bookingId, amount, status=PENDING}
  16. Generates random 6-digit OTP and stores it with the payment
  17. Saves Payment to DB
  [At this point, payment-service waits for user to submit OTP]
      │
[User calls validate payment endpoint with correct OTP]
      ▼
PaymentServiceImpl.validatePayment(ValidatePaymentDTO)
  18. Finds Payment by id
  19. Compares submitted OTP with stored OTP — MATCH ✅
  20. Sets payment.status = APPROVED
  21. Saves Payment to DB
  22. After DB commit (TransactionSynchronization.afterCommit):
        - Publishes PaymentStatusResponse{bookingId, status=APPROVED}
        - to Kafka topic: "payment_response"
      │
      ▼ (async, cross-service)
[Kafka topic: payment_response]
      │
      ▼
PaymentResponseListener.listenWithHeaders() [booking-service]
  23. Receives PaymentStatusResponse{bookingId, APPROVED}
  24. Calls bookingSagaManager.processPaymentResult(bookingId, APPROVED)
      │
      ▼
BookingSagaManagerImpl.processPaymentResult()
  25. Finds Booking by bookingId
  26. status == APPROVED → sendEvent(booking, PAYMENT_COMPLETED)
      │
      ▼
StateMachine transition: PAYMENT_PENDING → CONFIRMED
  27. BookingStateChangeInterceptor.preStateChange():
        - Updates booking.status = CONFIRMED and saves to DB

✅ Booking is now CONFIRMED
```

---

### ❌ Failure Path (Payment Declined / Wrong OTP)

```
[Steps 1–17 same as above]
      │
[User submits wrong OTP]
      ▼
PaymentServiceImpl.validatePayment(ValidatePaymentDTO)
  18. Finds Payment by id
  19. Compares OTP — NO MATCH ❌
  20. Sets payment.status = DECLINED
  21. Saves Payment to DB
  22. After DB commit: publishes PaymentStatusResponse{bookingId, status=DECLINED}
        to Kafka topic: "payment_response"
      │
      ▼
PaymentResponseListener [booking-service]
  23. Receives PaymentStatusResponse{bookingId, DECLINED}
  24. Calls bookingSagaManager.processPaymentResult(bookingId, DECLINED)
      │
      ▼
BookingSagaManagerImpl.processPaymentResult()
  25. Finds Booking by bookingId
  26. status != APPROVED → sendEvent(booking, PAYMENT_FAILED)
      │
      ▼
StateMachine transition: PAYMENT_PENDING → FAILED
  27. BookingStateChangeInterceptor.preStateChange():
        - Updates booking.status = FAILED and saves to DB

❌ Booking is now FAILED
```

---

### Key Design Notes

1. **State durability via Interceptor**: `BookingStateChangeInterceptor` fires `preStateChange` on every transition and persists the new state to the DB. This means if the service restarts, the SM is rebuilt from the persisted DB state (`DefaultStateMachineContext`).

2. **OTP-based payment validation**: The payment flow is not fully automated — `initiatePayment` merely creates a `PENDING` payment record with an OTP. The booking stays in `PAYMENT_PENDING` until the user explicitly calls `validatePayment`. This is the human task step in the SAGA.

3. **Outbox safety via `afterCommit`**: In `PaymentServiceImpl.validatePayment()`, the Kafka message is sent inside a `TransactionSynchronization.afterCommit()` callback, ensuring the message is only published **after** the DB transaction commits. This prevents a race condition where the event is published but the DB write rolls back.

4. **`BOOKING_ID_HEADER`**: The booking ID is threaded through the entire state machine as a message header, used by both the interceptor and the action to load the correct booking from the DB.

5. **No compensation (rollback) logic**: The `FAILED` state is a terminal end state. There is no compensation transaction to undo a previous step (e.g. no seat release logic visible in the codebase for this path), making it a simplified SAGA pattern.