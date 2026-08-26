# 02 — Especificación

Reglas y contratos exactos que implementa el código. Fuente: el enunciado
completo en [`../enunciado-logitrack-iq.md`](../enunciado-logitrack-iq.md).

## Zona horaria

Todos los cálculos de "hoy", "ayer" y "últimos 30 días" usan
`ZoneId.of("America/Bogota")`. `fecha` en el resumen del panel también usa
esa zona.

## Cálculo de stock

- El stock de un producto en una bodega es la suma/resta de
  `movimiento_detalle.cantidad` según el tipo de movimiento, **no** un
  campo estático. `Producto` no tiene columna `stock`; `InventarioBodega`
  sigue siendo la proyección materializada que ya mantiene
  `MovimientoService` (ENTRADA suma en destino, SALIDA resta en origen,
  TRANSFERENCIA resta en origen y suma en destino). Los endpoints nuevos
  leen esa proyección, ya coherente con los movimientos.
- Stock total de un producto = suma de `InventarioBodega.stock` en todas
  las bodegas donde tiene registro.
- No se permite dejar una bodega con stock negativo (regla ya aplicada en
  `MovimientoService.restarStock`, reutilizada sin cambios).

## Indicadores (`GET /kpis`)

| Campo | Regla |
|---|---|
| `ocupacionPorBodega[].porcentaje` | `(Σ stock de la bodega / capacidad) × 100` |
| `productosEnQuiebre` | Nº de productos con stock total `= 0` |
| `productosEnRiesgo` | Nº de productos con `proveedorPrincipal != null` y stock total `<` punto de reorden |
| `ordenesPorAprobar.cantidad` / `.montoTotal` | Nº y Σ `total` de órdenes en `BORRADOR` |
| `movimientosAyer.entrada/salida/transferencia` | Conteo de `Movimiento` (no de detalles) por tipo, cuya `fecha` cae en el día calendario anterior en America/Bogota |
| `calculadoEn` | Instante del cálculo, con offset de America/Bogota |

Contrato de respuesta (igual al del enunciado):

```json
{
  "calculadoEn": "2026-08-24T06:00:00-05:00",
  "ocupacionPorBodega": [{ "bodegaId": 1, "nombre": "Bogota", "porcentaje": 92.5 }],
  "productosEnQuiebre": 1,
  "productosEnRiesgo": 2,
  "ordenesPorAprobar": { "cantidad": 1, "montoTotal": 45000.0 },
  "movimientosAyer": { "entrada": 2, "salida": 3, "transferencia": 1 }
}
```

Una bodega es **crítica** cuando `porcentaje >= 90`. `GET /bodegas/criticas`
filtra `ocupacionPorBodega` con esa condición.

## Riesgo de producto (`GET /productos/riesgo`)

Para cada producto con `proveedorPrincipal != null`:

- `consumoDiarioPromedio` = `Σ cantidad` de `MovimientoDetalle` de ese
  producto en movimientos `SALIDA` cuya fecha esté entre
  `hoy-29 días 00:00` y `hoy 23:59:59` (America/Bogota, 30 días
  calendario incluyendo hoy), dividido entre `30`.
- `puntoReorden` = `consumoDiarioPromedio × proveedor.diasEntrega × 1.5`.
- `diasCobertura` = `stockTotal / consumoDiarioPromedio`.
  - Si `consumoDiarioPromedio == 0` → `diasCobertura = null` y
    `estadoCobertura = SIN_CONSUMO`.
  - En cualquier otro caso mostrado en `/productos/riesgo`,
    `estadoCobertura = EN_RIESGO` (la lista solo contiene productos que sí
    están en riesgo).
- Un producto aparece en la lista solo si `stockTotal < puntoReorden`
  (estrictamente menor; igual no cuenta como riesgo).
- `bodegaDestinoId` sugerida = bodega con **menor** stock de ese producto;
  en empate, la de menor `id`.

Campos de cada elemento: `productoId`, `nombreProducto`, `proveedorId`,
`stockTotal`, `consumoDiarioPromedio`, `puntoReorden`, `diasCobertura`,
`estadoCobertura`, `bodegaDestinoId`.

> Nota de diseño: el cálculo de cobertura/consumo/reorden vive en un
> método de servicio reutilizable (no solo dentro del endpoint), para
> poder probarlo por unidad incluso con productos que hoy no aparecerían
> en `/productos/riesgo` (p. ej. consumo 0 → no está en riesgo, pero debe
> poder calcularse igual y devolver `SIN_CONSUMO`).

## Órdenes de compra

Estados: `BORRADOR → APROBADA|CANCELADA`, `APROBADA → RECIBIDA|CANCELADA`,
`RECIBIDA`/`CANCELADA` sin salida. Cualquier otra transición → `400`.

- `POST /ordenes` crea en `BORRADOR`; `total = cantidad × precioUnitario`
  se calcula en el servidor (se ignora si el cliente lo envía).
  `cantidad <= 0` → `400`. Sin `bodegaDestino` → `400`.
