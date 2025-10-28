# Modelo de Datos (Conceptual, Lógico y Físico)

Este documento resume el **esquema conceptual (Peter‑Chen)**, el **esquema lógico (relacional)** y apunta al **esquema físico (PostgreSQL)** usado por el proyecto.

---

## 1) Esquema Conceptual — Notación Peter‑Chen

**Entidades y atributos (Peter‑Chen)**

```mermaid
flowchart LR
  %% Entidades (rectángulos)
  USER["USER"]
  PRODUCT["PRODUCT"]
  SAVED_CARD["SAVED_CARD"]
  CART["CART"]
  CART_ITEM["CART_ITEM"]
  ORDER["ORDER"]
  ORDER_ITEM["ORDER_ITEM"]

  %% Atributos (óvalos)
  %% USER
  u_id((id)):::att --> USER
  u_name((name)):::att --> USER
  u_email((email)):::att --> USER
  u_pass((passwordHash)):::att --> USER
  u_role((role)):::att --> USER
  u_active((active)):::att --> USER

  %% PRODUCT
  p_id((id)):::att --> PRODUCT
  p_name((name)):::att --> PRODUCT
  p_desc((description)):::att --> PRODUCT
  p_img((imageUrl)):::att --> PRODUCT
  p_price((price)):::att --> PRODUCT
  p_stock((stock)):::att --> PRODUCT
  p_cat((category)):::att --> PRODUCT
  p_cond((condition)):::att --> PRODUCT
  p_status((status)):::att --> PRODUCT
  p_created((createdAt)):::att --> PRODUCT
  p_updated((updatedAt)):::att --> PRODUCT

  %% SAVED_CARD
  sc_id((id)):::att --> SAVED_CARD
  sc_holder((holderName)):::att --> SAVED_CARD
  sc_num((cardNumber)):::att --> SAVED_CARD
  sc_mm((expMonth)):::att --> SAVED_CARD
  sc_yy((expYear)):::att --> SAVED_CARD
  sc_cvv((cvv)):::att --> SAVED_CARD
  sc_created((createdAt)):::att --> SAVED_CARD

  %% CART
  c_id((id)):::att --> CART
  c_created((createdAt)):::att --> CART
  c_updated((updatedAt)):::att --> CART

  %% CART_ITEM
  ci_id((id)):::att --> CART_ITEM
  ci_qty((quantity)):::att --> CART_ITEM
  ci_price((unitPrice)):::att --> CART_ITEM

  %% ORDER
  o_id((id)):::att --> ORDER
  o_created((createdAt)):::att --> ORDER
  o_eta((eta)):::att --> ORDER
  o_sub((subtotal)):::att --> ORDER
  o_fee((fee)):::att --> ORDER
  o_total((total)):::att --> ORDER
  o_status((status)):::att --> ORDER

  %% ORDER_ITEM
  oi_id((id)):::att --> ORDER_ITEM
  oi_qty((quantity)):::att --> ORDER_ITEM
  oi_price((unitPrice)):::att --> ORDER_ITEM

  %% Relaciones (rombos)
  R1{publica}:::rel
  R2{posee}:::rel
  R3{contiene}:::rel
  R4{esta_en}:::rel
  R5{guarda}:::rel
  R6{realiza}:::rel
  R7{incluye}:::rel
  R8{se_vende_en}:::rel

  %% Conexiones Entidad–Relación (con etiquetas de cardinalidad)
  USER -- "1" --- R1
  R1  -- "N" --- PRODUCT

  USER -- "1" --- R2
  R2  -- "1" --- CART

  CART -- "1" --- R3
  R3   -- "N" --- CART_ITEM

  PRODUCT -- "1" --- R4
  R4      -- "N" --- CART_ITEM

  USER -- "1" --- R5
  R5  -- "N" --- SAVED_CARD

  USER -- "1" --- R6
  R6  -- "N" --- ORDER

  ORDER -- "1" --- R7
  R7   -- "N" --- ORDER_ITEM

  PRODUCT -- "1" --- R8
  R8      -- "N" --- ORDER_ITEM

  classDef att fill:#fff,stroke:#888,stroke-width:1px,color:#333;
  classDef rel fill:#fff,stroke:#555,stroke-width:1.2px,color:#333,stroke-dasharray: 3 3;
```

