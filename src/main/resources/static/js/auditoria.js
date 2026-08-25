let auditoriasActuales = [];

async function iniciarAuditorias() {
    configurarCierreModal("auditDetailModal");
    document.getElementById("auditSearch").addEventListener("input", filtrarAuditorias);
    document.getElementById("auditTypeFilter").addEventListener("change", aplicarFiltrosAuditoria);
    document.getElementById("auditUserFilter").addEventListener("change", aplicarFiltrosAuditoria);

    await llenarFiltroUsuarios();
    await cargarAuditorias();
}

async function llenarFiltroUsuarios() {
    try {
        const usuarios = await peticionApi(API.RUTAS.USUARIOS);
        if (!esModuloVigente("auditoria")) {
            return;
        }
        const select = document.getElementById("auditUserFilter");
        select.innerHTML = '<option value="">Todos los usuarios</option>' +
            usuarios.map(u => `<option value="${u.id}">${escaparHtml(u.username)} (${u.rol})</option>`).join("");
    } catch (error) {
        // Si el usuario logueado no tiene permisos de ADMIN esto no debería pasar,
        // pero si falla, simplemente dejamos el filtro vacío sin romper la página.
    }
}

async function cargarAuditorias() {
    try {
        auditoriasActuales = await peticionApi(API.RUTAS.AUDITORIAS);
        if (!esModuloVigente("auditoria")) {
            return;
        }
        renderizarAuditorias(auditoriasActuales);
    } catch (error) {
        if (!esModuloVigente("auditoria")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarAuditorias([]);
    }
}

/** Consulta avanzada: auditorías por usuario o por tipo de operación, contra el backend. */
async function aplicarFiltrosAuditoria() {
    const usuarioId = document.getElementById("auditUserFilter").value;
    const tipo = document.getElementById("auditTypeFilter").value;

    try {
        if (usuarioId) {
            auditoriasActuales = await peticionApi(API.RUTAS.AUDITORIAS + "?usuarioId=" + usuarioId);
        } else if (tipo) {
            auditoriasActuales = await peticionApi(API.RUTAS.AUDITORIAS + "?tipoOperacion=" + tipo);
        } else {
            auditoriasActuales = await peticionApi(API.RUTAS.AUDITORIAS);
        }
        if (!esModuloVigente("auditoria")) {
            return;
        }
        filtrarAuditorias();
    } catch (error) {
        if (!esModuloVigente("auditoria")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

function filtrarAuditorias() {
    const texto = document.getElementById("auditSearch").value.toLowerCase();
    const tipo = document.getElementById("auditTypeFilter").value;
    const filtradas = auditoriasActuales.filter(item => {
        const coincideTexto = item.entidadAfectada.toLowerCase().includes(texto) ||
            String(item.usuarioUsername || "").toLowerCase().includes(texto);
        return coincideTexto && (!tipo || item.tipoOperacion === tipo);
    });
    renderizarAuditorias(filtradas);
}

function renderizarAuditorias(lista) {
    const body = document.getElementById("auditTableBody");
    document.getElementById("auditTableFooter").textContent =
        lista.length + (lista.length === 1 ? " operación registrada" : " operaciones registradas");

    if (lista.length === 0) {
        body.innerHTML = `
            <tr><td colspan="7">
                <div class="empty-state">
                    <strong>No hay auditorías disponibles</strong>
                    Aún no se han registrado cambios en el sistema.
                </div>
            </td></tr>`;
        return;
    }

    // Guardamos la lista renderizada para poder ubicarla por id al abrir el detalle.
    window.__auditoriasRenderizadas = lista;

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
                    <button class="action-button" onclick="verDetalleAuditoria(${item.id})">Ver</button>
                </td>
            </tr>`;
    }).join("");
}

function verDetalleAuditoria(id) {
    const registro = (window.__auditoriasRenderizadas || []).find(item => Number(item.id) === Number(id));
    if (!registro) {
        return;
    }
    document.getElementById("auditDetailBefore").textContent = formatearJsonAuditoria(registro.valoresAnteriores);
    document.getElementById("auditDetailAfter").textContent = formatearJsonAuditoria(registro.valoresNuevos);
    abrirModal("auditDetailModal");
}

function formatearJsonAuditoria(valor) {
    if (!valor) {
        return "— (no aplica para esta operación)";
    }
    try {
        return JSON.stringify(JSON.parse(valor), null, 2);
    } catch (error) {
        return valor;
    }
}
