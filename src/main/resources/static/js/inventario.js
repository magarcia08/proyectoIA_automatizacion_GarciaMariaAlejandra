let inventarioActual = [];
let inventarioBodegas = [];
let inventarioProductos = [];
let mostrandoSoloStockBajo = false;

async function iniciarInventario() {
    inventarioBodegas = await peticionApi(API.RUTAS.BODEGAS);
    inventarioProductos = await peticionApi(API.RUTAS.PRODUCTOS);

    if (!esModuloVigente("inventario")) {
        return;
    }

    llenarFiltroBodegas();
    document.getElementById("inventorySearch").addEventListener("input", filtrarInventario);
    document.getElementById("warehouseFilter").addEventListener("change", filtrarInventario);
    document.getElementById("lowStockToggle").addEventListener("change", alternarStockBajo);

    await cargarInventario();
}

async function cargarInventario() {
    try {
        inventarioActual = await peticionApi(API.RUTAS.INVENTARIO);
        if (!esModuloVigente("inventario")) {
            return;
        }
        filtrarInventario();
    } catch (error) {
        if (!esModuloVigente("inventario")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarInventario([]);
    }
}

/** Consulta avanzada: productos con stock bajo (< 10), contra el endpoint dedicado del backend. */
async function alternarStockBajo(evento) {
    mostrandoSoloStockBajo = evento.target.checked;
    try {
        if (mostrandoSoloStockBajo) {
            inventarioActual = await peticionApi(API.RUTAS.INVENTARIO + "/stock-bajo");
        } else {
            inventarioActual = await peticionApi(API.RUTAS.INVENTARIO);
        }
        if (!esModuloVigente("inventario")) {
            return;
        }
        filtrarInventario();
    } catch (error) {
        if (!esModuloVigente("inventario")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

function llenarFiltroBodegas() {
    const select = document.getElementById("warehouseFilter");
    select.innerHTML = '<option value="">Todas las bodegas</option>' +
        inventarioBodegas.map(item => `<option value="${item.id}">${escaparHtml(item.nombre)}</option>`).join("");
}

function filtrarInventario() {
    const texto = document.getElementById("inventorySearch").value.toLowerCase();
    const bodegaId = document.getElementById("warehouseFilter").value;

    const filtrados = inventarioActual.filter(item => {
        const coincideTexto = item.bodegaNombre.toLowerCase().includes(texto) ||
                              item.productoNombre.toLowerCase().includes(texto);
        const coincideBodega = !bodegaId || Number(item.bodegaId) === Number(bodegaId);
        return coincideTexto && coincideBodega;
    });
    renderizarInventario(filtrados);
}

function renderizarInventario(lista) {
    const body = document.getElementById("inventoryTableBody");
    const total = lista.reduce((suma, item) => suma + Number(item.stock), 0);
    document.getElementById("inventoryTableFooter").textContent =
        lista.length + " registros · " + formatoNumero(total) + " unidades";

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="5"><div class="empty-state">No hay inventario para mostrar.</div></td></tr>';
        return;
    }

    body.innerHTML = lista.map(item => {
        const estado = estadoStock(item.stock);
        // El endpoint /inventario/stock-bajo no trae "id" (es un reporte, no una entidad),
        // así que mostramos bodegaId-productoId como referencia en ese caso.
        const identificador = item.id != null ? "#" + item.id : `${item.bodegaId}-${item.productoId}`;
        return `
            <tr>
                <td>${identificador}</td>
                <td class="cell-title">${escaparHtml(item.bodegaNombre)}</td>
                <td>${escaparHtml(item.productoNombre)}</td>
                <td>${formatoNumero(item.stock)}</td>
                <td><span class="badge ${estado.clase}">${estado.texto}</span></td>
            </tr>`;
    }).join("");
}
