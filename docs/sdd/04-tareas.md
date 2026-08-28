# 04 — Tareas

Checklist de trabajo, agrupado por commit. Se marca `[x]` al cerrar cada
fase (evidencia real: código + `mvn test` + artefactos).

## Fase 1 — `docs: define LogiTrack IQ scope` (25-ago)
- [x] Guardar el enunciado completo en `docs/enunciado-logitrack-iq.md`.
- [x] `docs/sdd/01-propuesta.md`
- [x] `docs/sdd/02-especificacion.md`
- [x] `docs/sdd/03-diseno.md`
- [x] `docs/sdd/04-tareas.md` (este archivo)
- [x] Borrador de `skills/operacion-logitrack/SKILL.md`

## Fase 2 — `test: define reorder and order-state rules` (26-ago)
- [x] Enums nuevos: `EstadoOrden`, `EstadoCobertura`, `Severidad`,
      `TipoAccionResumen`; `Rol.AGENTE`.
- [x] Entidades nuevas: `Proveedor`, `OrdenCompra`, `ResumenPanel`;
      `Producto.proveedorPrincipal`.
- [x] Repositorios: `ProveedorRepository`, `OrdenCompraRepository`,
      `ResumenPanelRepository` (+ metodos nuevos en `MovimientoRepository`
      y `MovimientoDetalleRepository`).
- [x] `schema.sql`/`data.sql`: tablas nuevas, columna
      `producto.proveedor_principal_id`, valor `AGENTE` en
      `rol_usuario`, proveedores, usuario `agente` de prueba y datos de
      consumo/ocupacion para la demo (ver comentarios en `data.sql`).
- [x] Controladores + DTOs + reglas de seguridad (`@PreAuthorize`) para
      los 13 endpoints nuevos, cableados a los servicios (ver mas abajo).
- [x] `src/test/resources/application.properties` (Postgres real
      embebido, ver decision 8 en `03-diseno.md`).
- [x] Tests (rojo esperado) de las 8 reglas obligatorias + prueba de
      integracion de `PATCH /ordenes/{id}/estado` y `POST /panel/resumen`.
- [x] Ejecutar `mvn test`, guardar salida real en
      `docs/sdd/evidencia/01-rojo-mvn-test-full.log` (resumen en
      `01-rojo-resumen.md`): 11 pruebas, 3 pasan (validacion/seguridad ya
      completas), 8 fallan exactamente en la logica de negocio pendiente.

## Fase 3 — `feat: implement LogiTrack IQ rules` (27-ago)
- [x] `RiesgoService`/`KpiService`: ocupación, quiebre, riesgo, consumo,
      punto de reorden, cobertura, movimientos de ayer, bodegas críticas.
- [x] `OrdenCompraService`: crear borrador (total en servidor), máquina
      de estados, recepción transaccional (orden + movimiento ENTRADA,
      reutilizando `MovimientoService`).
- [x] `PanelResumenService`: validación de contrato + reemplazo por
      fecha + auditoría (vía `Auditable`/`AuditoriaEntityListener`).
- [x] `PdfService` (PDFBox 3): generar/guardar/leer PDF, marca de agua
      diagonal BORRADOR, borrado del PDF al cambiar estado.
- [x] Controladores: `KpiController`, `ProveedorController`,
      `OrdenCompraController`, `PanelResumenController`; extensión de
      `ProductoController` (`/{id}/stock`, `/riesgo`) y `BodegaController`
      (`/criticas`).
- [x] Reglas AGENTE vs ADMIN por endpoint (`@PreAuthorize` en cada
      controlador; no fue necesario tocar `SecurityConfig`, que ya deja
      pasar cualquier ruta autenticada por el catch-all existente).
- [x] Swagger (`@Tag`/`@Operation`) en los controladores nuevos.
- [x] `mvn test` en verde (11/11); salida real en
      `docs/sdd/evidencia/02-verde-mvn-test-full.log` (resumen en
      `02-verde-resumen.md`).

## Fase 4 — commit final (28-ago)
- [ ] `mcp-server/` (6 herramientas) + `README.md` + `EVIDENCIA.md`.
- [ ] `skills/operacion-logitrack/SKILL.md` final.
- [ ] `n8n/resumen-diario-inventario.json` + `n8n/README.md`.
- [ ] Dashboard: módulo "Torre de control" (KPIs, riesgo, órdenes,
      generar/ver PDF, botón Aprobar solo ADMIN); `sessionStorage` en
      `api.js`.
- [ ] `docs/sdd/evidencia-sdd.md` (enlaces, tabla regla→prueba, hashes,
      evidencia roja/verde, reflexión).
- [ ] `README.md` actualizado (instalación, usuarios de prueba, rutas,
      pendientes del usuario: video y ejecución de n8n).
