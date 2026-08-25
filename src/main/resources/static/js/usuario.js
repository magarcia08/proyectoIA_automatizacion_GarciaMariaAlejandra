let usuariosActuales = [];

async function iniciarUsuarios() {
    configurarCierreModal("userModal");
    document.getElementById("newUserButton").addEventListener("click", abrirUsuarioNuevo);
    document.getElementById("userForm").addEventListener("submit", guardarUsuario);
    document.getElementById("userSearch").addEventListener("input", filtrarUsuarios);
    document.getElementById("userRoleFilter").addEventListener("change", filtrarUsuarios);
    llenarSelectRolDisponible();
    await cargarUsuarios();
}

/**
 * Un ADMIN solo puede crear EMPLEADO; un SUPERADMIN puede crear ADMIN o EMPLEADO
 * (la validación real y definitiva vive en el backend, esto es solo para no
 * mostrar opciones que el backend va a rechazar de todas formas).
 */
function llenarSelectRolDisponible() {
    const usuarioActual = obtenerUsuario() || {};
    const select = document.getElementById("newRol");
    const hint = document.getElementById("rolHint");

    if (usuarioActual.rol === "SUPERADMIN") {
        select.innerHTML = '<option value="ADMIN">ADMIN</option><option value="EMPLEADO">EMPLEADO</option>';
        hint.textContent = "Como SUPERADMIN puedes crear cuentas ADMIN o EMPLEADO.";
    } else {
        select.innerHTML = '<option value="EMPLEADO">EMPLEADO</option>';
        hint.textContent = "Como ADMIN solo puedes crear cuentas EMPLEADO.";
    }
}

async function cargarUsuarios() {
    try {
        usuariosActuales = await peticionApi(API.RUTAS.USUARIOS);
        if (!esModuloVigente("usuario")) {
            return;
        }
        renderizarUsuarios(usuariosActuales);
    } catch (error) {
        if (!esModuloVigente("usuario")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarUsuarios([]);
    }
}

function filtrarUsuarios() {
    const texto = document.getElementById("userSearch").value.toLowerCase();
    const rol = document.getElementById("userRoleFilter").value;
    const filtrados = usuariosActuales.filter(item => {
        const coincideTexto = item.username.toLowerCase().includes(texto) ||
            item.email.toLowerCase().includes(texto);
        return coincideTexto && (!rol || item.rol === rol);
    });
    renderizarUsuarios(filtrados);
}

function renderizarUsuarios(lista) {
    const body = document.getElementById("userTableBody");
    document.getElementById("userTableFooter").textContent =
        lista.length + (lista.length === 1 ? " usuario registrado" : " usuarios registrados");

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="7"><div class="empty-state">No hay usuarios para mostrar.</div></td></tr>';
        return;
    }

    body.innerHTML = lista.map(item => {
        const claseRol = item.rol === "SUPERADMIN" ? "badge-red" : item.rol === "ADMIN" ? "badge-blue" : "badge-green";
        const claseEstado = item.activo ? "badge-green" : "badge-red";
        const textoEstado = item.activo ? "Activo" : "Inactivo";
        const puedeCambiarEstado = item.rol !== "SUPERADMIN";
        return `
            <tr>
                <td>#${item.id}</td>
                <td class="cell-title">${escaparHtml(item.username)}</td>
                <td>${escaparHtml(item.email)}</td>
                <td><span class="badge ${claseRol}">${item.rol}</span></td>
                <td><span class="badge ${claseEstado}">${textoEstado}</span></td>
                <td>${formatoFecha(item.creadoEn)}</td>
                <td>
                    ${puedeCambiarEstado
                        ? `<div class="table-actions">
                             <button class="action-button" onclick="cambiarEstadoUsuario(${item.id}, ${!item.activo})">
                                 ${item.activo ? "Desactivar" : "Activar"}
                             </button>
                           </div>`
                        : '<span style="color:var(--text-muted,#6b7280);font-size:.85rem">—</span>'}
                </td>
            </tr>`;
    }).join("");
}

function abrirUsuarioNuevo() {
    document.getElementById("userForm").reset();
    llenarSelectRolDisponible();
    abrirModal("userModal");
}

async function guardarUsuario(evento) {
    evento.preventDefault();
    const usuario = {
        username: document.getElementById("newUsername").value.trim(),
        email: document.getElementById("newEmail").value.trim(),
        password: document.getElementById("newPassword").value,
        rol: document.getElementById("newRol").value
    };

    try {
        await peticionApi(API.RUTAS.REGISTRO, {
            method: "POST",
            body: JSON.stringify(usuario)
        });
        cerrarModal("userModal");
        mostrarToast("Usuario creado correctamente.", "success");
        await cargarUsuarios();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

async function cambiarEstadoUsuario(id, nuevoEstado) {
    try {
        await peticionApi(API.RUTAS.USUARIOS + "/" + id + "/estado?activo=" + nuevoEstado, {
            method: "PATCH"
        });
        mostrarToast(nuevoEstado ? "Usuario activado." : "Usuario desactivado.", "success");
        await cargarUsuarios();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}
