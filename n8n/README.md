# Flujo n8n — Resumen diario de inventario

`resumen-diario-inventario.json` es el export de un único flujo llamado
**Resumen diario de inventario**, con esta estructura:

```
Schedule Trigger (6:00 a.m. America/Bogota)
        │
        ▼
     AI Agent  ←──ai_languageModel── Modelo de chat (OpenAI/Anthropic/…)
        │      ←──ai_tool──────────── LogiTrack MCP (6 herramientas)
        ▼
   ¿Hubo error? ──sí──▶ Salida: error controlado
        │
        no
        ▼
   Salida: exito
```

El nodo **AI Agent** sigue, en su `systemMessage`, las mismas reglas de
[`../skills/operacion-logitrack/SKILL.md`](../skills/operacion-logitrack/SKILL.md)
(consultar riesgo primero, máximo una orden en borrador, nunca aprobar,
publicar el resumen con el contrato exacto, informar cualquier error).

## Importarlo

1. En n8n: **Workflows → Import from File** → selecciona
   `resumen-diario-inventario.json`.
2. **Este entorno no tiene una instancia de n8n disponible para
   ejecutarlo**: el estudiante debe importarlo en su propio n8n
   (self-hosted o cloud) y completar lo siguiente antes de correrlo.

## Configuración necesaria después de importar

1. **Backend accesible**: LogiTrack debe estar corriendo (por defecto
   `http://localhost:8085`; si n8n corre en Docker, usa la URL que
   resuelva a tu máquina host, p. ej. `http://host.docker.internal:8085`).
2. **Nodo "LogiTrack MCP (6 herramientas)"**: es un nodo *MCP Client
   Tool*. Si al importar n8n lo marca como nodo desconocido, agrégalo de
   nuevo desde el panel de nodos (busca "MCP Client Tool"), configúralo
   con:
   - Transporte: **Command Line (STDIO)**
   - Comando: `node`
   - Argumentos: ruta absoluta a `mcp-server/src/index.js` en tu máquina
   - Variables de entorno (si tu backend no usa los valores por defecto):
     `LOGITRACK_API_URL`, `LOGITRACK_AGENTE_USERNAME`,
     `LOGITRACK_AGENTE_PASSWORD`
   - Reconéctalo como herramienta (`ai_tool`) del nodo **AI Agent**.
3. **Nodo "Modelo de chat"**: agrega tu propia credencial de LLM (OpenAI,
   Anthropic, el que tengas disponible) y conéctalo como
   `ai_languageModel` del **AI Agent**. El nodo importado trae un
   `credentials.id` placeholder que hay que reemplazar.
4. Verifica que **Settings → Timezone** del workflow quede en
   `America/Bogota` (ya viene así en el JSON).

## Ejecutarlo para la demo

El enunciado permite ejecutar el flujo manualmente (sin cambiar su
cronograma) para la demostración: usa el botón **Execute Workflow** en
vez de esperar a las 6:00 a.m.

Para la evidencia pedida en los entregables, captura:

- una ejecución exitosa (un producto en riesgo real en los datos de
  `data.sql`, como `Silla ergonómica oficina`, que crea una orden en
  BORRADOR y publica el resumen — ver
  [`../mcp-server/EVIDENCIA.md`](../mcp-server/EVIDENCIA.md) para ver
  exactamente esa ejecución hecha con las mismas herramientas, sin pasar
  por n8n);
- una ejecución con un error controlado (por ejemplo, backend apagado, o
  credencial de MCP mal configurada) que caiga en la rama "Salida: error
  controlado" **sin** haber creado una orden indebida.

## Por qué no se ejecutó aquí

Este flujo requiere una instancia de n8n corriendo y una credencial de
modelo de lenguaje (OpenAI/Anthropic/etc.) con costo asociado, ninguna de
las cuales está disponible en el entorno de desarrollo donde se construyó
este proyecto. En su lugar, se generó evidencia real y equivalente
llamando las mismas 6 herramientas del servidor MCP directamente como
cliente MCP (mismo protocolo, mismo servidor) contra el backend real —
ver [`../mcp-server/EVIDENCIA.md`](../mcp-server/EVIDENCIA.md) y
[`../docs/evidencia-flujo-completo.md`](../docs/evidencia-flujo-completo.md).
Ejecutar este flujo importado, con tu propia credencial de modelo, es lo
único que falta para la captura de pantalla que pide el entregable 8.
