let reporteMovBodegas = [];
let reporteMovProductos = [];
let reporteAuditoriasActuales = [];

async function iniciarReportes() {
    document.getElementById("printReportButton").addEventListener("click", () => window.print());
    configurarCierreModal("reportAuditDetailModal");

    document.getElementById("reportMovFilterButton").addEventListener("click", aplicarFiltroReporteMovimientos);
    document.getElementById("reportMovClearButton").addEventListener("click", limpiarFiltroReporteMovimientos);
    document.getElementById("reportAudFilterButton").addEventListener("click", aplicarFiltroReporteAuditoria);
    document.getElementById("reportAudClearButton").addEventListener("click", limpiarFiltroReporteAuditoria);

    try {
        const resumen = await peticionApi(API.RUTAS.REPORTES + "/resumen");
        if (!esModuloVigente("reporte")) {
            return;
        }
        construirKpisReporte(resumen);
        construirBarrasReporte(resumen);
        construirProductosMovidos(resumen);
    } catch (error) {
        if (!esModuloVigente("reporte")) {
            return;
        }
        mostrarToast(error.message, "error");
    }

    await cargarSelectsFiltrosReporte();
    if (!esModuloVigente("reporte")) {
        return;
    }
    await cargarReporteMovimientos();
    await cargarReporteAuditoria();
}

function construirKpisReporte(resumen) {
    const stockTotal = resumen.stockPorBodega.reduce((suma, item) => suma + Number(item.stockTotal), 0);

    document.getElementById("reportKpis").innerHTML =
        tarjetaKpi("▦", "Stock consolidado", formatoNumero(stockTotal), "unidades en todas las bodegas", "#dcfce7", "#16a34a") +
        tarjetaKpi("▥", "Bodegas", resumen.totalBodegas, "bodegas registradas", "#ede9fe", "#7c3aed") +
        tarjetaKpi("□", "Productos", resumen.totalProductos, "productos en catálogo", "#dbeafe", "#1d4ed8") +
        tarjetaKpi("⇄", "Movimientos", resumen.totalMovimientos, "movimientos registrados", "#fef3c7", "#d97706");
}

function construirBarrasReporte(resumen) {
    const totales = resumen.stockPorBodega.map(item => ({
        nombre: item.bodegaNombre,
        total: Number(item.stockTotal)
    }));
    const maximo = Math.max(...totales.map(item => item.total), 1);
    document.getElementById("reportWarehouseBars").innerHTML = totales.map(item => `
        <div class="warehouse-row">
            <strong>${escaparHtml(item.nombre)}</strong>
            <div class="bar-track">
                <span class="bar-fill" style="width:${(item.total / maximo) * 100}%"></span>
            </div>
            <span>${formatoNumero(item.total)}</span>
        </div>`).join("");
}

function construirProductosMovidos(resumen) {
    const productos = resumen.productosMasMovidos.slice(0, 5);
    if (productos.length === 0) {
        document.getElementById("topProductsList").innerHTML =
            '<div class="empty-state">Todavía no hay movimientos registrados.</div>';
        return;
    }
    document.getElementById("topProductsList").innerHTML = productos.map((producto, index) => `
        <div class="activity-item">
            <span class="activity-icon badge-blue">${index + 1}</span>
            <div class="activity-copy">
                <strong>${escaparHtml(producto.productoNombre)}</strong>
                <small>${formatoNumero(producto.totalMovido)} unidades movidas</small>
            </div>
        </div>`).join("");
}

// ==================== Explorador de movimientos (examen: filtros) ====================