- `PATCH /ordenes/{id}/estado` recibe `{"estado": "APROBADA"}`. Solo
  `ADMIN`/`SUPERADMIN`; `AGENTE` → `403`.
- `APROBADA → RECIBIDA`: en una única transacción se actualiza el estado
  de la orden **y** se crea un `Movimiento` `ENTRADA` (con su
  `MovimientoDetalle`) hacia `bodegaDestino` por `cantidad` unidades del
  `producto`. Si algo falla, no se guarda ni el cambio de estado ni el
  movimiento.
- Cualquier cambio de estado borra el PDF guardado (`pdfDocumento` y
  `pdfGeneradoEn` vuelven a `null`); hay que generarlo de nuevo con
  `POST /ordenes/{id}/pdf` para reflejar el estado vigente.
- `GET /ordenes/{id}/pdf` sin PDF generado → `404`.
- El PDF incluye: número de orden, fecha de creación, proveedor,
  producto, cantidad, precio unitario, total, bodega destino y estado; si
  el estado es `BORRADOR`, se dibuja una marca de agua diagonal
  semitransparente con el texto "BORRADOR".

## Resumen del panel

`POST /panel/resumen` — DTO estricto (rechaza propiedades no declaradas):

```json
{
  "fecha": "2026-08-24",
  "narrativa": "Hay productos en riesgo y una orden pendiente de aprobación.",
  "alertas": [
    { "severidad": "ALTA", "titulo": "Producto en riesgo",
      "detalle": "Producto X está por debajo de su punto de reorden.",
      "productoId": 12, "ordenId": null, "bodegaId": 3 }
  ],
  "accionesSugeridas": [
    { "tipo": "REVISAR_ORDEN", "descripcion": "Revisar la orden 14 antes de aprobarla.",
      "ordenId": 14, "productoId": null, "bodegaId": null }
  ]
}
```

Reglas de validación (Bean Validation + comprobaciones de servicio, sin
librería de JSON Schema):

- `fecha`: `YYYY-MM-DD`, debe ser la fecha actual en America/Bogota.
- `narrativa`: 20–500 caracteres.
- `alertas`/`accionesSugeridas`: arreglos (pueden ir vacíos), nunca
  `null`.
- `severidad ∈ {BAJA, MEDIA, ALTA}`; `tipo ∈ {REVISAR_ORDEN,
  REVISAR_PRODUCTO, REVISAR_BODEGA}`.
- Cada `productoId`/`ordenId`/`bodegaId` informado debe existir.
- Una alerta enlaza **al menos uno** de `productoId`/`ordenId`/`bodegaId`;
  una acción enlaza **exactamente uno**.
- Solo puede existir un `ResumenPanel` por `fecha`: publicar de nuevo para
  la misma fecha reemplaza el `contenidoJson` anterior (se audita el
  reemplazo vía `AuditoriaEntityListener`, igual que el resto de
  entidades `Auditable`).
- JSON inválido → `400`, y `GET /panel/resumen` sigue devolviendo el
  último resumen **válido** (el intento fallido nunca se persiste).

## Seguridad

Nuevo rol `AGENTE`, añadido a `rol_usuario` y a `Rol`. Reglas:

| Endpoint | AGENTE | ADMIN/SUPERADMIN | otros autenticados |
|---|---|---|---|
| `GET /kpis`, `/productos/*/stock`, `/productos/riesgo`, `/bodegas/criticas`, `/proveedores`, `GET /ordenes*`, `GET /panel/resumen` | ✅ | ✅ | ✅ (lectura general, igual que el resto de la app) |
| `POST /ordenes` | ✅ | ✅ | ❌ |
| `POST /panel/resumen` | ✅ | ✅ | ❌ |
| `PATCH /ordenes/{id}/estado` | ❌ (403) | ✅ | ❌ |
| `POST/GET /ordenes/{id}/pdf` | ❌ | ✅ | ❌ |

Errores: `400` reglas/transiciones inválidas, `404` recurso inexistente,
`403` prohibido por rol, `401` sesión inválida — reutilizando
`GlobalExceptionHandler` ya existente.

## MCP (6 herramientas, sin más)

`consultar_stock_producto`, `consultar_bodegas_criticas`,
`consultar_productos_en_riesgo`, `consultar_kpis`,
`crear_orden_borrador`, `publicar_resumen`. Ninguna herramienta aprueba,
cancela ni recibe órdenes.

## n8n

Un flujo "Resumen diario de inventario": Schedule Trigger 6:00 a. m.
America/Bogota → AI Agent (con las 6 tools MCP + prompt basado en el
skill) → como máximo 1 orden borrador (para el primer producto en riesgo,
`cantidad = ceil(max(1, puntoReorden×2 - stockTotal))`) → publicar
resumen → nodo de salida éxito/error.
