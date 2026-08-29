# Evidencia SDD/TDD — LogiTrack IQ

## Documentos

- [01-propuesta.md](01-propuesta.md) — problema, objetivo, alcance y fuera de alcance.
- [02-especificacion.md](02-especificacion.md) — reglas, contratos y ejemplo de resumen.
- [03-diseno.md](03-diseno.md) — entidades, decisiones y diagrama del flujo.
- [04-tareas.md](04-tareas.md) — checklist por fase/commit.

## Trazabilidad: regla → prueba

| # | Regla obligatoria (ver `../enunciado-logitrack-iq.md`) | Prueba(s) |
|---|---|---|
| 1 | Consumo 0 → cobertura `null` y estado `SIN_CONSUMO` | `RiesgoServiceTest.consumoCero_coberturaEsNullYEstadoEsSinConsumo` |
| 2 | Stock igual al punto de reorden → no está en riesgo | `RiesgoServiceTest.stockIgualAlPuntoDeReorden_noEstaEnRiesgo` |
| 3 | Cantidad 0 o negativa → `400` | `LogiTrackIqIntegrationTest.crearOrden_conCantidadInvalida_devuelve400` |
| 4 | Orden cancelada → no se puede aprobar (`400`) | `OrdenCompraServiceTest.ordenCancelada_noSePuedeAprobar` |
| 5 | Orden aprobada → recibida genera una entrada | `OrdenCompraServiceTest.ordenAprobada_alRecibirse_generaMovimientoEntrada` |
| 6 | AGENTE intenta aprobar → `403` | `LogiTrackIqIntegrationTest.agenteIntentaAprobarOrden_devuelve403` |
| 7 | Resumen con severidad inválida o id inexistente → `400`, se conserva el anterior | `LogiTrackIqIntegrationTest.publicarResumen_conSeveridadInvalida_devuelve400` + `PanelResumenServiceTest.idInexistenteEnAlerta_seRechazaYConservaElResumenAnterior` |
| 8 | PDF de orden BORRADOR: se guarda con marca BORRADOR; al cambiar estado ya no está disponible | `PdfServiceTest.pdfDeOrdenBorrador_seGuardaYContieneMarcaDeAguaBorrador` + `PdfServiceTest.alCambiarEstado_elPdfGuardadoYaNoQuedaDisponible` |
| — | Prueba de integración (HTTP + JWT + `SecurityFilterChain` reales) para `PATCH /ordenes/{id}/estado` y `POST /panel/resumen` | Toda la clase `LogiTrackIqIntegrationTest` (4 pruebas) |

Código de las pruebas:
[`src/test/java/.../logitrackiq/`](../../src/test/java/com/project/springboot/demoproject/logitrackiq/).

## Hashes de los 3 commits (en orden)

| Orden | Commit | Hash |
|---|---|---|
| 1 | `docs: define LogiTrack IQ scope` | `841d1d464920e2038cd5f875e539d43d5a36d3b0` |
| 2 | `test: define reorder and order-state rules` | `90294d7a3ec32c1bf97ca57948d7c79b779815aa` |
| 3 | `feat: implement LogiTrack IQ rules` | `5a43565af0ec4d2559ae8bbd591e66077bdefa1c` |

Verificable con `git log --oneline` en el repositorio publicado.

## Evidencia roja → verde

- **Rojo** (commit 2, pruebas escritas antes de implementar la lógica):
  [`evidencia/01-rojo-resumen.md`](evidencia/01-rojo-resumen.md) —
  11 pruebas, 8 fallan exactamente en la lógica de negocio pendiente
  (`UnsupportedOperationException` / setup dependiente de ella), 3 pasan
  porque solo dependían de validación/seguridad ya completa.
- **Verde** (commit 3, después de implementar):
  [`evidencia/02-verde-resumen.md`](evidencia/02-verde-resumen.md) — las
  mismas 11 pruebas, sin modificarlas, ahora en verde
  (`Tests run: 11, Failures: 0, Errors: 0`).
- Evidencia adicional de ejecución real end-to-end (backend real, no
  simulado) en [`../evidencia-flujo-completo.md`](../evidencia-flujo-completo.md)
  y de las 6 herramientas MCP en
  [`../../mcp-server/EVIDENCIA.md`](../../mcp-server/EVIDENCIA.md).

## Reflexión (≤150 palabras)

El diseño original (`03-diseno.md`, decisión 8) planeaba probar contra H2
en memoria. Al implementar se descubrió que `Usuario.rol`,
`Movimiento.tipo` y `Auditoria.tipoOperacion` (entidades del reto
anterior) fijan `columnDefinition` con el nombre literal de un enum de
PostgreSQL, y el binder de Hibernate para `NAMED_ENUM` no es portable a
H2 (`ClassCastException` comprobado). Se cambió a un PostgreSQL real
embebido (`io.zonky.test:embedded-postgres`, sin Docker), lo que además
permitió reutilizar el `schema.sql` de producción tal cual, sin mantener
un esquema paralelo. También la base remota de Supabase configurada en
`application.properties` dejó de aceptar el usuario del pooler al
generar la evidencia del MCP; se documentó y se usó una Postgres local
real equivalente. Ninguna regla de negocio cambió respecto a
`02-especificacion.md`: solo la infraestructura de pruebas/demo.
