# ECommerceGT

Plataforma de **e‑commerce** desarrollada con arquitectura **Cliente‑Servidor (Nivel 2)**:  
Frontend (Vue 3 + Vite + Netlify) consumiendo API (Spring Boot + JWT) expuesta mediante **ngrok**.

## Funcionalidades principales
- Autenticación JWT con roles (**ADMIN**, **MODERATOR**, **LOGISTICS**, **COMMON**).
- Publicación, moderación y compra de productos.
- Carrito de compras y pagos con tarjetas guardadas.
- Logística: actualización de estado de órdenes (**SHIPPED**, **DELIVERED**).
- Reportes administrativos (Top productos, clientes, vendedores y pedidos).

## Estructura del proyecto
```
.
├─ api/                  # Backend (Spring Boot, Java 17+)
│  ├─ src/main/java/com/ecommercegt/api
│  └─ ...
├─ web/                  # Frontend (Vue 3 + Vite)
│  ├─ src/
│  ├─ public/_redirects  # SPA redirects para Netlify
│  └─ ...
└─ docs/                 # Documentación técnica y funcional
```

## Roles demo
| Rol | Usuario | Descripción |
|------|-----------|-------------|
| ADMIN | `admin@gmail.com` | Acceso a /admin y /admin/reports |
| MODERATOR | `mod1@gmail.com` | Revisa/aprueba/rechaza productos |
| LOGISTICS | `log1@gmail.com` | Cambia estado de órdenes |
| COMMON | `user1@gmail.com` | Publica, compra y consulta productos |

## Configuración
### Frontend
Crea `.env` en `/web` o usa variables de Netlify:
```
VITE_API_BASE_URL=http://localhost:8080   # o https://<tu-ngrok>.ngrok-free.app
```

### Backend
Edita `api/src/main/resources/application.properties`:
- Configura tu base de datos PostgreSQL.
- Asegura que los orígenes CORS incluyan tu dominio Netlify/ngrok.

## Ejecución local
### Backend
```bash
cd api
./mvnw spring-boot:run
```

### Frontend
```bash
cd web
npm install
npm run dev
```
Abre http://localhost:5173

## Despliegue
### Frontend (Netlify)
- **Base directory:** `web`
- **Build command:** `npm run build`
- **Publish directory:** `web/dist`
- Archivo `web/public/_redirects`:
```
/*    /index.html   200
```
- Variable de entorno: `VITE_API_BASE_URL` → URL del backend (ngrok o servidor).

### Backend (ngrok)
```bash
ngrok http 8080
```
Usa la URL HTTPS generada como base de tu API.

---

## Documentación completa
| Documento                                       | Descripción |
|-------------------------------------------------|-------------|
| [Thecnical-Manual.md](docs/Technical-Manual.md) | Arquitectura, stack, despliegue y configuración |
| [User-Manual.md](docs/User-Manual.md)           | Guía funcional paso a paso por rol |
| [Data-Model.md](docs/Data-Model.md)             | Modelo de datos: esquemas conceptual, lógico y físico |

---

## 🧪 Tecnologías utilizadas
- **Backend:** Spring Boot 3.5, Spring Security, JPA/Hibernate, PostgreSQL
- **Frontend:** Vue 3, Vite, Pinia, Axios, Netlify
- **Integraciones:** ngrok (exposición segura HTTPs)

---

---
> **Autor:** Carlos López – Proyecto académico (ECommerceGT)  
> **Entrega:** Octubre 2025