# LogiTrack — Sistema de gestión y auditoría de bodegas + LogiTrack IQ

Backend REST desarrollado en **Spring Boot 3.3 (Java 17)** para LogiTrack S.A., que centraliza el control de
inventarios entre bodegas, registra automáticamente auditorías de cada cambio y protege todos los endpoints
con autenticación **JWT**. Incluye un frontend estático (HTML/CSS/JS) que consume la API.

Este repositorio incluye, además del reto original, la extensión **LogiTrack IQ** (torre de control de
inventario): detección de productos en riesgo, órdenes de compra con flujo de estados, PDF con marca de agua,
un servidor MCP, un flujo n8n y un dashboard nuevo. Ver la sección **[12. LogiTrack IQ](#12-logitrack-iq)**
más abajo y [`docs/enunciado-logitrack-iq.md`](docs/enunciado-logitrack-iq.md) para el enunciado completo.

---

## 1. Tecnologías

| Componente          | Tecnología                                   |
|----------------------|-----------------------------------------------|
| Lenguaje / Runtime   | Java 17                                       |
| Framework            | Spring Boot 3.3.4 (Web, Data JPA, Security, Validation) |
| Base de datos        | PostgreSQL                                    |
| Seguridad            | Spring Security + JWT (JJWT 0.12.6)           |
| Documentación API    | springdoc-openapi (Swagger UI)                |
| PDF (LogiTrack IQ)   | Apache PDFBox 3                               |
| Automatización       | n8n (AI Agent) + servidor MCP propio (Node.js) |
| Pruebas              | JUnit 5 + PostgreSQL real embebido (`io.zonky.test:embedded-postgres`, sin Docker) |
| Build                | Maven (empaquetado `.jar`)                    |
| Frontend             | HTML / CSS / JavaScript puro                  |

---

## 2. Estructura del proyecto

```
demoproject/
 ├─ src/main/java/com/project/springboot/demoproject/
 │   ├─ controllers/      -> Endpoints REST
 │   ├─ services/         -> Lógica de negocio
 │   ├─ repositories/     -> Spring Data JPA
 │   ├─ entities/         -> Entidades JPA (mapeadas 1:1 al schema.sql)
 │   ├─ dto/               -> Request/Response DTOs (+ dto/auth, dto/reportes)
 │   ├─ enums/             -> Rol, TipoMovimiento, TipoOperacionAuditoria
 │   ├─ security/          -> JWT, SecurityConfig, UserDetailsService
 │   ├─ audit/             -> Auditoría automática vía JPA EntityListeners
 │   ├─ exception/         -> GlobalExceptionHandler (@ControllerAdvice) + excepciones
 │   ├─ config/            -> OpenAPI / Swagger
 │   └─ testGarciaMaria/   -> Modulo de Reportes con filtros (examen, ver seccion 6.1)
 ├─ src/main/resources/
 │   ├─ application.properties
 │   ├─ schema.sql         -> DDL de PostgreSQL (proporcionado)
 │   ├─ data.sql           -> Datos de prueba (usuarios, bodegas, productos...)
 │   └─ static/            -> Frontend (HTML/CSS/JS), servido por el propio Spring Boot
 │       ├─ index.html, login.html, dashboard.html
 │       ├─ css/  js/  pages/  (incluye pages/torre-control.html, js/torre-control.js)
 ├─ pom.xml
 ├─ mcp-server/        -> Servidor MCP de LogiTrack IQ (Node.js, 6 herramientas)
 ├─ n8n/                -> Export del flujo "Resumen diario de inventario"
 ├─ skills/operacion-logitrack/SKILL.md  -> Reglas del flujo automatizado
 ├─ frontend/           -> Apunta a src/main/resources/static (ver frontend/README.md)
 └─ docs/
     ├─ enunciado-logitrack-iq.md, diagrama-flujo.md, evidencia-flujo-completo.md
     └─ sdd/            -> Propuesta, especificación, diseño, tareas, evidencia SDD/TDD
```

---

## Pantallazos examen - pruebas Thunder Client

Pruebas de `GET /reportes/movimientos` y `GET /reportes/auditoria` con distintas combinaciones
de filtros, hechas con la extensión **Thunder Client** de VS Code (login previo en `/auth/login`
para obtener el token JWT usado en el header `Authorization`).

| # | Captura | Qué prueba |
|---|---------|------------|
| 1 | ![cap1](docs/capturas/cap1thunder.png) | Login (`POST /auth/login`) y token JWT obtenido |
| 2 | ![cap2](docs/capturas/cap2thunder.png) | `GET /reportes/movimientos` sin filtros |
| 3 | ![cap3](docs/capturas/cap3thunder.png) | `GET /reportes/movimientos` filtrado por `tipoMovimiento=SALIDA` y rango de fechas |
| 4 | ![cap4](docs/capturas/cap4thunder.png) | `GET /reportes/movimientos` filtrado por `bodega` y `producto` |
| 5 | ![cap5](docs/capturas/cap5thunder.png) | `GET /reportes/auditoria` sin filtros |
| 6 | ![cap6](docs/capturas/cap6thunder.png) | `GET /reportes/auditoria` filtrado por `producto` |
| 7 | ![cap7](docs/capturas/cap7thunder.png) | `GET /reportes/auditoria` filtrado por `campoModificado` |

> Ajusta la columna "Qué prueba" si alguna captura corresponde a otra combinación de filtros.

---

## 3. Instalación y ejecución

### 3.1 Prerrequisitos
- JDK 17
- Maven (o usar el wrapper `./mvnw` incluido)
- PostgreSQL 14+ corriendo localmente (o accesible por red)

### 3.2 Crear la base de datos

```sql
CREATE DATABASE logitrack;
```

El propio Spring Boot ejecuta `schema.sql` y `data.sql` automáticamente al arrancar
(`spring.sql.init.mode=always`), así que **no hay que correr el script a mano**: solo
necesitas la base de datos vacía creada.

### 3.3 Configurar credenciales

Edita `src/main/resources/application.properties` (o usa variables de entorno):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/logitrack
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3.4 Compilar y ejecutar

```bash
# Compilar y generar el .jar (omite tests, que requieren la BD levantada)
./mvnw clean package -DskipTests

# Ejecutar
java -jar target/demoproject-0.0.1-SNAPSHOT.jar
```

O directamente en modo desarrollo:

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 3.5 Swagger / OpenAPI

- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`

Para probar endpoints protegidos: haz login en `/auth/login`, copia el `token` de la
respuesta y pulsa **Authorize** en Swagger, pegando `Bearer <token>`.

### 3.6 Frontend

El frontend (HTML/CSS/JS) vive dentro de `src/main/resources/static`, así que **Spring Boot
lo sirve automáticamente junto con la API** — no necesitas un servidor aparte. Con el backend
corriendo, entra directo a:

- `http://localhost:8080/` → landing page
- `http://localhost:8080/login.html` → inicio de sesión
- `http://localhost:8080/dashboard.html` → panel (requiere login)

`frontend/js/api.js` usa `BASE_URL: ""` (mismo origen) porque ya viaja empaquetado dentro del
mismo `.jar`. Si en algún momento quieres servir el frontend por separado (por ejemplo con
Live Server en otro puerto durante el desarrollo), solo cambia `API.BASE_URL` a
`"http://localhost:8080"` en ese archivo.

Usuarios de prueba (creados por `data.sql`):

| Username    | Password      | Rol        |
|-------------|---------------|------------|
| superadmin  | superadmin123 | SUPERADMIN |
| admin       | admin123      | ADMIN      |
| jperez      | empleado123   | EMPLEADO   |
| agente      | agente123     | AGENTE (LogiTrack IQ — usado por `mcp-server/`) |

### 3.7 Codificación UTF-8 (tildes/ñ)

Si ves texto como `TecnologÃa` en vez de `Tecnología`, revisa esto en orden:

1. **`application.properties`** ya incluye `spring.sql.init.encoding=UTF-8` (fuerza a que
   `data.sql`/`schema.sql` se lean como UTF-8, sin importar el charset por defecto del SO —
   esto es lo que suele fallar en Windows) y `server.servlet.encoding.force=true` (fuerza
   respuestas HTTP en UTF-8).
2. **La base de datos debe estar creada con encoding UTF8.** Verifica con:
   ```sql
   SHOW server_encoding;
   SELECT datname, pg_encoding_to_char(encoding) FROM pg_database WHERE datname = 'logitrack';
   ```
   Si no da `UTF8`, recréala así:
   ```sql
   DROP DATABASE logitrack;
   CREATE DATABASE logitrack WITH ENCODING 'UTF8' TEMPLATE template0;
   ```
3. Reinicia el backend para que vuelva a correr `schema.sql`/`data.sql` con la codificación
   correcta.

### 3.8 Qué incluye el frontend (más allá del CRUD básico)

Implementa las **consultas avanzadas** del punto 6 del enunciado directamente en la UI, no
solo por Swagger/Thunder Client:

- **Movimientos**: formulario con **desglose dinámico de productos** (agregar/quitar filas de
  producto + cantidad — así se registran "productos y cantidades" en plural, no solo uno).
  Incluye filtro por **rango de fechas** (`BETWEEN`, contra `GET /movimientos?desde=&hasta=`).
- **Inventario**: switch "Solo stock bajo (&lt; 10)" que consulta el endpoint dedicado
  `GET /inventario/stock-bajo`.
- **Auditoría**: filtro por **usuario** y por **tipo de operación** contra el backend
  (`?usuarioId=`, `?tipoOperacion=`), más un botón "Ver" por fila que abre un modal con el
  JSON de `valores_anteriores` / `valores_nuevos` — se ve la trazabilidad completa del cambio.
- **Reportes** (examen, ver sección 6.1): dos exploradores con filtros en vivo — movimientos
  por bodega/producto/tipo/rango de fechas, y auditoría por producto/rango de fechas/campo
  modificado — contra `GET /reportes/movimientos` y `GET /reportes/auditoria`.
- **Usuarios** (módulo nuevo, visible solo para ADMIN/SUPERADMIN): listar, activar/desactivar,
  y crear usuarios respetando la jerarquía de roles (ADMIN solo crea EMPLEADO; SUPERADMIN crea
  ADMIN o EMPLEADO). Usa `GET /usuarios`, `POST /auth/register`, `PATCH /usuarios/{id}/estado`.
- **Control de acceso en la UI**: los botones de crear/editar/eliminar bodegas y productos, y
  los módulos de Auditoría/Usuarios, se ocultan si el usuario logueado es EMPLEADO (el backend
  igual los rechaza con 403; esto solo evita mostrar botones que fallarían).
- **Enlace directo a Swagger** en el pie del menú lateral.
- Los toasts de error muestran el detalle completo de validaciones (`@NotNull`/`@Size`/`@Min`),
  no solo un mensaje genérico.

---

## 4. Capturas de Swagger y pruebas

**Listado de endpoints en Swagger UI** (`/swagger-ui/index.html`):

![Endpoints en Swagger UI](docs/capturas/swagger-endpoints.png)

**Prueba de `POST /auth/login` desde Swagger**, con el `curl` generado, la URL de la petición
y la respuesta `200` con el JWT emitido:

![Prueba de login y token JWT en Swagger](docs/capturas/swagger-login-jwt.png)

Ver también la sección **"Pantallazos examen - pruebas Thunder Client"** más arriba, con las
pruebas específicas del módulo de reportes filtrados.

---

## 5. Autenticación (JWT) y roles

### 4.1 Jerarquía de roles

- **SUPERADMIN**: único rol creado "de fábrica" por `data.sql` (usuario `superadmin`). Hereda
  automáticamente todos los permisos de ADMIN (vía `RoleHierarchy` de Spring Security) y además
  es el único que puede crear cuentas **ADMIN**.
- **ADMIN**: gestiona bodegas, productos, inventario y auditoría; puede crear cuentas **EMPLEADO**
  (no puede crear otros ADMIN ni SUPERADMIN).
- **EMPLEADO**: opera el día a día (movimientos, consultas). No puede gestionar usuarios.

`/auth/register` **ya no es público**: requiere un JWT válido de un ADMIN o SUPERADMIN. La regla
de "quién puede asignar qué rol" se valida en `UsuarioService.registrar()`, no solo a nivel de ruta.

### 4.2 Registro (requiere estar logueado como ADMIN o SUPERADMIN)

```http
POST /auth/register
Authorization: Bearer <token-de-admin-o-superadmin>
Content-Type: application/json

{
  "username": "mgomez",
  "password": "clave123",
  "email": "mgomez@logitrack.com",
  "rol": "EMPLEADO"
}
```

Si un ADMIN intenta crear un usuario con `rol: "ADMIN"` o `"SUPERADMIN"`, el backend responde
`400` con `"Un ADMIN solo puede crear usuarios con rol EMPLEADO"`.

### 4.3 Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJBRE1JTiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzUzMzY0ODAwLCJleHAiOjE3NTM0NTEyMDB9.abc123...",
  "tipo": "Bearer",
  "username": "admin",
  "rol": "ADMIN"
}
```

### 4.4 Usar el token

Todos los endpoints (excepto `/auth/**` y Swagger) requieren el header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJBRE1JTiIs...
```

El token expira en 24 horas (`jwt.expiration-ms`, configurable). El rol viaja embebido en
el token (`claim "rol"`) y también se valida contra la base de datos en cada request.

---

## 6. Endpoints principales

| Método | Ruta                              | Rol requerido     | Descripción |
|--------|-------------------------------------|--------------------|-------------|
| POST   | `/auth/register`                   | ADMIN / SUPERADMIN | Crear usuario (ADMIN solo puede crear EMPLEADO; SUPERADMIN crea ADMIN o EMPLEADO) |
| POST   | `/auth/login`                      | Público            | Login, devuelve JWT |
| GET    | `/bodegas`                          | Autenticado        | Listar bodegas (`?ubicacion=`) |
| POST   | `/bodegas`                          | ADMIN              | Crear bodega |
| PUT    | `/bodegas/{id}`                     | ADMIN              | Actualizar bodega |
| DELETE | `/bodegas/{id}`                     | ADMIN              | Eliminar bodega |
| GET    | `/productos`                        | Autenticado        | Listar productos (`?nombre=`, `?categoria=`) |
| POST   | `/productos`                        | ADMIN              | Crear producto |
| PUT/DELETE | `/productos/{id}`                | ADMIN              | Editar / eliminar producto |
| GET    | `/inventario`                       | Autenticado        | Stock por bodega (`?bodegaId=`) |
| GET    | `/inventario/stock-bajo`            | Autenticado        | Productos con stock < 10 |
| POST   | `/movimientos`                      | Autenticado        | Registrar ENTRADA / SALIDA / TRANSFERENCIA |
| GET    | `/movimientos`                      | Autenticado        | Listar (`?desde=&hasta=`, `?tipo=`, `?usuarioId=`) |
| GET    | `/auditorias`                       | ADMIN              | Consultar auditoría (`?usuarioId=`, `?tipoOperacion=`, `?entidad=`, `?desde=&hasta=`) |
| GET    | `/reportes/resumen`                 | Autenticado        | Reporte JSON: stock por bodega + productos más movidos |
| GET    | `/reportes/stock-por-bodega`        | Autenticado        | Stock total por bodega |
| GET    | `/reportes/productos-mas-movidos`   | Autenticado        | Ranking de productos más movidos |
| GET    | `/reportes/movimientos`             | Autenticado        | **(examen)** Movimientos filtrados (`?bodega=`, `?producto=`, `?tipoMovimiento=`, `?fechaInicio=&fechaFin=`) |
| GET    | `/reportes/auditoria`               | Autenticado        | **(examen)** Auditoría filtrada (`?producto=`, `?fechaInicio=&fechaFin=`, `?campoModificado=`) |

### Ejemplo: registrar una TRANSFERENCIA

```http
POST /movimientos
Authorization: Bearer <token>
Content-Type: application/json

{
  "tipo": "TRANSFERENCIA",
  "bodegaOrigenId": 1,
  "bodegaDestinoId": 2,
  "detalles": [
    { "productoId": 1, "cantidad": 5 },
    { "productoId": 2, "cantidad": 10 }
  ]
}
```

El backend valida stock disponible en la bodega origen, descuenta y suma en `inventario_bodega`
dentro de una única transacción, y registra automáticamente el `INSERT` en `auditoria`.

### 6.1 Módulo de Reportes con filtros (examen)

Implementado en el paquete `testGarciaMaria` (`ReporteController`, `ReporteService`) más dos
métodos nuevos en `MovimientoRepository` y `AuditoriaRepository`.

**`GET /reportes/movimientos`** — filtros opcionales y combinables:

| Parámetro       | Tipo                              | Filtra por |
|-----------------|-----------------------------------|------------|
| `bodega`        | Long                               | Bodega origen **o** destino del movimiento |
| `producto`      | Long                               | Producto incluido en el detalle del movimiento |
| `tipoMovimiento`| `ENTRADA` \| `SALIDA` \| `TRANSFERENCIA` | Tipo de movimiento |
| `fechaInicio` / `fechaFin` | `LocalDateTime` (ISO)   | Rango de fecha del movimiento |

**`GET /reportes/auditoria`** — filtros opcionales y combinables:

| Parámetro        | Tipo                    | Filtra por |
|-------------------|-------------------------|------------|
| `producto`         | Long                    | Auditorías cuya entidad afectada sea un producto con ese id |
| `fechaInicio` / `fechaFin` | `LocalDateTime` (ISO) | Rango de fecha del cambio (`fecha_hora`) |
| `campoModificado`  | String                  | Busca la clave dentro del JSON de `valores_anteriores`/`valores_nuevos` (la entidad `Auditoria` no guarda un campo modificado por separado; guarda el snapshot completo) |

Ejemplos:

```http
GET /reportes/movimientos?tipoMovimiento=SALIDA&fechaInicio=2026-01-01T00:00:00&fechaFin=2026-01-31T23:59:59
GET /reportes/movimientos?bodega=1&producto=2
GET /reportes/auditoria?producto=2
GET /reportes/auditoria?campoModificado=precio
```

**Detalle técnico:** ambos métodos (`buscarConFiltros`) están escritos como `@Query` con SQL
**nativo** de PostgreSQL (no JPQL). Se decidió así porque, en este esquema en particular:

- `tipoMovimiento` compara contra un ENUM propio de Postgres (`tipo_movimiento`), y JPQL no
  logra inferir el tipo de un parámetro que solo aparece en un `? IS NULL` aislado contra una
  columna de tipo enum.
- `valores_anteriores`/`valores_nuevos` son `TEXT` en el `schema.sql`, pero Hibernate los mapea
  internamente como `CLOB` por el `@Lob` de la entidad `Auditoria`, lo que rompía `LOWER()` en
  JPQL. En SQL nativo se tratan directamente como el `TEXT` que son.

Cada filtro sigue el patrón `(CAST(:parametro AS tipo) IS NULL OR columna = CAST(:parametro AS tipo))`:
si el parámetro llega `null` desde el controlador, esa condición se ignora y no filtra nada.

---

## 7. Auditoría automática

Se implementó con **Listeners de JPA** (`@EntityListeners`), tal como pide el enunciado:

- `Auditable`: interfaz que implementan `Usuario`, `Bodega`, `Producto`, `InventarioBodega` y `Movimiento`.
- `AuditoriaEntityListener`: usa los callbacks `@PostLoad`, `@PostPersist`, `@PostUpdate` y `@PostRemove`
  para detectar INSERT/UPDATE/DELETE automáticamente, sin que cada `Service` tenga que invocarlo a mano.
- `SpringContext`: como Hibernate (no Spring) instancia el listener, este helper expone el
  `ApplicationContext` estáticamente para poder inyectar los repositorios necesarios.
- `CurrentUserProvider`: resuelve el usuario autenticado (desde el JWT / `SecurityContextHolder`) que
  queda registrado como responsable de cada cambio.
- `AuditSnapshotUtil`: genera un JSON plano de la entidad (sin colecciones ni password) para
  `valores_anteriores` / `valores_nuevos`.

---

## 8. Manejo de errores

`GlobalExceptionHandler` (`@ControllerAdvice`) centraliza todas las respuestas de error con este formato:

```json
{
  "timestamp": "2026-07-24T10:15:30",
  "status": 400,
  "error": "Error de validacion",
  "message": "Uno o mas campos no son validos",
  "path": "/bodegas",
  "detalles": ["capacidad: La capacidad no puede ser negativa"]
}
```

Casos cubiertos: 400 (validación / regla de negocio), 401 (credenciales inválidas / token
inválido), 403 (rol insuficiente), 404 (recurso no encontrado), 409 (duplicados / conflictos
de integridad), 500 (error genérico).

---

## 9. Diagrama de clases (entidades)

```mermaid
classDiagram
    class Usuario {
      +Long id
      +String username
      +String password
      +String email
      +Rol rol
      +Boolean activo
      +LocalDateTime creadoEn
    }
    class Bodega {
      +Long id
      +String nombre
      +String ubicacion
      +Integer capacidad
      +String encargado
    }
    class Producto {
      +Long id
      +String nombre
      +String categoria
      +BigDecimal precio
    }
    class InventarioBodega {
      +Long id
      +Integer stock
    }
    class Movimiento {
      +Long id
      +LocalDateTime fecha
      +TipoMovimiento tipo
    }
    class MovimientoDetalle {
      +Long id
      +Integer cantidad
    }
    class Auditoria {
      +Long id
      +TipoOperacionAuditoria tipoOperacion
      +LocalDateTime fechaHora
      +String entidadAfectada
      +Long entidadId
      +String valoresAnteriores
      +String valoresNuevos
    }

    InventarioBodega "N" --> "1" Bodega
    InventarioBodega "N" --> "1" Producto
    Movimiento "N" --> "1" Usuario : responsable
    Movimiento "N" --> "0..1" Bodega : origen
    Movimiento "N" --> "0..1" Bodega : destino
    Movimiento "1" --> "N" MovimientoDetalle
    MovimientoDetalle "N" --> "1" Producto
    Auditoria "N" --> "1" Usuario
```

---

## 10. Notas de diseño / decisiones tomadas

- El `schema.sql` enviado se respeta **tal cual** (Hibernate corre con `ddl-auto=none`); los
  ENUM nativos de Postgres (`rol_usuario`, `tipo_movimiento`, `tipo_operacion_auditoria`) se
  mapean con `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` de Hibernate 6.
- El schema no tiene columna `stock` en `producto` (el stock vive por bodega en
  `inventario_bodega`), así que el CRUD de productos expone `stockTotal` calculado
  (suma en todas las bodegas) en las respuestas, en vez de aceptarlo como campo editable.
- Las contraseñas se cifran con **BCrypt** (`PasswordEncoder`), nunca se guardan en texto plano
  ni se devuelven en las respuestas (`UsuarioResponse` las omite).
- Todas las rutas de escritura de `/bodegas`, `/productos` e `/inventario` (POST/PUT/DELETE),
  además de `/auditorias` y `/auth/register`/`/usuarios/**`, están restringidas a `ADMIN` o
  `SUPERADMIN`. El resto de rutas solo exige estar autenticado (cualquier rol).
- La jerarquía de roles (`SecurityConfig.roleHierarchy`) hace que `SUPERADMIN` herede
  automáticamente todo lo que puede hacer `ADMIN`, sin duplicar reglas `hasRole(...)` por todo
  el código. La única cuenta `SUPERADMIN` nace por `data.sql`; no se puede crear otra por API
  (ver `UsuarioService.validarPermisoDeCreacion`).
- El `pom.xml` fuerza explícitamente `<parameters>true</parameters>` en el
  `maven-compiler-plugin`: sin ese flag, Spring no puede leer por reflexión los nombres de los
  `@RequestParam` que no traen `name="..."` explícito, y cualquier endpoint con query params
  responde `400 Bad Request`.

---

## 11. Pendiente / ideas de mejora

- Paginación (`Pageable`) en los listados grandes (`/movimientos`, `/auditorias`).
- Refresh tokens (actualmente el JWT expira y hay que loguear de nuevo).

---

## 12. LogiTrack IQ

Extensión del backend anterior: torre de control de inventario con detección de riesgo, órdenes
de compra, PDF con marca de agua, un servidor MCP, un flujo n8n y un módulo nuevo del dashboard.
Documentación completa del proceso (SDD/TDD, trazabilidad regla→prueba, hashes de commits) en
[`docs/sdd/evidencia-sdd.md`](docs/sdd/evidencia-sdd.md).

### 12.1 Instalación y ejecución

Mismos pasos de la sección 3: `./mvnw spring-boot:run`. `schema.sql`/`data.sql` ya incluyen las
tablas y datos nuevos (proveedores, `producto.proveedor_principal_id`, el rol `AGENTE`, historial
de movimientos para que haya un producto en riesgo real desde el primer arranque — ver los
comentarios en `data.sql`). El servidor queda en `http://localhost:8085` (`server.port=8085`).

> **Importante**: la Postgres remota de Supabase configurada en `application.properties` no
> aceptó autenticación al preparar este proyecto (`FATAL: tenant/user ... not found` — el
> proyecto de Supabase pudo pausarse o rotar credenciales). Verifica tus credenciales antes de
> grabar el video, o apunta `spring.datasource.*` a una Postgres local.

### 12.2 Endpoints nuevos

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/kpis` | Autenticado | Los 4 indicadores + movimientos de ayer + `calculadoEn` |
| GET | `/productos/{id}/stock` | Autenticado | Stock total y desglose por bodega |
| GET | `/productos/riesgo` | Autenticado | Productos en riesgo (proveedor, consumo, reorden, cobertura) |
| GET | `/bodegas/criticas` | Autenticado | Bodegas con ocupación ≥ 90% |
| GET | `/proveedores` | Autenticado | Proveedores precargados |
| GET | `/ordenes` | Autenticado | Listar órdenes (`?estado=`) |
| POST | `/ordenes` | AGENTE / ADMIN | Crear orden en `BORRADOR` (total calculado en servidor) |
| GET | `/ordenes/{id}` | Autenticado | Detalle de una orden |
| POST / GET | `/ordenes/{id}/pdf` | ADMIN | Generar / descargar el PDF (marca de agua si `BORRADOR`) |
| PATCH | `/ordenes/{id}/estado` | ADMIN | Cambiar estado (`AGENTE` → `403`) |
| POST | `/panel/resumen` | AGENTE / ADMIN | Publicar/reemplazar el resumen del día |
| GET | `/panel/resumen` | Autenticado | Último resumen válido (`404` si no existe) |

Swagger UI: `http://localhost:8085/swagger-ui.html` (todos los endpoints nuevos están
documentados con `@Tag`/`@Operation`, en las mismas etiquetas "LogiTrack IQ - ...").

### 12.3 Dashboard — módulo "Torre de control"

Con el backend corriendo, entra a `http://localhost:8085/dashboard.html` (login con `admin` /
`admin123`) y abre **Torre de control** en el menú lateral: los 4 indicadores, ocupación por
bodega, movimientos de ayer, el último resumen del panel, la tabla de productos en riesgo y las
órdenes en `BORRADOR` (generar/ver PDF, aprobar — visible solo si el usuario logueado es
`ADMIN`/`SUPERADMIN`). El JWT ahora se guarda en `sessionStorage` (no `localStorage`). Detalle en
[`frontend/README.md`](frontend/README.md).

### 12.4 Servidor MCP

```bash
cd mcp-server
npm install
npm start
```

6 herramientas (`consultar_stock_producto`, `consultar_bodegas_criticas`,
`consultar_productos_en_riesgo`, `consultar_kpis`, `crear_orden_borrador`, `publicar_resumen`) —
ninguna aprueba/cancela/recibe órdenes. Detalle, configuración y evidencia real de
entrada/salida de cada herramienta en [`mcp-server/README.md`](mcp-server/README.md) y
[`mcp-server/EVIDENCIA.md`](mcp-server/EVIDENCIA.md).

### 12.5 Flujo n8n

`n8n/resumen-diario-inventario.json` — Schedule Trigger (6:00 a.m. `America/Bogota`) + AI Agent +
las 6 herramientas MCP. **No se pudo ejecutar en este entorno** (no hay una instancia de n8n ni
credencial de modelo de lenguaje disponibles aquí). Instrucciones completas para importarlo,
conectar el servidor MCP y tu propia credencial de LLM en
[`n8n/README.md`](n8n/README.md).

### 12.6 Skill

[`skills/operacion-logitrack/SKILL.md`](skills/operacion-logitrack/SKILL.md) — reglas operativas
del flujo automatizado (consultar riesgo primero, máximo una orden por ejecución, nunca aprobar,
contrato exacto del resumen, informar errores).

### 12.7 Evidencia y flujo completo

- [`docs/evidencia-flujo-completo.md`](docs/evidencia-flujo-completo.md): el flujo de negocio
  completo (producto en riesgo → orden BORRADOR vía MCP → aprobación → recepción → movimiento
  ENTRADA → dashboard actualizado) ejecutado de punta a punta contra un backend real.
- [`docs/capturas/orden-1-borrador-marca-de-agua.pdf`](docs/capturas/orden-1-borrador-marca-de-agua.pdf):
  PDF real generado por el sistema, con la marca de agua diagonal BORRADOR.
- [`docs/diagrama-flujo.md`](docs/diagrama-flujo.md): diagrama n8n → MCP → API → PostgreSQL → dashboard.
- [`docs/sdd/evidencia-sdd.md`](docs/sdd/evidencia-sdd.md): trazabilidad regla→prueba, hashes de
  los 3 commits (`docs:`/`test:`/`feat:`) y evidencia roja→verde de `mvn test`.

### 12.8 Qué falta (tarea del estudiante, no reproducible en este entorno)

1. **Verificar/actualizar las credenciales de Supabase** en `application.properties` (o usar una
   Postgres propia) antes de grabar.
2. **Ejecutar el flujo n8n de verdad** (importarlo, conectar tu credencial de LLM, correrlo
   manualmente) y capturar una ejecución exitosa y una con error controlado.
3. **Grabar el video de 4-6 minutos** mostrando: datos iniciales + ejecución manual de n8n →
   consulta de riesgo y orden `BORRADOR` → aprobación por ADMIN → recepción, movimiento ENTRADA y
   dashboard actualizado. Sin mostrar ni explicar código.