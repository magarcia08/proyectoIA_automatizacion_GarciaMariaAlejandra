let bodegasActuales = [];

async function iniciarBodegas() {
    configurarCierreModal("warehouseModal");
    document.getElementById("newWarehouseButton").classList.toggle("hidden", !esAdminOSuperior());
    document.getElementById("newWarehouseButton").addEventListener("click", abrirBodegaNueva);
    document.getElementById("warehouseForm").addEventListener("submit", guardarBodega);
    document.getElementById("warehouseSearch").addEventListener("input", filtrarBodegas);
    await cargarBodegas();
}

async function cargarBodegas() {
    try {
        bodegasActuales = await peticionApi(API.RUTAS.BODEGAS);
        if (!esModuloVigente("bodega")) {
            return;
        }
        renderizarBodegas(bodegasActuales);
    } catch (error) {
        if (!esModuloVigente("bodega")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarBodegas([]);
    }
}

function renderizarBodegas(lista) {
    const body = document.getElementById("warehouseTableBody");
    document.getElementById("warehouseTableFooter").textContent =
        lista.length + (lista.length === 1 ? " bodega registrada" : " bodegas registradas");

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="6"><div class="empty-state">No hay bodegas para mostrar.</div></td></tr>';
        return;
    }

    body.innerHTML = lista.map(item => `
        <tr>
            <td>#${item.id}</td>
            <td class="cell-title">${escaparHtml(item.nombre)}</td>
            <td>${escaparHtml(item.ubicacion)}</td>
            <td>${formatoNumero(item.capacidad)} unidades</td>
            <td>${escaparHtml(item.encargado)}</td>
            <td>
                ${esAdminOSuperior() ? `
                <div class="table-actions">
                    <button class="action-button" onclick="editarBodega(${item.id})" aria-label="Editar">✎</button>
                    <button class="action-button danger" onclick="eliminarBodega(${item.id})" aria-label="Eliminar">×</button>
                </div>` : '<span style="color:var(--text-muted,#6b7280);font-size:.85rem">—</span>'}
            </td>
        </tr>`).join("");
}

function filtrarBodegas(evento) {
    const texto = evento.target.value.toLowerCase();
    renderizarBodegas(bodegasActuales.filter(item =>
        item.nombre.toLowerCase().includes(texto) ||
        item.ubicacion.toLowerCase().includes(texto)
    ));
}

function abrirBodegaNueva() {
    document.getElementById("warehouseForm").reset();
    document.getElementById("warehouseId").value = "";
    document.getElementById("warehouseModalTitle").textContent = "Nueva bodega";
    abrirModal("warehouseModal");
}

function editarBodega(id) {
    const bodega = bodegasActuales.find(item => Number(item.id) === Number(id));
    if (!bodega) {
        return;
    }
    document.getElementById("warehouseId").value = bodega.id;
    document.getElementById("warehouseName").value = bodega.nombre;
    document.getElementById("warehouseLocation").value = bodega.ubicacion;
    document.getElementById("warehouseCapacity").value = bodega.capacidad;
    document.getElementById("warehouseManager").value = bodega.encargado;
    document.getElementById("warehouseModalTitle").textContent = "Editar bodega";
    abrirModal("warehouseModal");
}

async function guardarBodega(evento) {
    evento.preventDefault();
    const id = document.getElementById("warehouseId").value;
    const bodega = {
        nombre: document.getElementById("warehouseName").value.trim(),
        ubicacion: document.getElementById("warehouseLocation").value.trim(),
        capacidad: Number(document.getElementById("warehouseCapacity").value),
        encargado: document.getElementById("warehouseManager").value.trim()
    };
    try {
        await peticionApi(id ? API.RUTAS.BODEGAS + "/" + id : API.RUTAS.BODEGAS, {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(bodega)
        });
        cerrarModal("warehouseModal");
        mostrarToast("Bodega guardada correctamente.", "success");
        cargarBodegas();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

async function eliminarBodega(id) {
    if (!window.confirm("¿Deseas eliminar esta bodega?")) {
        return;
    }
    try {
        await peticionApi(API.RUTAS.BODEGAS + "/" + id, { method: "DELETE" });
        mostrarToast("Bodega eliminada.", "success");
        cargarBodegas();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}
