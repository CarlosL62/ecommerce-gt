# Manual de Usuario

## 1. Acceso e Inicio de Sesión
1. Ingrese a la URL del sitio desplegado (Netlify).
2. Seleccione la opción **Iniciar Sesión** e introduzca su correo y contraseña.

![Login](docs/screenshots/login.png)

3. Una vez autenticado, el sistema detectará su rol y mostrará el menú correspondiente.

## 2. Roles y Funciones
| Rol | Funcionalidad principal |
|-----|--------------------------|
| **COMMON** | Ver catálogo, agregar productos al carrito, pagar, publicar productos propios. |
| **MODERATOR** | Revisar productos pendientes, aprobar o rechazar publicaciones. |
| **LOGISTICS** | Visualizar y actualizar el estado de órdenes (SHIPPED, DELIVERED). |
| **ADMIN** | Gestionar usuarios y generar reportes administrativos. |

## 3. Flujos por Rol

### 3.1 Usuario Común (COMMON)
- **Comprar productos:**
  1. Acceda al **Catálogo**.
  2. Haga clic en **Agregar al carrito** en los productos deseados.

![Agregar al carrito](docs/screenshots/add_cart.png)

  3. Abra su **Carrito** y revise los artículos.
  4. Seleccione una **tarjeta guardada** o ingrese una nueva y haga clic en **Pagar**.

![Pago con tarjeta](docs/screenshots/payment.png)

  5. El pedido aparecerá en **Mis pedidos** con estado inicial *PLACED*.

![Pedido generado](docs/screenshots/order_info.png)

- **Publicar un producto:**
  1. Diríjase a la sección **Publicar**.
  2. Complete el formulario (nombre, descripción, imagen, precio, stock, categoría, condición).

![Formulario de publicación](docs/screenshots/publish_product.png)

  3. Envíe el producto (quedará en estado *PENDING* hasta ser aprobado por un moderador).

### 3.2 Moderador (MODERATOR)
- Acceda a la vista **Productos pendientes**.
- Revise cada publicación y haga clic en **Aprobar** o **Rechazar**.

![Vista de moderación](docs/screenshots/mod_review.png)

- Una vez aprobada, el producto pasa al catálogo público.

### 3.3 Logística (LOGISTICS)
- Desde **Órdenes**, seleccione una orden activa.
- Cambie su estado a **SHIPPED** cuando el producto haya sido despachado.

![Vista de logística](docs/screenshots/log_orders.png)

- Finalmente, marque como **DELIVERED** cuando el cliente confirme la entrega.

### 3.4 Administrador (ADMIN)
- Ingrese a **Reportes** desde el menú principal.
- Use los filtros de fecha y límite para generar estadísticas:

![Vista de reportes](docs/screenshots/admin_reports.png)

  - Top 10 productos más vendidos.
  - Top 5 clientes por gasto.
  - Top 5 vendedores por unidades vendidas.
  - Top 10 clientes con más pedidos.
  - Top 10 vendedores con más productos activos.

## 4. Consejos y Mensajes Comunes
| Situación | Mensaje / Solución |
|------------|--------------------|
| No se puede pagar | Verifique que haya artículos en el carrito y al menos una tarjeta registrada. |
| Producto rechazado | Revise la descripción y la categoría antes de volver a publicarlo. |
| Rol incorrecto | Cierre sesión y vuelva a iniciar con el usuario adecuado. |
| Error 403 / 401 | Sesión expirada o permisos insuficientes; vuelva a iniciar sesión. |

## 5. Preguntas Frecuentes
- **¿Puedo editar mis productos?** Sí, desde *Mis productos* usando el ícono de edición. El producto regresará a revisión.  
- **¿Dónde veo mis pedidos?** En *Mis pedidos* dentro del portal de usuario.  
- **¿Cómo agrego tarjetas?** Desde el carrito o la vista de pago, haciendo clic en *Agregar nueva tarjeta*.  
- **¿Puedo cambiar mi contraseña?** Actualmente no; contacte con el administrador para hacerlo manualmente.  

## 6. Soporte
Para incidencias:
1. Tome una captura de pantalla del error.
2. Anote los pasos previos al fallo.
3. Contacte al equipo técnico indicando el rol y la hora del problema.

**Capturas sugeridas:** guarde las imágenes de evidencia en la carpeta `docs/screenshots/` o insértelas directamente aquí con la sintaxis Markdown: `![Descripción](docs/screenshots/nombre-de-imagen.png)`.

## 7. Glosario Básico
| Término | Definición breve |
|----------|------------------|
| **Producto** | Artículo publicado por un usuario y disponible para compra. |
| **Orden** | Registro de compra que contiene productos, totales y estado. |
| **Moderador** | Usuario encargado de aprobar o rechazar productos. |
| **Administrador** | Usuario con privilegios totales del sistema. |
| **Carrito** | Contenedor temporal de productos antes de pagar. |

---
> Este manual resume las funciones y flujos disponibles para todos los roles dentro de la plataforma **ECommerceGT**.
