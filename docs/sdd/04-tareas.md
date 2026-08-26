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
- [ ] Enums nuevos: `EstadoOrden`, `EstadoCobertura`, `Severidad`,
      `TipoAccionResumen`; `Rol.AGENTE`.
- [ ] Entidades nuevas: `Proveedor`, `OrdenCompra`, `ResumenPanel`;
      `Producto.proveedorPrincipal`.
- [ ] Repositorios: `ProveedorRepository`, `OrdenCompraRepository`,
      `ResumenPanelRepository`.
- [ ] `schema.sql`/`data.sql`: tablas nuevas, columna
      `producto.proveedor_principal_id`, valor `AGENTE` en
      `rol_usuario`, proveedores y usuario `agente` de prueba.
- [ ] `src/test/resources/application.properties` (H2 en memoria).
- [ ] Tests (rojo esperado) de las 8 reglas obligatorias + 1 prueba de
      integración de `PATCH /ordenes/{id}/estado` o `POST /panel/resumen`.
- [ ] Ejecutar `mvn test`, guardar salida real en
      `docs/sdd/evidencia/01-rojo.txt`.

## Fase 3 — `feat: implement LogiTrack IQ rules` (27-ago)
- [ ] `RiesgoService`/`KpiService`: ocupación, quiebre, riesgo, consumo,
      punto de reorden, cobertura, movimientos de ayer, bodegas críticas.
- [ ] `OrdenCompraService`: crear borrador (total en servidor), máquina
      de estados, recepción transaccional (orden + movimiento ENTRADA).
- [ ] `PanelResumenService`: validación de contrato + reemplazo por
      fecha + auditoría.
- [ ] `PdfService` (PDFBox): generar/guardar/leer PDF, marca de agua
      BORRADOR, borrado del PDF al cambiar estado.
- [ ] Controladores: `KpiController`, `ProveedorController`,
      `OrdenCompraController`, `PanelResumenController`; extensión de
      `ProductoController` y `BodegaController`.
- [ ] `SecurityConfig`: reglas AGENTE vs ADMIN por endpoint.
- [ ] Swagger (`@Tag`/`@Operation`) en los controladores nuevos.
- [ ] `mvn test` en verde; salida real en
      `docs/sdd/evidencia/02-verde.txt`.

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
