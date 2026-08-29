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
- [x] `mcp-server/` (6 herramientas, Node.js + `@modelcontextprotocol/sdk`)
      + `README.md` + `EVIDENCIA.md` con request/response REAL (backend
      real levantado localmente, incluido un error controlado) y
      `evidencia-runner.mjs` (cliente MCP real que la generó).
- [x] `skills/operacion-logitrack/SKILL.md` (ya estaba completo desde la
      fase 1; verificado consistente con las 6 herramientas del MCP).
- [x] `n8n/resumen-diario-inventario.json` + `n8n/README.md` (no se pudo
      ejecutar en este entorno: sin instancia de n8n ni credencial de
      LLM; documentado qué falta y por qué).
- [x] Dashboard: módulo "Torre de control" (`pages/torre-control.html` +
      `js/torre-control.js`: KPIs, ocupación, riesgo, órdenes BORRADOR,
      generar/ver PDF, botón Aprobar solo ADMIN); `sessionStorage` en
      `api.js` (antes `localStorage`) + `peticionApiBinaria` para el PDF.
- [x] `docs/sdd/evidencia-sdd.md` (enlaces, tabla regla→prueba, hashes,
      evidencia roja/verde, reflexión ≤150 palabras).
- [x] `docs/diagrama-flujo.md` y `docs/evidencia-flujo-completo.md`
      (flujo completo verificado end-to-end contra un backend real:
      producto en riesgo → orden BORRADOR → aprobación → recepción →
      movimiento ENTRADA → dashboard actualizado, con stock y KPIs antes/
      después reales).
- [x] `README.md` actualizado (sección 12: instalación, endpoints
      nuevos, dashboard, MCP, n8n, skill, evidencia, y checklist de lo
      que falta hacer el estudiante: video y ejecución real de n8n).
