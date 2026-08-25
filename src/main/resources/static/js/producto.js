let productosActuales = [];

async function iniciarProductos() {
    configurarCierreModal("productModal");
    document.getElementById("newProductButton").classList.toggle("hidden", !esAdminOSuperior());
    document.getElementById("newProductButton").addEventListener("click", abrirProductoNuevo);
    document.getElementById("productForm").addEventListener("submit", guardarProducto);
    document.getElementById("productSearch").addEventListener("input", filtrarProductos);
    await cargarProductos();
}

async function cargarProductos() {
    try {
        productosActuales = await peticionApi(API.RUTAS.PRODUCTOS);
        if (!esModuloVigente("producto")) {
            return;
        }
        renderizarProductos(productosActuales);
    } catch (error) {
        if (!esModuloVigente("producto")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarProductos([]);
    }
}

function renderizarProductos(lista) {
    const body = document.getElementById("productTableBody");
    const bajos = lista.filter(item => Number(item.stockTotal) < 10).length;
    document.getElementById("lowStockCount").textContent = bajos + " con stock bajo";
    document.getElementById("productTableFooter").textContent =
        lista.length + (lista.length === 1 ? " producto registrado" : " productos registrados");

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="6"><div class="empty-state">No hay productos para mostrar.</div></td></tr>';
        return;
    }

    body.innerHTML = lista.map(item => {
        const estado = estadoStock(item.stockTotal);
        return `
            <tr>
                <td>#${item.id}</td>
                <td class="cell-title">${escaparHtml(item.nombre)}</td>
                <td><span class="badge badge-blue">${escaparHtml(item.categoria)}</span></td>
                <td><span class="badge ${estado.clase}">${item.stockTotal} · ${estado.texto}</span></td>
                <td>${formatoMoneda(item.precio)}</td>
                <td>
                    ${esAdminOSuperior() ? `
                    <div class="table-actions">
                        <button class="action-button" onclick="editarProducto(${item.id})" aria-label="Editar">✎</button>
                        <button class="action-button danger" onclick="eliminarProducto(${item.id})" aria-label="Eliminar">×</button>
                    </div>` : '<span style="color:var(--text-muted,#6b7280);font-size:.85rem">—</span>'}
                </td>
            </tr>`;
    }).join("");
}

function filtrarProductos(evento) {
    const texto = evento.target.value.toLowerCase();
    const filtrados = productosActuales.filter(item =>
        item.nombre.toLowerCase().includes(texto) ||
        item.categoria.toLowerCase().includes(texto)
    );
    renderizarProductos(filtrados);
}

function abrirProductoNuevo() {
    document.getElementById("productForm").reset();
    document.getElementById("productId").value = "";
    document.getElementById("productModalTitle").textContent = "Nuevo producto";
    abrirModal("productModal");
}

function editarProducto(id) {
    const producto = productosActuales.find(item => Number(item.id) === Number(id));
    if (!producto) {
        return;
    }
    document.getElementById("productId").value = producto.id;
    document.getElementById("productName").value = producto.nombre;
    document.getElementById("productCategory").value = producto.categoria;
    document.getElementById("productPrice").value = producto.precio;
    document.getElementById("productModalTitle").textContent = "Editar producto";
    abrirModal("productModal");
}

async function guardarProducto(evento) {
    evento.preventDefault();
    const id = document.getElementById("productId").value;
    const producto = {
        nombre: document.getElementById("productName").value.trim(),
        categoria: document.getElementById("productCategory").value.trim(),
        precio: Number(document.getElementById("productPrice").value)
    };

    try {
        await peticionApi(id ? API.RUTAS.PRODUCTOS + "/" + id : API.RUTAS.PRODUCTOS, {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(producto)
        });
        cerrarModal("productModal");
        mostrarToast("Producto guardado correctamente.", "success");
        cargarProductos();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

async function eliminarProducto(id) {
    if (!window.confirm("¿Deseas eliminar este producto?")) {
        return;
    }
    try {
        await peticionApi(API.RUTAS.PRODUCTOS + "/" + id, { method: "DELETE" });
        mostrarToast("Producto eliminado.", "success");
        cargarProductos();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}
