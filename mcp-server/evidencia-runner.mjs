// Runner temporal para generar EVIDENCIA.md: llama las 6 herramientas del
// servidor MCP como cliente MCP real (protocolo stdio), contra el backend
// real levantado en http://localhost:8085. No forma parte del servidor.
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

const client = new Client({ name: "evidencia-runner", version: "1.0.0" });
const transport = new StdioClientTransport({
    command: process.execPath,
    args: ["src/index.js"],
    env: { ...process.env, LOGITRACK_API_URL: "http://localhost:8085" },
});

await client.connect(transport);

const salida = [];

function registrar(nombre, args, resultado) {
    salida.push({ herramienta: nombre, entrada: args, salida: resultado });
    console.log(`\n=== ${nombre} ===`);
    console.log("Entrada:", JSON.stringify(args));
    console.log("Salida:", JSON.stringify(resultado, null, 2));
}

try {
    const kpis = await client.callTool({ name: "consultar_kpis", arguments: {} });
    registrar("consultar_kpis", {}, kpis);

    const criticas = await client.callTool({ name: "consultar_bodegas_criticas", arguments: {} });
    registrar("consultar_bodegas_criticas", {}, criticas);

    const riesgo = await client.callTool({ name: "consultar_productos_en_riesgo", arguments: {} });
    registrar("consultar_productos_en_riesgo", {}, riesgo);

    const riesgoTexto = riesgo.content[0].text;
    const productosRiesgo = JSON.parse(riesgoTexto);
    const primero = productosRiesgo[0];

    const stock = await client.callTool({
        name: "consultar_stock_producto",
        arguments: { productoId: primero.productoId },
    });
    registrar("consultar_stock_producto", { productoId: primero.productoId }, stock);

    const cantidad = Math.max(1, Math.ceil(primero.puntoReorden * 2 - primero.stockTotal));
    const ordenArgs = {
        productoId: primero.productoId,
        proveedorId: primero.proveedorId,
        bodegaDestinoId: primero.bodegaDestinoId,
        cantidad,
        precioUnitario: 450000,
    };
    const orden = await client.callTool({ name: "crear_orden_borrador", arguments: ordenArgs });
    registrar("crear_orden_borrador", ordenArgs, orden);

    const ordenCreada = JSON.parse(orden.content[0].text);

    const hoy = new Date().toLocaleDateString("en-CA", { timeZone: "America/Bogota" });
    const resumenArgs = {
        resumen: {
            fecha: hoy,
            narrativa: `Se detecto el producto ${primero.nombreProducto} por debajo de su punto de reorden y se creo la orden ${ordenCreada.id} en BORRADOR para revision del ADMIN.`,
            alertas: [
                {
                    severidad: "ALTA",
                    titulo: "Producto en riesgo",
                    detalle: `${primero.nombreProducto} tiene stock ${primero.stockTotal}, por debajo del punto de reorden ${primero.puntoReorden}.`,
                    productoId: primero.productoId,
                },
            ],
            accionesSugeridas: [
                {
                    tipo: "REVISAR_ORDEN",
                    descripcion: `Revisar la orden ${ordenCreada.id} antes de aprobarla.`,
                    ordenId: ordenCreada.id,
                },
            ],
        },
    };
    const resumen = await client.callTool({ name: "publicar_resumen", arguments: resumenArgs });
    registrar("publicar_resumen", resumenArgs, resumen);

    // Caso de error controlado: crear_orden_borrador con un producto inexistente.
    try {
        const errorArgs = { productoId: 999999, proveedorId: 999999, bodegaDestinoId: 999999, cantidad: 1, precioUnitario: 1 };
        const errorado = await client.callTool({ name: "crear_orden_borrador", arguments: errorArgs });
        registrar("crear_orden_borrador (error controlado: producto inexistente)", errorArgs, errorado);
    } catch (e) {
        registrar("crear_orden_borrador (error controlado: producto inexistente)", "productoId=999999", { error: e.message });
    }

    process.stdout.write("\n\n===EVIDENCIA_JSON_START===\n");
    process.stdout.write(JSON.stringify(salida, null, 2));
    process.stdout.write("\n===EVIDENCIA_JSON_END===\n");
} finally {
    await client.close();
}
