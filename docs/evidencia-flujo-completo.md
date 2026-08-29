# Evidencia — flujo completo end-to-end (backend real)

Ejecución real contra un backend de LogiTrack corriendo en
`http://localhost:8085` (ver nota sobre la base de datos en
[`../mcp-server/EVIDENCIA.md`](../mcp-server/EVIDENCIA.md)). Demuestra el
flujo de negocio completo que pide el enunciado, de punta a punta, con
respuestas reales de la API (no simuladas):

1. **Existe un producto en riesgo** — `Silla ergonómica oficina`
   (id 3): stock 8, punto de reorden 22.5 → `EN_RIESGO`
   (`GET /productos/riesgo`, ver `mcp-server/EVIDENCIA.md` #3).
2. **El servidor MCP (rol AGENTE) crea la orden en BORRADOR** —
   `crear_orden_borrador`, cantidad `37` = `ceil(max(1, 22.5×2 - 8))`
   (`mcp-server/EVIDENCIA.md` #5).
3. **Un ADMIN aprueba la orden** — `PATCH /ordenes/1/estado {"estado":"APROBADA"}`:

   ```json
   {"id":1,"productoId":3,"estado":"APROBADA", "...": "..."}
   ```
4. **El ADMIN recibe la orden** — `PATCH /ordenes/1/estado {"estado":"RECIBIDA"}`:

   ```json
   {"id":1,"productoId":3,"estado":"RECIBIDA", "...": "..."}
   ```
5. **La recepción generó automáticamente el movimiento ENTRADA** — se
   verifica indirectamente por el efecto en el stock (no hace falta
   consultar `/movimientos`: el cambio de 8 → 45 unidades solo puede
   venir de un movimiento ENTRADA de 37 unidades a la bodega destino,
   que es exactamente `cantidad` de la orden):

   `GET /productos/3/stock` **antes**: `stockTotal: 8`
   `GET /productos/3/stock` **después**: `stockTotal: 45` (8 + 37 ✓)

6. **El dashboard (KPIs) refleja el inventario actualizado**:

   | Indicador | Antes de recibir | Después de recibir |
   |---|---|---|
   | `productosEnRiesgo` | 1 | **0** (la Silla ya no está en riesgo) |
   | Stock de la Silla (bodega Central) | 8 | **45** |

Este recorrido (paso 1 → 6) es el mismo que debe mostrarse en el video
de 4-6 minutos, ahora usando el dashboard y n8n en vez de curl/MCP
directo.
