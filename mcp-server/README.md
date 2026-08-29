# Servidor MCP — LogiTrack IQ

Servidor MCP pequeño (Node.js, transporte stdio, SDK oficial
`@modelcontextprotocol/sdk`) que expone **exactamente seis herramientas**
sobre la API real de LogiTrack, autenticado como un usuario con rol
`AGENTE`. No accede a la base de datos directamente ni implementa reglas
de negocio: cada herramienta es un llamado HTTP a un endpoint ya
protegido por Spring Security.

## Herramientas

| Herramienta | Endpoint |
|---|---|
| `consultar_stock_producto(productoId)` | `GET /productos/{id}/stock` |
| `consultar_bodegas_criticas()` | `GET /bodegas/criticas` |
| `consultar_productos_en_riesgo()` | `GET /productos/riesgo` |
| `consultar_kpis()` | `GET /kpis` |
| `crear_orden_borrador(productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario)` | `POST /ordenes` |
| `publicar_resumen(resumen)` | `POST /panel/resumen` |

**No existe ninguna herramienta para aprobar, cancelar o recibir una
orden.** Esa restricción está impuesta aquí (el código simplemente no
tiene esa herramienta), no solo en el prompt del agente — ver
[`../skills/operacion-logitrack/SKILL.md`](../skills/operacion-logitrack/SKILL.md).

## Instalación

```bash
cd mcp-server
npm install
```

## Configuración

Variables de entorno (todas opcionales, con valores por defecto que
coinciden con `data.sql`):

| Variable | Default | Descripción |
|---|---|---|
| `LOGITRACK_API_URL` | `http://localhost:8085` | URL base del backend de LogiTrack |
| `LOGITRACK_AGENTE_USERNAME` | `agente` | Usuario con rol `AGENTE` |
| `LOGITRACK_AGENTE_PASSWORD` | `agente123` | Contraseña de ese usuario |

El servidor hace login contra `POST /auth/login` la primera vez que se
usa una herramienta, guarda el token JWT en memoria y vuelve a
autenticarse automáticamente si el token expira (`401`).

## Ejecutar

```bash
npm start
# equivalente a: node src/index.js
```

El proceso queda esperando mensajes JSON-RPC por stdin/stdout (protocolo
MCP estándar). No se ejecuta solo: lo lanza un cliente MCP (n8n, Claude
Desktop, el `evidencia-runner.mjs` de este mismo directorio, etc.).

## Usarlo desde n8n

En el nodo **MCP Client Tool** (conectado como herramienta del nodo **AI
Agent**), configura:

- **Transporte**: Command Line (STDIO)
- **Comando**: `node`
- **Argumentos**: `<ruta-absoluta-al-repo>/mcp-server/src/index.js`
- **Variables de entorno**: `LOGITRACK_API_URL`, `LOGITRACK_AGENTE_USERNAME`,
  `LOGITRACK_AGENTE_PASSWORD` (si el backend no corre en
  `localhost:8085` con las credenciales por defecto).

Ver [`../n8n/README.md`](../n8n/README.md) para el flujo completo.

## Evidencia

Ver [`EVIDENCIA.md`](EVIDENCIA.md): entrada/salida real de las 6
herramientas (incluye un error controlado) contra un backend real
levantado localmente, y el PDF con marca de agua generado a partir de la
orden creada por `crear_orden_borrador`
([`../docs/capturas/orden-1-borrador-marca-de-agua.pdf`](../docs/capturas/orden-1-borrador-marca-de-agua.pdf)).

`evidencia-runner.mjs` es el script (cliente MCP real, mismo SDK) que
generó esa evidencia; se puede volver a correr con:

```bash
node evidencia-runner.mjs
```

(requiere el backend corriendo y accesible en `LOGITRACK_API_URL`).
