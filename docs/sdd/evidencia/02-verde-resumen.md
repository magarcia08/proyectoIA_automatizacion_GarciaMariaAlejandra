# Evidencia verde — `mvn test` después de implementar

Comando: `./mvnw test -Dtest='com.project.springboot.demoproject.logitrackiq.**'`
Log completo: [02-verde-mvn-test-full.log](02-verde-mvn-test-full.log)

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Las mismas 11 pruebas escritas en la fase anterior (ver
[01-rojo-resumen.md](01-rojo-resumen.md)) ahora pasan, sin haber
modificado ningún test: solo se implementó la lógica de negocio en
`RiesgoService`, `KpiService`, `OrdenCompraService`, `PanelResumenService`
y `PdfService`.

| Prueba | Resultado |
|---|---|
| RiesgoServiceTest.consumoCero_coberturaEsNullYEstadoEsSinConsumo | ✅ |
| RiesgoServiceTest.stockIgualAlPuntoDeReorden_noEstaEnRiesgo | ✅ |
| OrdenCompraServiceTest.ordenCancelada_noSePuedeAprobar | ✅ |
| OrdenCompraServiceTest.ordenAprobada_alRecibirse_generaMovimientoEntrada | ✅ |
| PdfServiceTest.pdfDeOrdenBorrador_seGuardaYContieneMarcaDeAguaBorrador | ✅ |
| PdfServiceTest.alCambiarEstado_elPdfGuardadoYaNoQuedaDisponible | ✅ |
| PanelResumenServiceTest.idInexistenteEnAlerta_seRechazaYConservaElResumenAnterior | ✅ |
| LogiTrackIqIntegrationTest.agenteIntentaAprobarOrden_devuelve403 | ✅ |
| LogiTrackIqIntegrationTest.crearOrden_conCantidadInvalida_devuelve400 | ✅ |
| LogiTrackIqIntegrationTest.publicarResumen_conSeveridadInvalida_devuelve400 | ✅ |
| LogiTrackIqIntegrationTest.kpis_requiereAutenticacion | ✅ |

## Ajustes hechos durante la implementación (no en las pruebas)

Dos correcciones de infraestructura, ninguna cambia el comportamiento que
las pruebas verifican:

1. `OrdenCompra.pdfDocumento` se guardaba con `@Lob` sobre `byte[]`, que en
   PostgreSQL usa Large Objects (columna `oid`) en vez de `bytea` (el tipo
   real de la columna en `schema.sql`). Se quitó `@Lob`: un `byte[]` plano
   se enlaza directamente como `bytea`.
2. `PDPageContentStream.setNonStrokingColor(int,int,int)` en PDFBox 3.x
   espera componentes en 0..1, no 0..255. Se cambió a
   `setNonStrokingColor(java.awt.Color)`.

Además se validó (fuera de la suite automatizada, ver terminal) que
`src/main/resources/data.sql` — con los datos nuevos de LogiTrack IQ
(proveedores, `proveedor_principal_id`, usuario `agente`, historial de
`SALIDA` para demostrar un producto en riesgo) — carga sin errores contra
un PostgreSQL real.
