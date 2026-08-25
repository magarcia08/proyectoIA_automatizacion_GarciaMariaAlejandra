# Frontend LogiTrack

Frontend **HTML/CSS/JavaScript puro** (sin frameworks ni build step) que consume la API REST
de LogiTrack. Vive dentro de `src/main/resources/static`, por lo que el propio backend de
Spring Boot lo sirve automáticamente como contenido estático: no hace falta Node, npm, ni un
servidor aparte.

---

## 1. Cómo ejecutarlo

Con el backend corriendo (`./mvnw spring-boot:run` desde la raíz del proyecto), abre directamente:

- `http://localhost:8080/` → landing page
- `http://localhost:8080/login.html` → inicio de sesión
- `http://localhost:8080/dashboard.html` → panel principal (requiere estar logueado)

### Usuarios de prueba

| Username    | Password      | Rol        |
|-------------|---------------|------------|
| superadmin  | superadmin123 | SUPERADMIN |
| admin       | admin123      | ADMIN      |
| jperez      | empleado123   | EMPLEADO   |

### Consumir un backend distinto

Si quieres servir este frontend por separado (por ejemplo con Live Server, en otro puerto,
durante el desarrollo), edita `js/api.js`:

```js
const API = {
    BASE_URL: "http://localhost:8080", // en vez de "" (mismo origen)
    ...
};
```

---

## 2. Estructura de archivos

```
static/
 ├─ index.html          -> landing page pública
 ├─ login.html           -> formulario de inicio de sesión
 ├─ dashboard.html        -> shell de la SPA (sidebar + header + contenedor de módulos)
 ├─ css/
 │   ├─ variables.css     -> paleta de colores, espaciados
 │   ├─ global.css        -> layout base, tipografía, botones
 │   ├─ login.css         -> estilos exclusivos del login
 │   ├─ dashboard.css     -> sidebar, header, tarjetas KPI, tablas
 │   ├─ form.css          -> formularios y modales
 │   ├─ table.css         -> tablas de datos
 │   ├─ animation.css     -> fade-in, spinner de carga
 │   └─ responsive.css    -> breakpoints móviles
 ├─ js/
 │   ├─ api.js            -> configuración central (BASE_URL, rutas) + peticionApi()
 │   ├─ auth.js           -> lógica de login.html
 │   ├─ dashboard.js       -> arranque del dashboard, navegación entre módulos, resumen/KPIs
 │   ├─ utils.js           -> helpers compartidos (formato, toasts, modales, badges de stock)
 │   ├─ bodega.js          -> módulo Bodegas
 │   ├─ producto.js        -> módulo Productos
 │   ├─ inventario.js      -> módulo Inventario
 │   ├─ movimiento.js      -> módulo Movimientos
 │   ├─ auditoria.js       -> módulo Auditoría
 │   ├─ reporte.js         -> módulo Reportes
 │   └─ usuario.js         -> módulo Usuarios
 └─ pages/                -> fragmentos HTML de cada módulo, cargados dinámicamente
     ├─ bodega.html
     ├─ producto.html
     ├─ inventario.html
     ├─ movimiento.html
     ├─ auditoria.html
     ├─ reporte.html
     └─ usuario.html
```

---

## 3. Cómo funciona (arquitectura del frontend)

### 3.1 `api.js`: capa única de acceso al backend

Todas las peticiones pasan por `peticionApi(ruta, opciones)`:

- Agrega automáticamente el header `Authorization: Bearer <token>` (excepto en `/auth/login`),
  tomando el token guardado en `localStorage` tras el login.
- Centraliza el manejo de errores: si la respuesta no es `ok`, lanza un `Error` con el mensaje
  del backend (`error.message`) y, si existen, el detalle de validaciones (`error.detalles`).
- `API.RUTAS` mapea nombres lógicos (`LOGIN`, `BODEGAS`, `PRODUCTOS`, ...) a las rutas reales
  del backend, para no repetir strings sueltos en cada módulo.

### 3.2 Autenticación

- `auth.js` valida el formulario de `login.html`, llama a `POST /auth/login` y, si es exitoso,
  guarda el `token` JWT y los datos básicos del usuario (`username`, `rol`) en `localStorage`
  (`guardarToken` / `guardarUsuario` en `api.js`).
