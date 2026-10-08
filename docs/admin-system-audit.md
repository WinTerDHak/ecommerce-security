# Admin System Audit

## 1. Current Architecture
The current application utilizes a Spring Boot backend with a PostgreSQL database, secured by a JWT-based authentication layer (with robust RBAC via `ROLE_ADMIN` and `ROLE_CUSTOMER`). The frontend is built on React + TypeScript. While the core customer flow (product browsing, cart, checkout, payment) is stable, the Admin interfaces are largely incomplete with the exception of the recently built Product Management module.

## 2. Product Management
**Status: Implemented**
- **Frontend**: A dedicated React interface (`AdminDashboard.tsx`) allows an admin to list all products, create new products, and securely update/deactivate them. Visual badges identify stock thresholds (Còn hàng, Sắp hết, Hết hàng).
- **Backend API**: Endpoints under `/api/admin/products` (`POST`, `PUT`, `DELETE`) are fully guarded by `@PreAuthorize("hasRole('ADMIN')")`.
- **Data Inconsistency Root Cause**: The observed "Yoga Mat #197 vs #10" inconsistency is actually **not a bug**. Our security audit confirms that ID #197 is a distinct, soft-deleted (inactive) record spawned by previous automated integration tests or manual admin testing. The public `/products` catalog correctly excludes it via `findByActiveTrue()`, rendering only the original seed data (ID #10). The Admin Dashboard correctly displays both due to administrative visibility requirements.

## 3. Order Management
**Status: Partial (Customer side only)**
- **Backend**: `OrderController` exposes `POST /api/orders`, `GET /api/orders` and `GET /api/orders/{id}` for authenticated customers.
- **Missing**: There are no admin-facing endpoints (e.g., `GET /api/admin/orders` or status update `PUT` endpoints). Admin cannot view or fulfill customer orders.

## 4. User Management
**Status: Missing**
- **Backend**: `User` entity and `UserRepository` are solid. Registration and Auth work securely.
- **Missing**: There is no `AdminController` or equivalent to perform `GET /api/admin/users`. Admins cannot view user profiles, check roles, or lock malicious accounts.

## 5. Payment Management
**Status: Partial (Customer flow only)**
- **Backend**: VNPAY integrates flawlessly via `PaymentController` with IPN and return URL callbacks tracking success/fail.
- **Missing**: Admins lack a unified endpoint to trace payment transactions, view status, or reconcile transaction references against orders without direct database access.

## 6. Security & Audit Logs
**Status: Partial**
- **Backend**: `AuditLogService` is actively recording `LOGIN_SUCCESS`, `LOGOUT`, `USER_REGISTERED`, `PAYMENT_*`, and `ADMIN_PRODUCT_*` events.
- **Missing**: It fails to record security edge-cases such as `LOGIN_FAILED`, `ACCESS_DENIED`, or `RATE_LIMIT` breaches.
- **Missing API**: There is zero API exposure for the Admin to read these logs (`GET /api/admin/audit-logs`). 

## 7. Frontend Admin UI
**Status: Partial**
- **Implemented**: Sidebar layout, Product Management grid, Create/Edit Modals.
- **Missing**: Placeholder links exist for Orders, Users, and Security, but clicking them does nothing. The frontend lacks these pages entirely.

## 8. Backend APIs
All existing APIs strictly adhere to JWT extraction and `@AuthenticationPrincipal` validation. `ROLE_ADMIN` boundaries correctly block `CUSTOMER` and `UNAUTHENTICATED` requests. Security is robust but coverage is shallow.

## 9. Database Relationships
The database schema natively handles soft deletion and preserves referential integrity:
```
User
 ├── Orders
 ├── RefreshTokens
 └── AuditLogs

Order
 ├── OrderItems
 └── Payment

Product
 ├── Category
 └── OrderItems
```
(Orders and Payments are safely shielded from product deactivation).

## 10. Implemented / Partial / Missing Matrix

| Module | Backend API | Frontend UI | Security / DB | Status |
|---|---|---|---|---|
| Products | Yes | Yes | Yes | Implemented |
| Inventory | Yes | Yes | Yes | Implemented |
| Orders | No | No | Yes (Customer) | Partial |
| Users | No | No | Yes (Auth only) | Partial |
| Payments | No | No | Yes (VNPAY) | Partial |
| Security | Yes | No | Yes | Partial |
| Audit Logs | No | No | Yes (DB only) | Partial |

## 11. Recommended Implementation Order
1. **Admin Order Management**: Crucial for actual e-commerce operations. Needs APIs to list, filter, and modify shipping/fulfillment statuses.
2. **Admin User Management**: Needs APIs to list customers and lock/disable accounts.
3. **Admin Security/Audit Dashboard**: Extend the backend to query `AuditLogRepository` so admins can trace system actions. Needs to add missing events (failed logins, rate limits).
4. **Admin Payment visibility**: Read-only endpoints to reconcile VNPAY records.
5. **Dashboard statistics**: Aggregate data for the main `/admin` landing page.
