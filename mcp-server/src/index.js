#!/usr/bin/env node
// Servidor MCP de LogiTrack IQ. Expone EXACTAMENTE 6 herramientas (ver
// docs/enunciado-logitrack-iq.md, seccion 7): consultar_stock_producto,
// consultar_bodegas_criticas, consultar_productos_en_riesgo,
// consultar_kpis, crear_orden_borrador y publicar_resumen. No existe
// ninguna herramienta para aprobar/cancelar/recibir ordenes: esa
// restriccion es obligatoria y esta impuesta aqui, no solo en el prompt
// del agente.

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { get, post } from "./logitrackClient.js";

const server = new McpServer({
    name: "logitrack-iq-mcp",
    version: "1.0.0",
});

function resultadoJson(datos) {
    return { content: [{ type: "text", text: JSON.stringify(datos, null, 2) }] };
}

function resultadoError(error) {
    return {
        content: [{ type: "text", text: `Error al llamar la API de LogiTrack: ${error.message}` }],
        isError: true,
    };
}

server.tool(
    "consultar_stock_producto",
    "Consulta el stock total y el desglose por bodega de un producto, calculado desde movimientos (GET /productos/{id}/stock).",
    { productoId: z.number().int().positive() },
    async ({ productoId }) => {
        try {
            return resultadoJson(await get(`/productos/${productoId}/stock`));
        } catch (error) {
            return resultadoError(error);
        }
    },
);

server.tool(
    "consultar_bodegas_criticas",
    "Lista las bodegas con ocupacion mayor o igual a 90% (GET /bodegas/criticas).",
    {},
    async () => {
        try {
            return resultadoJson(await get("/bodegas/criticas"));
        } catch (error) {
            return resultadoError(error);
        }
    },
);

server.tool(
    "consultar_productos_en_riesgo",
    "Lista los productos con proveedor principal cuyo stock total esta por debajo de su punto de reorden, con consumo, cobertura y bodega destino sugerida (GET /productos/riesgo).",
    {},
    async () => {
        try {
            return resultadoJson(await get("/productos/riesgo"));
        } catch (error) {
            return resultadoError(error);
        }
    },
);

server.tool(
    "consultar_kpis",
    "Obtiene los indicadores del dashboard: ocupacion por bodega, productos en quiebre, productos en riesgo, ordenes por aprobar y movimientos de ayer (GET /kpis).",
    {},
    async () => {
        try {
            return resultadoJson(await get("/kpis"));
        } catch (error) {
            return resultadoError(error);
        }
    },
);

server.tool(
    "crear_orden_borrador",
    "Crea una orden de compra en estado BORRADOR (POST /ordenes). El total lo calcula el servidor. " +
        "IMPORTANTE: esta herramienta NUNCA aprueba, cancela ni recibe una orden; esa accion es exclusiva " +
        "de un ADMIN humano desde el dashboard.",
    {
        productoId: z.number().int().positive(),
        proveedorId: z.number().int().positive(),
        bodegaDestinoId: z.number().int().positive(),
        cantidad: z.number().int().positive(),
        precioUnitario: z.number().nonnegative(),
    },
    async (args) => {
        try {
            return resultadoJson(await post("/ordenes", args));
        } catch (error) {
            return resultadoError(error);
        }
    },
);

const AlertaShape = {
    severidad: z.enum(["BAJA", "MEDIA", "ALTA"]),
    titulo: z.string(),
    detalle: z.string(),
    productoId: z.number().int().nullish(),
    ordenId: z.number().int().nullish(),
    bodegaId: z.number().int().nullish(),
};

const AccionShape = {
    tipo: z.enum(["REVISAR_ORDEN", "REVISAR_PRODUCTO", "REVISAR_BODEGA"]),
    descripcion: z.string(),
    ordenId: z.number().int().nullish(),
    productoId: z.number().int().nullish(),
    bodegaId: z.number().int().nullish(),
};

server.tool(
    "publicar_resumen",
    "Publica (o reemplaza si ya existe uno para hoy) el resumen del panel del dia (POST /panel/resumen). " +
        "Debe cumplir EXACTAMENTE el contrato: fecha (YYYY-MM-DD, hoy en America/Bogota), narrativa " +
        "(20-500 caracteres), alertas y accionesSugeridas (arreglos, pueden ir vacios).",
    {
        resumen: z.object({
            fecha: z.string(),
            narrativa: z.string(),
            alertas: z.array(z.object(AlertaShape)),
            accionesSugeridas: z.array(z.object(AccionShape)),
        }),
    },
    async ({ resumen }) => {
        try {
            return resultadoJson(await post("/panel/resumen", resumen));
        } catch (error) {
            return resultadoError(error);
        }
    },
);

const transport = new StdioServerTransport();
await server.connect(transport);
