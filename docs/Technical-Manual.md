# API Reference

> Base URL configurable: **VITE_API_BASE_URL** (local: `http://localhost:8080`, producción: ngrok URL).  
> Todas las rutas **/api/** devuelven JSON. Autenticación con **JWT** en `Authorization: Bearer <token>` salvo endpoints públicos.

## Auth
### POST /api/auth/login
**Body**
```json
{ "email": "admin@gmail.com", "password": "…" }
```
**200**
```json
{ "token": "eyJhbGciOiJIUzI1NiIs…" }
```

### GET /api/auth/me  (auth requerido)
**200**
```json
{ "id": 1, "name": "Admin", "email": "admin@gmail.com", "role": "ADMIN", "active": true }
```

---

## Users (ADMIN)
### GET /api/admin/users
Lista usuarios.

### PATCH /api/admin/users/{id}/suspend
Suspende usuario (`active=false`).

### PATCH /api/admin/users/{id}/activate
Reactiva usuario (`active=true`).

**200**
```json
{ "id": 8, "name": "Moderadora Uno", "email": "mod1@gmail.com", "role": "MODERATOR", "active": true }
```

---

## Products
### GET /api/products/public
Catálogo público (solo `APPROVED`). **Público**.

**200**
```json
[
  { "id": 12, "name": "Teclado Mecánico", "price": 450.0, "imageUrl": "…", "stock": 3, "category": "TECHNOLOGY" }
]
```

### GET /api/products/mine (COMMON)
Productos del usuario autenticado (cualquier status).

### POST /api/products (COMMON)
Crea un producto (queda `PENDING`).
**Body**
```json
{ "name":"Laptop usada", "description":"Core i5, 8GB", "imageUrl":"https://picsum.photos/seed/lap/800/600", "price":2500.0, "stock":3, "condition":"USED", "category":"TECHNOLOGY" }
```
**201**
```json
{ "id": 42, "status": "PENDING" }
```

### PUT /api/products/{id} (COMMON)
Actualiza producto propio. Pasa a `PENDING` para re‑revisión.

---

## Moderation (MODERATOR)
### GET /api/moderation/products/pending
Lista de productos `PENDING`.

### PATCH /api/moderation/products/{id}/approve
Aprueba.

### PATCH /api/moderation/products/{id}/reject
Rechaza.

**200**
```json
{ "id": 42, "status": "APPROVED" }
```

---

## Cart (COMMON)
### GET /api/cart
Obtiene carrito del usuario.
**200**
```json
{ "id": 7, "items": [ { "productId": 12, "name": "Teclado", "quantity": 2, "unitPrice": 450.0, "lineTotal": 900.0 } ], "subtotal": 900.0 }
```

### POST /api/cart/add
Agrega producto.
**Body**
```json
{ "productId": 12, "quantity": 1 }
```

### POST /api/cart/remove
Elimina producto del carrito.
**Body**
```json
{ "productId": 12 }
```

---

## Saved Cards (COMMON)
### GET /api/cards
Lista tarjetas del usuario.

### POST /api/cards
Crea tarjeta.
**Body**
```json
{ "holderName":"Carlos", "cardNumber":"4111111111111111", "expMonth":12, "expYear":2028, "cvv":"123" }
```

---

## Orders
### POST /api/orders/checkout (COMMON)
Realiza el pago y genera orden.
**Body (usar tarjeta guardada)**
```json
{ "paymentMethod": "SAVED", "cardId": 2 }
```
**Body (nueva tarjeta)**
```json
{ "paymentMethod": "NEW", "holderName":"Carlos", "cardNumber":"4111111111111111", "expMonth":12, "expYear":2028, "cvv":"123" }
```
**200**
```json
{ "orderId": 101, "status": "PLACED", "total": 1250.00 }
```

### GET /api/orders/mine (COMMON)
Lista de órdenes del usuario.

### PATCH /api/logistics/orders/{id}/ship (LOGISTICS)
Cambia a `SHIPPED`.

### PATCH /api/logistics/orders/{id}/deliver (LOGISTICS)
Cambia a `DELIVERED`.

---

## Reports (ADMIN)
> Filtros `from` y `to` en formato `YYYY-MM-DD`. `limit` por página.

### GET /api/reports/top-products
**Params**: `from`, `to`, `limit` (default 10)
**200**
```json
[ { "productId": 2, "productName": "Mouse G305", "unitsSold": 15, "revenue": 1875.0 } ]
```

### GET /api/reports/top-customers-spend
**Params**: `from`, `to`, `limit` (default 5)
**200**
```json
[ { "customerId": 3, "customerName": "Carlos López", "ordersCount": 6, "itemsCount": 22, "totalSpent": 7800.0 } ]
```

### GET /api/reports/top-sellers-units
**Params**: `from`, `to`, `limit` (default 5)

### GET /api/reports/top-customers-orders
**Params**: `from`, `to`, `limit` (default 10)

### GET /api/reports/top-sellers-active-listings
**Params**: `limit` (default 10)

---

## Status Codes comunes
- **200 OK** – Éxito / datos devueltos
- **201 Created** – Recurso creado
- **204 No Content** – Acción aplicada sin cuerpo
- **400 Bad Request** – Petición inválida (validación)
- **401 Unauthorized** – Falta/expiró token
- **403 Forbidden** – Rol insuficiente
- **404 Not Found** – Recurso no existe o no pertenece al usuario

## Seguridad / Notas
- En controllers se usa `@PreAuthorize` por rol.
- CORS: añadir dominios de Netlify/ngrok en `SecurityConfig`.
- ngrok (free): header `ngrok-skip-browser-warning: true` ya lo gestiona el cliente axios.
