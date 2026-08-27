# Evidencia roja — `mvn test` antes de implementar

Comando: `./mvnw test -Dtest='com.project.springboot.demoproject.logitrackiq.**'`
Log completo (con stack traces reales): [01-rojo-mvn-test-full.log](01-rojo-mvn-test-full.log)

```
Tests run: 11, Failures: 1, Errors: 7, Skipped: 0
BUILD FAILURE
```

| Prueba | Resultado | Motivo |
|---|---|---|
| RiesgoServiceTest.consumoCero_coberturaEsNullYEstadoEsSinConsumo | ERROR | `UnsupportedOperationException` en `RiesgoService.calcularCobertura` (sin implementar) |
| RiesgoServiceTest.stockIgualAlPuntoDeReorden_noEstaEnRiesgo | ERROR | idem |
| OrdenCompraServiceTest.ordenCancelada_noSePuedeAprobar | ERROR | `UnsupportedOperationException` en `OrdenCompraService.cambiarEstado` |
| OrdenCompraServiceTest.ordenAprobada_alRecibirse_generaMovimientoEntrada | ERROR | idem |
| PdfServiceTest.pdfDeOrdenBorrador_seGuardaYContieneMarcaDeAguaBorrador | ERROR | `UnsupportedOperationException` en `PdfService.generarYGuardar` |
| PdfServiceTest.alCambiarEstado_elPdfGuardadoYaNoQuedaDisponible | ERROR | idem (via `crearOrdenBorrador`) |
| PanelResumenServiceTest.idInexistenteEnAlerta_seRechazaYConservaElResumenAnterior | ERROR | `UnsupportedOperationException` en `PanelResumenService.publicar` |
| LogiTrackIqIntegrationTest.agenteIntentaAprobarOrden_devuelve403 | FAILURE (esperaba 201, obtuvo 500) | La creacion de la orden (paso previo del test) depende de `OrdenCompraService.crear`, sin implementar |
| LogiTrackIqIntegrationTest.crearOrden_conCantidadInvalida_devuelve400 | **PASA** | Bean Validation (`@Min(1)`) ya rechaza `cantidad<=0` sin necesitar logica de negocio |
| LogiTrackIqIntegrationTest.publicarResumen_conSeveridadInvalida_devuelve400 | **PASA** | Jackson ya rechaza un valor de enum invalido (`severidad: "URGENTE"`) con 400 |
| LogiTrackIqIntegrationTest.kpis_requiereAutenticacion | **PASA** | La seguridad (capa de infraestructura) ya esta completa |

Las 3 pruebas que ya pasan solo ejercitan validacion/seguridad (capas ya
completas en esta fase); las 8 restantes fallan exactamente donde se
espera: en la logica de negocio que se implementa en la siguiente fase
(`feat: implement LogiTrack IQ rules`). Ver evidencia verde en
[02-verde-resumen.md](02-verde-resumen.md) (fase siguiente).

## Nota sobre la base de datos de pruebas

Las pruebas usan un PostgreSQL real embebido (`io.zonky.test:embedded-postgres`,
arrancado por `AbstractLogiTrackIqTest`), no H2: varias entidades ya
existentes del proyecto anterior (`Usuario.rol`, `Movimiento.tipo`,
`Auditoria.tipoOperacion`) fijan `columnDefinition` con el nombre literal
de un tipo ENUM de PostgreSQL, y el binder JDBC de Hibernate para
`@JdbcTypeCode(NAMED_ENUM)` no es portable a H2 (se probo y produce un
`ClassCastException` al enlazar el valor). En vez de modificar esas
entidades ya construidas, se opto por una base Postgres real sin Docker.
Ver decision completa en [`../03-diseno.md`](../03-diseno.md).