/** Llena los selects de bodega/producto usados por los dos filtros de abajo. */
async function cargarSelectsFiltrosReporte() {
    try {
        [reporteMovBodegas, reporteMovProductos] = await Promise.all([
            peticionApi(API.RUTAS.BODEGAS),
            peticionApi(API.RUTAS.PRODUCTOS)
        ]);
        if (!esModuloVigente("reporte")) {
            return;
        }

        const opcionesBodega = '<option value="">Todas las bodegas</option>' +
            reporteMovBodegas.map(b => `<option value="${b.id}">${escaparHtml(b.nombre)}</option>`).join("");
        document.getElementById("reportMovBodega").innerHTML = opcionesBodega;

        const opcionesProducto = '<option value="">Todos los productos</option>' +
            reporteMovProductos.map(p => `<option value="${p.id}">${escaparHtml(p.nombre)}</option>`).join("");
        document.getElementById("reportMovProducto").innerHTML = opcionesProducto;
        document.getElementById("reportAudProducto").innerHTML = opcionesProducto;
    } catch (error) {
        if (!esModuloVigente("reporte")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

/** Arma el query string a partir de los filtros que sí tengan valor. */
function armarQueryReporte(parametros) {
    const partes = Object.entries(parametros)
        .filter(([, valor]) => valor !== null && valor !== undefined && valor !== "")
        .map(([clave, valor]) => `${clave}=${encodeURIComponent(valor)}`);
    return partes.length === 0 ? "" : "?" + partes.join("&");
}

async function cargarReporteMovimientos() {
    try {
        const lista = await peticionApi(API.RUTAS.REPORTES + "/movimientos");
        if (!esModuloVigente("reporte")) {
            return;
        }
        renderizarReporteMovimientos(lista);
    } catch (error) {
        if (!esModuloVigente("reporte")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarReporteMovimientos([]);
    }
}

async function aplicarFiltroReporteMovimientos() {
    const bodega = document.getElementById("reportMovBodega").value;
    const producto = document.getElementById("reportMovProducto").value;
    const tipoMovimiento = document.getElementById("reportMovTipo").value;
    const desde = document.getElementById("reportMovDesde").value;
    const hasta = document.getElementById("reportMovHasta").value;

    const query = armarQueryReporte({
        bodega,
        producto,
        tipoMovimiento,
        fechaInicio: desde ? desde + "T00:00:00" : "",
        fechaFin: hasta ? hasta + "T23:59:59" : ""
    });

    try {
        const lista = await peticionApi(API.RUTAS.REPORTES + "/movimientos" + query);
        renderizarReporteMovimientos(lista);
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

function limpiarFiltroReporteMovimientos() {
    document.getElementById("reportMovBodega").value = "";
    document.getElementById("reportMovProducto").value = "";
    document.getElementById("reportMovTipo").value = "";
    document.getElementById("reportMovDesde").value = "";
    document.getElementById("reportMovHasta").value = "";
    cargarReporteMovimientos();
}

function renderizarReporteMovimientos(lista) {
    const body = document.getElementById("reportMovTableBody");
    document.getElementById("reportMovTableFooter").textContent =
        lista.length + (lista.length === 1 ? " movimiento encontrado" : " movimientos encontrados");

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="7"><div class="empty-state">No hay movimientos para estos filtros.</div></td></tr>';
        return;
    }

    body.innerHTML = lista.slice().reverse().map(item => {
        const clase = item.tipo === "ENTRADA" ? "badge-green"
            : item.tipo === "SALIDA" ? "badge-red" : "badge-blue";
        const detalle = (item.detalles || [])
            .map(d => `${escaparHtml(d.productoNombre)} (${d.cantidad})`)
            .join(", ");
        return `
            <tr>
                <td>#${item.id}</td>
                <td>${formatoFecha(item.fecha)}</td>
                <td><span class="badge ${clase}">${item.tipo}</span></td>
                <td>${escaparHtml(item.usuarioUsername || "")}</td>
                <td>${escaparHtml(item.bodegaOrigenNombre || "—")}</td>
                <td>${escaparHtml(item.bodegaDestinoNombre || "—")}</td>
                <td>${detalle || "—"}</td>
            </tr>`;
    }).join("");
}

// ==================== Explorador de auditoría (examen: filtros) ====================

async function cargarReporteAuditoria() {
    try {
        const lista = await peticionApi(API.RUTAS.REPORTES + "/auditoria");
        if (!esModuloVigente("reporte")) {
            return;
        }
        renderizarReporteAuditoria(lista);
    } catch (error) {
        if (!esModuloVigente("reporte")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarReporteAuditoria([]);
    }
}

async function aplicarFiltroReporteAuditoria() {
    const producto = document.getElementById("reportAudProducto").value;
    const desde = document.getElementById("reportAudDesde").value;
    const hasta = document.getElementById("reportAudHasta").value;
    const campoModificado = document.getElementById("reportAudCampo").value.trim();

    const query = armarQueryReporte({
        producto,
        fechaInicio: desde ? desde + "T00:00:00" : "",
        fechaFin: hasta ? hasta + "T23:59:59" : "",
        campoModificado
    });

    try {
        const lista = await peticionApi(API.RUTAS.REPORTES + "/auditoria" + query);
        renderizarReporteAuditoria(lista);
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

function limpiarFiltroReporteAuditoria() {
    document.getElementById("reportAudProducto").value = "";
    document.getElementById("reportAudDesde").value = "";
    document.getElementById("reportAudHasta").value = "";
    document.getElementById("reportAudCampo").value = "";
    cargarReporteAuditoria();
}

function renderizarReporteAuditoria(lista) {
    const body = document.getElementById("reportAudTableBody");
    document.getElementById("reportAudTableFooter").textContent =
        lista.length + (lista.length === 1 ? " registro encontrado" : " registros encontrados");

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="7"><div class="empty-state">No hay auditorías para estos filtros.</div></td></tr>';
        return;
    }

    // Se guarda para poder ubicar el registro por id al abrir el detalle (mismo patron que auditoria.js).
    reporteAuditoriasActuales = lista;

    body.innerHTML = lista.slice().reverse().map(item => {
        const clase = item.tipoOperacion === "INSERT" ? "badge-green"
            : item.tipoOperacion === "DELETE" ? "badge-red" : "badge-amber";
        return `
            <tr>
                <td>#${item.id}</td>
                <td>${formatoFecha(item.fechaHora)}</td>
                <td><span class="badge ${clase}">${item.tipoOperacion}</span></td>
                <td>${escaparHtml(item.usuarioUsername || "")}</td>
                <td class="cell-title">${escaparHtml(item.entidadAfectada)}</td>
                <td>#${item.entidadId || "—"}</td>
                <td>
                    <button class="action-button" onclick="verDetalleReporteAuditoria(${item.id})">Ver</button>
                </td>
            </tr>`;
    }).join("");
}

function verDetalleReporteAuditoria(id) {
    const registro = reporteAuditoriasActuales.find(item => Number(item.id) === Number(id));
    if (!registro) {
        return;
    }
    document.getElementById("reportAuditDetailBefore").textContent = formatearJsonAuditoria(registro.valoresAnteriores);
    document.getElementById("reportAuditDetailAfter").textContent = formatearJsonAuditoria(registro.valoresNuevos);
    abrirModal("reportAuditDetailModal");
}