- `dashboard.js` verifica al cargar (`prepararSesion`) que exista un token; si no, redirige a
  `login.html`. El logout (`cerrarSesion`) simplemente borra el token/usuario de `localStorage`.
- El token no se refresca: expira a las 24h y hay que loguear de nuevo.

### 3.3 Dashboard como mini-SPA

`dashboard.html` es el único "shell": sidebar + header + un contenedor `#mainContent`.
`dashboard.js` implementa una navegación cliente muy simple, sin router ni framework:

1. Al hacer clic en un ítem del menú, `abrirModulo(nombre)` hace `fetch("pages/<nombre>.html")`
   e inyecta el fragmento HTML en `#mainContent`.
2. Luego llama a `iniciarModulo(nombre)`, que invoca la función `iniciar*()` correspondiente
   (`iniciarBodegas`, `iniciarProductos`, etc.), definida en el `.js` de ese módulo, la cual
   pide los datos a la API y pinta la tabla/formulario.
3. La vista de inicio (`mostrarInicio`) combina tres llamadas en paralelo
   (`GET /reportes/resumen`, `GET /productos`, `GET /movimientos`) para armar las tarjetas KPI,
   la barra de stock por bodega y la actividad reciente.

### 3.4 Control de acceso en la UI

`aplicarPermisosNavegacion()` oculta del menú lateral los módulos de **Usuarios** y
**Auditoría**, y los botones de crear/editar/eliminar en Bodegas/Productos, cuando el usuario
logueado es `EMPLEADO`. Esto es solo cosmético: el backend igual rechaza esas operaciones con
`403` si se intentan de todos modos; la UI únicamente evita mostrar acciones que fallarían.

### 3.5 Utilidades comunes (`utils.js`)

- `mostrarToast(mensaje, tipo)`: notificaciones flotantes de éxito/error.
- `abrirModal(id)` / `cerrarModal(id)` / `configurarCierreModal(id)`: modales genéricos
  (usados por ejemplo para ver el detalle JSON de una auditoría, o crear/editar registros).
- `estadoStock(stock)`: badge de color según el nivel de inventario (`Crítico` < 10,
  `Bajo` < 25, `Disponible` en otro caso).
- `formatoMoneda`, `formatoNumero`, `formatoFecha`: formateo con `Intl` en locale `es-CO`.
- `escaparHtml(valor)`: escapa texto antes de insertarlo en el DOM vía `innerHTML`, para
  evitar HTML/JS injection al mostrar datos que vienen del backend (nombres, comentarios, etc.).
- `esAdminOSuperior()`: helper de rol, usado por varios módulos para decidir qué botones pintar.

### 3.6 Funcionalidad por módulo

- **Bodegas / Productos**: CRUD completo (listar, crear, editar, eliminar), con búsqueda por
  nombre/ubicación/categoría contra los query params del backend.
- **Inventario**: consulta de stock por bodega, con switch "Solo stock bajo (< 10)" que cambia
  entre `GET /inventario` y `GET /inventario/stock-bajo`.
- **Movimientos**: formulario con desglose dinámico de productos (agregar/quitar filas de
  producto + cantidad en un mismo movimiento) y filtro por rango de fechas
  (`GET /movimientos?desde=&hasta=`).
- **Auditoría** (solo ADMIN/SUPERADMIN): filtro por usuario y tipo de operación, y un botón
  "Ver" por fila que abre un modal con el JSON de `valoresAnteriores` / `valoresNuevos`.
- **Usuarios** (solo ADMIN/SUPERADMIN): listar, activar/desactivar y crear usuarios respetando
  la jerarquía de roles (un ADMIN solo puede crear EMPLEADO; SUPERADMIN crea ADMIN o EMPLEADO).
- **Reportes**: resumen general, stock por bodega y ranking de productos más movidos.

---

## 4. Notas

- No hay build step ni dependencias externas: es HTML/CSS/JS servido tal cual, sin bundler.
- Los mensajes de error de formularios muestran el detalle completo devuelto por el backend
  (por ejemplo `capacidad: La capacidad no puede ser negativa`), no solo un mensaje genérico.
- Hay un enlace directo a Swagger UI en el pie del menú lateral del dashboard.
