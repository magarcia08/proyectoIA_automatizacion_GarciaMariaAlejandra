# Evidencia — servidor MCP de LogiTrack IQ

Ejecución real de las 6 herramientas, como cliente MCP (protocolo stdio,
mismo `@modelcontextprotocol/sdk`, ver `evidencia-runner.mjs`) contra un
backend real de LogiTrack corriendo en `http://localhost:8085`.

> **Nota sobre la base de datos**: la Postgres remota configurada en
> `src/main/resources/application.properties` (Supabase) no aceptó
> autenticación al momento de generar esta evidencia (`FATAL: tenant/user
> ... not found`, posiblemente el proyecto de Supabase está pausado o las
> credenciales cambiaron). Se usó en su lugar un PostgreSQL real local
> (mismo `schema.sql`/`data.sql` de producción, mismo motor, misma
> versión), así que el comportamiento es idéntico. El estudiante debe
> verificar que sus credenciales de Supabase sigan vigentes antes del
> video, o usar una Postgres local para la demo.

Cada llamada usó el usuario `agente` (rol `AGENTE`, precargado en
`data.sql`) autenticado automáticamente por el servidor MCP.

## 1. `consultar_kpis()`

Entrada: `{}`

Salida:
```json
{
  "calculadoEn": "2026-08-30T21:39:21.6102299-05:00",
  "ocupacionPorBodega": [
    { "bodegaId": 1, "nombre": "Bodega Central", "porcentaje": 3.06 },
    { "bodegaId": 2, "nombre": "Bodega Norte", "porcentaje": 10.5 },
    { "bodegaId": 3, "nombre": "Bodega Sur", "porcentaje": 1.8 },
    { "bodegaId": 4, "nombre": "Bodega Este", "porcentaje": 96 }
  ],
  "productosEnQuiebre": 1,
  "productosEnRiesgo": 1,
  "ordenesPorAprobar": { "cantidad": 0, "montoTotal": 0 },
  "movimientosAyer": { "entrada": 2, "salida": 3, "transferencia": 1 }
}
```

(`movimientosAyer` coincide exactamente con el ejemplo del enunciado:
entrada 2, salida 3, transferencia 1 — ver el seed dedicado en
`data.sql`.)

## 2. `consultar_bodegas_criticas()`

Entrada: `{}`

Salida:
```json
[
  { "bodegaId": 4, "nombre": "Bodega Este", "porcentaje": 96 }
]
```

## 3. `consultar_productos_en_riesgo()`

Entrada: `{}`

Salida:
```json
[
  {
    "productoId": 3,
    "nombreProducto": "Silla ergonómica oficina",
    "proveedorId": 2,
    "stockTotal": 8,
    "consumoDiarioPromedio": 1,
    "puntoReorden": 22.5,
    "diasCobertura": 8,
    "estadoCobertura": "EN_RIESGO",
    "bodegaDestinoId": 1
  }
]
```

## 4. `consultar_stock_producto(productoId: 3)`

Entrada: `{ "productoId": 3 }`

Salida:
```json
{
  "productoId": 3,
  "nombreProducto": "Silla ergonómica oficina",
  "stockTotal": 8,
  "porBodega": [
    { "bodegaId": 1, "nombreBodega": "Bodega Central", "stock": 8 }
  ]
}
```

## 5. `crear_orden_borrador(...)`

Cantidad calculada con la misma fórmula que usa el flujo n8n:
`ceil(max(1, puntoReorden×2 - stockTotal))` = `ceil(max(1, 22.5×2 - 8))` = `37`.

Entrada:
```json
{
  "productoId": 3,
  "proveedorId": 2,
  "bodegaDestinoId": 1,
  "cantidad": 37,
  "precioUnitario": 450000
}
```

Salida:
```json
{
  "id": 1,
  "productoId": 3,
  "nombreProducto": "Silla ergonómica oficina",
  "proveedorId": 2,
  "nombreProveedor": "Muebles y Oficina Ltda",
  "bodegaDestinoId": 1,
  "nombreBodegaDestino": "Bodega Central",
  "cantidad": 37,
  "precioUnitario": 450000,
  "total": 16650000,
  "fechaCreacion": "2026-08-30T21:39:21.8304477",
  "estado": "BORRADOR",
  "creadoPor": "agente",
  "pdfDisponible": false,
  "pdfGeneradoEn": null
}
```

A partir de esta orden se generó el PDF real con marca de agua diagonal
BORRADOR (`POST /ordenes/1/pdf`, como ADMIN): ver
[`../docs/capturas/orden-1-borrador-marca-de-agua.pdf`](../docs/capturas/orden-1-borrador-marca-de-agua.pdf).

## 6. `publicar_resumen(resumen)`

Entrada:
```json
{
  "resumen": {
    "fecha": "2026-08-30",
    "narrativa": "Se detecto el producto Silla ergonómica oficina por debajo de su punto de reorden y se creo la orden 1 en BORRADOR para revision del ADMIN.",
    "alertas": [
      {
        "severidad": "ALTA",
        "titulo": "Producto en riesgo",
        "detalle": "Silla ergonómica oficina tiene stock 8, por debajo del punto de reorden 22.5.",
        "productoId": 3
      }
    ],
    "accionesSugeridas": [
      {
        "tipo": "REVISAR_ORDEN",
        "descripcion": "Revisar la orden 1 antes de aprobarla.",
        "ordenId": 1
      }
    ]
  }
}
```

Salida:
```json
{
  "fecha": "2026-08-30",
  "narrativa": "Se detecto el producto Silla ergonómica oficina por debajo de su punto de reorden y se creo la orden 1 en BORRADOR para revision del ADMIN.",
  "alertas": [
    { "severidad": "ALTA", "titulo": "Producto en riesgo",
      "detalle": "Silla ergonómica oficina tiene stock 8, por debajo del punto de reorden 22.5.",
      "productoId": 3, "ordenId": null, "bodegaId": null }
  ],
  "accionesSugeridas": [
    { "tipo": "REVISAR_ORDEN", "descripcion": "Revisar la orden 1 antes de aprobarla.",
      "ordenId": 1, "productoId": null, "bodegaId": null }
  ],
  "autor": "agente",
  "publicadoEn": "2026-08-30T21:39:22.0376924"
}
```

## 7. Error controlado (sin crear una orden indebida)

`crear_orden_borrador` con un `productoId` que no existe:

Entrada:
```json
{ "productoId": 999999, "proveedorId": 999999, "bodegaDestinoId": 999999, "cantidad": 1, "precioUnitario": 1 }
```

Salida (herramienta MCP marcada `isError: true`, ninguna orden creada):
```json
{
  "content": [
    { "type": "text", "text": "Error al llamar la API de LogiTrack: Producto con id 999999 no fue encontrado(a)" }
  ],
  "isError": true
}
```

Este es el mismo tipo de error que produce el nodo AI Agent de n8n
cuando una herramienta falla: se reporta el error (ver
`skills/operacion-logitrack/SKILL.md`, sección "límites duros") en vez de
reintentar creando datos duplicados o inventar un resultado exitoso.