**Relaciones (cardinalidades)**
- USER **1..N** —publica→ **PRODUCT**  (PRODUCT.owner = USER.id)
- USER **1..1** —posee→ **CART**  (CART.owner = USER.id)
- CART **1..N** —contiene→ **CART_ITEM**  (CART_ITEM.cart = CART.id)
- PRODUCT **1..N** —está_en→ **CART_ITEM**  (CART_ITEM.product = PRODUCT.id)
- USER **1..N** —guarda→ **SAVED_CARD**  (SAVED_CARD.owner = USER.id)
- USER **1..N** —realiza→ **ORDER**  (ORDER.buyer = USER.id)
- ORDER **1..N** —incluye→ **ORDER_ITEM**  (ORDER_ITEM.order = ORDER.id)
- PRODUCT **1..N** —se_vende_en→ **ORDER_ITEM**  (ORDER_ITEM.product = PRODUCT.id)

> Dominios: 
> - `role ∈ {ADMIN, MODERATOR, LOGISTICS, COMMON}`
> - `product.status ∈ {PENDING, APPROVED, REJECTED}`
> - `product.condition ∈ {NEW, USED}`
> - `product.category ∈ {TECHNOLOGY, HOME, ACADEMIC, PERSONAL, DECORATION, OTHER}`
> - `order.status ∈ {PLACED, SHIPPED, DELIVERED}`

> **Nota**: Si tu visor soporta Mermaid, puedes renderizar un ER rápido:
>
> ```mermaid
> erDiagram
>   USER ||--o{ PRODUCT : owns
>   USER ||--|| CART : has
>   CART ||--o{ CART_ITEM : contains
>   PRODUCT ||--o{ CART_ITEM : appears_in
>   USER ||--o{ SAVED_CARD : stores
>   USER ||--o{ ORDER : places
>   ORDER ||--o{ ORDER_ITEM : includes
>   PRODUCT ||--o{ ORDER_ITEM : sold_as
> ```

---

## 2) Esquema Lógico — Relacional

**USER**(id PK, name, email UQ, password_hash, role, active, created_at, updated_at)

**PRODUCT**(id PK, owner_id FK→USER.id, name, description, image_url, price, stock, category, condition, status, created_at, updated_at)

**SAVED_CARD**(id PK, owner_id FK→USER.id, holder_name, card_number, exp_month, exp_year, cvv, created_at)

**CART**(id PK, owner_id FK→USER.id, created_at, updated_at, UQ(owner_id))

**CART_ITEM**(id PK, cart_id FK→CART.id, product_id FK→PRODUCT.id, quantity, unit_price, UQ(cart_id, product_id))

**ORDER**(id PK, buyer_id FK→USER.id, created_at, eta, subtotal, fee, status, total)

**ORDER_ITEM**(id PK, order_id FK→ORDER.id, product_id FK→PRODUCT.id, quantity, unit_price)

**Índices sugeridos**
- `PRODUCT(status, category)`
- `ORDER(buyer_id, created_at)`
- `ORDER_ITEM(product_id)`
- `CART_ITEM(cart_id)`
- `SAVED_CARD(owner_id)`

```mermaid
erDiagram
  USER ||--o{ PRODUCT : owns
  USER ||--|| CART : has
  CART ||--o{ CART_ITEM : contains
  PRODUCT ||--o{ CART_ITEM : appears_in
  USER ||--o{ SAVED_CARD : stores
  USER ||--o{ ORDER : places
  ORDER ||--o{ ORDER_ITEM : includes
  PRODUCT ||--o{ ORDER_ITEM : sold_as
```

---

## 3) Esquema Físico — PostgreSQL

La definición DDL completa está en: `db/schema.sql`

Incluye:
- `CHECK` para dominios (roles, estatus, categorías, etc.)
- Claves foráneas con `ON UPDATE CASCADE` y `ON DELETE` apropiado
- Índices y restricciones `UNIQUE`
- Seeds mínimos comentados

---

## 4) Notas de implementación
- Los cambios de **status** en `Product` implican flujo de moderación.
- En `Order`, `status` inicia en `PLACED` y avanza a `SHIPPED`/`DELIVERED` por logística.
- El **split** de ingresos (95% vendedor, 5% plataforma) se calcula a nivel de servicio (checkout/reportes), no se persiste como columnas separadas por simplicidad.
