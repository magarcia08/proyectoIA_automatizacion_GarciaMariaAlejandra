let tcRiesgoActual = [];
let tcOrdenesActuales = [];

async function iniciarTorreControl() {
    document.getElementById("tcRefreshButton").addEventListener("click", cargarTorreControl);
    await cargarTorreControl();
}

async function cargarTorreControl() {
    if (!esModuloVigente("torre-control")) {
        return;
    }
    await Promise.all([
        cargarKpis(),
        cargarResumenPanel(),
        cargarProductosEnRiesgo(),
        cargarOrdenesBorrador()
    ]);
}

// ------------------------------------------------------------------
// KPIs + ocupación + movimientos de ayer
// ------------------------------------------------------------------

async function cargarKpis() {
    try {
        const kpis = await peticionApi(API.RUTAS.KPIS);
        if (!esModuloVigente("torre-control")) {
            return;
        }
        document.getElementById("tcQuiebre").textContent = kpis.productosEnQuiebre;
        document.getElementById("tcRiesgo").textContent = kpis.productosEnRiesgo;
        document.getElementById("tcOrdenesCantidad").textContent = kpis.ordenesPorAprobar.cantidad;
        document.getElementById("tcOrdenesMonto").textContent = "monto total: " + formatoMoneda(kpis.ordenesPorAprobar.montoTotal);
        document.getElementById("tcCriticas").textContent =
            kpis.ocupacionPorBodega.filter(b => b.porcentaje >= 90).length;
        document.getElementById("tcCalculadoEn").textContent = "Calculado: " + formatoFecha(kpis.calculadoEn);

        document.getElementById("tcOcupacion").innerHTML = kpis.ocupacionPorBodega.map(b => `
            <div class="warehouse-row">
                <strong>${escaparHtml(b.nombre)}${b.porcentaje >= 90 ? ' <span class="badge badge-red">crítica</span>' : ""}</strong>
                <div class="bar-track"><span class="bar-fill" style="width:${Math.min(b.porcentaje, 100)}%;${b.porcentaje >= 90 ? "background:#dc2626" : ""}"></span></div>
                <span>${b.porcentaje}%</span>
            </div>`).join("") || '<div class="empty-state">Sin bodegas registradas.</div>';

        const m = kpis.movimientosAyer;
        document.getElementById("tcMovimientosAyer").innerHTML = `
            <div class="activity-item"><span class="activity-icon badge-green">⇄</span>
                <div class="activity-copy"><strong>ENTRADA</strong><p>${m.entrada} movimiento(s)</p></div></div>
            <div class="activity-item"><span class="activity-icon badge-red">⇄</span>
                <div class="activity-copy"><strong>SALIDA</strong><p>${m.salida} movimiento(s)</p></div></div>
            <div class="activity-item"><span class="activity-icon badge-blue">⇄</span>
                <div class="activity-copy"><strong>TRANSFERENCIA</strong><p>${m.transferencia} movimiento(s)</p></div></div>`;
    } catch (error) {
        if (!esModuloVigente("torre-control")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

// ------------------------------------------------------------------
// Resumen del panel (último válido; 404 si no hay ninguno)
// ------------------------------------------------------------------

async function cargarResumenPanel() {
    const contenedor = document.getElementById("tcResumenContenido");
    try {
        const resumen = await peticionApi(API.RUTAS.PANEL_RESUMEN);
        if (!esModuloVigente("torre-control")) {
            return;
        }
        document.getElementById("tcResumenFecha").textContent = resumen.fecha + " · " + escaparHtml(resumen.autor);
        contenedor.innerHTML = `
            <p style="margin:.25rem 0 1rem">${escaparHtml(resumen.narrativa)}</p>
            <div class="dashboard-grid">
                <div>
                    <strong style="display:block;margin-bottom:.5rem">Alertas</strong>
                    ${resumen.alertas.length === 0 ? '<div class="empty-state">Sin alertas.</div>' :
                        resumen.alertas.map(a => `
                            <div class="activity-item">
                                <span class="activity-icon ${a.severidad === "ALTA" ? "badge-red" : a.severidad === "MEDIA" ? "badge-amber" : "badge-blue"}">!</span>
                                <div class="activity-copy"><strong>${escaparHtml(a.titulo)}</strong><p>${escaparHtml(a.detalle)}</p></div>
                            </div>`).join("")}
                </div>
                <div>
                    <strong style="display:block;margin-bottom:.5rem">Acciones sugeridas</strong>
                    ${resumen.accionesSugeridas.length === 0 ? '<div class="empty-state">Sin acciones sugeridas.</div>' :
                        resumen.accionesSugeridas.map(a => `
                            <div class="activity-item">
                                <span class="activity-icon badge-blue">→</span>
                                <div class="activity-copy"><strong>${escaparHtml(a.tipo)}</strong><p>${escaparHtml(a.descripcion)}</p></div>
                            </div>`).join("")}
                </div>
            </div>`;
    } catch (error) {
        if (!esModuloVigente("torre-control")) {
            return;
        }
        document.getElementById("tcResumenFecha").textContent = "—";
        contenedor.innerHTML = '<div class="empty-state">Todavía no se ha publicado un resumen (lo publica el flujo diario de n8n, o POST /panel/resumen).</div>';
    }
}

// ------------------------------------------------------------------
// Productos en riesgo
// ------------------------------------------------------------------

async function cargarProductosEnRiesgo() {
    try {
        tcRiesgoActual = await peticionApi(API.RUTAS.PRODUCTOS_RIESGO);
        if (!esModuloVigente("torre-control")) {
            return;
        }
        renderizarRiesgo();
    } catch (error) {
        if (!esModuloVigente("torre-control")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

function renderizarRiesgo() {
    const body = document.getElementById("tcRiesgoBody");
    document.getElementById("tcRiesgoFooter").textContent =
        tcRiesgoActual.length + (tcRiesgoActual.length === 1 ? " producto en riesgo" : " productos en riesgo");

    if (tcRiesgoActual.length === 0) {
        body.innerHTML = '<tr><td colspan="7"><div class="empty-state">Ningún producto está en riesgo ahora mismo.</div></td></tr>';
        return;
    }
    body.innerHTML = tcRiesgoActual.map(p => `
        <tr>
            <td class="cell-title">${escaparHtml(p.nombreProducto)}</td>
            <td>${p.stockTotal}</td>
            <td>${p.consumoDiarioPromedio ?? "—"}</td>
            <td>${p.puntoReorden ?? "—"}</td>
            <td>${p.diasCobertura ?? "Sin consumo"}</td>
            <td><span class="badge ${p.estadoCobertura === "SIN_CONSUMO" ? "badge-amber" : "badge-red"}">${p.estadoCobertura}</span></td>
            <td>#${p.bodegaDestinoId ?? "—"}</td>
        </tr>`).join("");
}

// ------------------------------------------------------------------
// Órdenes en BORRADOR
// ------------------------------------------------------------------

async function cargarOrdenesBorrador() {
    try {
        tcOrdenesActuales = await peticionApi(API.RUTAS.ORDENES + "?estado=BORRADOR");
        if (!esModuloVigente("torre-control")) {
            return;
        }
        renderizarOrdenes();
    } catch (error) {
        if (!esModuloVigente("torre-control")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

function renderizarOrdenes() {
    const body = document.getElementById("tcOrdenesBody");
    document.getElementById("tcOrdenesFooter").textContent =
        tcOrdenesActuales.length + (tcOrdenesActuales.length === 1 ? " orden en BORRADOR" : " órdenes en BORRADOR");

    if (tcOrdenesActuales.length === 0) {
        body.innerHTML = '<tr><td colspan="9"><div class="empty-state">No hay órdenes en BORRADOR.</div></td></tr>';
        return;
    }

    const esAdmin = esAdminOSuperior();
    body.innerHTML = tcOrdenesActuales.map(o => `
        <tr>
            <td>#${o.id}</td>
            <td class="cell-title">${escaparHtml(o.nombreProducto)}</td>
            <td>${escaparHtml(o.nombreProveedor)}</td>
            <td>${escaparHtml(o.nombreBodegaDestino)}</td>
            <td>${o.cantidad}</td>
            <td>${formatoMoneda(o.total)}</td>
            <td>${escaparHtml(o.creadoPor)}</td>
            <td>${o.pdfDisponible ? '<span class="badge badge-green">generado</span>' : '<span class="badge badge-amber">pendiente</span>'}</td>
            <td>
                <div class="table-actions">
                    <button class="action-button" onclick="tcGenerarPdf(${o.id})" title="Generar PDF">📄+</button>
                    ${o.pdfDisponible ? `<button class="action-button" onclick="tcVerPdf(${o.id})" title="Ver PDF">👁</button>` : ""}
                    ${esAdmin ? `<button class="action-button" onclick="tcAprobarOrden(${o.id})" title="Aprobar">✓</button>` : ""}
                </div>
            </td>
        </tr>`).join("");
}

async function tcGenerarPdf(ordenId) {
    try {
        await peticionApiBinaria(API.RUTAS.ORDENES + "/" + ordenId + "/pdf", { method: "POST" });
        mostrarToast("PDF generado (con marca de agua BORRADOR).", "success");
        await cargarOrdenesBorrador();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

async function tcVerPdf(ordenId) {
    try {
        const blob = await peticionApiBinaria(API.RUTAS.ORDENES + "/" + ordenId + "/pdf");
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

async function tcAprobarOrden(ordenId) {
    if (!window.confirm("¿Aprobar la orden #" + ordenId + "?")) {
        return;
    }
    try {
        await peticionApi(API.RUTAS.ORDENES + "/" + ordenId + "/estado", {
            method: "PATCH",
            body: JSON.stringify({ estado: "APROBADA" })
        });
        mostrarToast("Orden aprobada. Recíbela desde su detalle cuando llegue la mercancía.", "success");
        // La orden aprobada ya no es BORRADOR: se quita de esta tabla al recargar.
        await Promise.all([cargarOrdenesBorrador(), cargarKpis()]);
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}
