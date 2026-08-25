function formatoMoneda(valor) {
    return new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        maximumFractionDigits: 0
    }).format(Number(valor || 0));
}

function formatoNumero(valor) {
    return new Intl.NumberFormat("es-CO").format(Number(valor || 0));
}

function formatoFecha(valor) {
    if (!valor) {
        return "Sin fecha";
    }
    return new Intl.DateTimeFormat("es-CO", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(valor));
}

function escaparHtml(valor) {
    const elemento = document.createElement("div");
    elemento.textContent = valor == null ? "" : String(valor);
    return elemento.innerHTML;
}

function mostrarToast(mensaje, tipo = "normal") {
    const contenedor = document.getElementById("toastContainer");
    if (!contenedor) {
        return;
    }
    const toast = document.createElement("div");
    toast.className = "toast fade-in";
    if (tipo === "error") {
        toast.style.background = "#991b1b";
    }
    if (tipo === "success") {
        toast.style.background = "#166534";
    }
    toast.textContent = mensaje;
    contenedor.appendChild(toast);
    setTimeout(() => toast.remove(), 3200);
}

function abrirModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.add("open");
        modal.setAttribute("aria-hidden", "false");
    }
}

function cerrarModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove("open");
        modal.setAttribute("aria-hidden", "true");
    }
}

function configurarCierreModal(id) {
    const modal = document.getElementById(id);
    if (!modal) {
        return;
    }
    modal.querySelectorAll(".modal-close").forEach(button => {
        button.addEventListener("click", () => cerrarModal(id));
    });
    modal.addEventListener("click", event => {
        if (event.target === modal) {
            cerrarModal(id);
        }
    });
}

function estadoStock(stock) {
    const cantidad = Number(stock);
    if (cantidad < 10) {
        return { texto: "Crítico", clase: "badge-red" };
    }
    if (cantidad < 25) {
        return { texto: "Bajo", clase: "badge-amber" };
    }
    return { texto: "Disponible", clase: "badge-green" };
}

function obtenerNombrePorId(lista, id, campo = "nombre") {
    const item = lista.find(elemento => Number(elemento.id) === Number(id));
    return item ? item[campo] : "No aplica";
}

/** true si el usuario logueado es ADMIN o SUPERADMIN (permisos de escritura). */
function esAdminOSuperior() {
    const usuario = obtenerUsuario() || {};
    return usuario.rol === "ADMIN" || usuario.rol === "SUPERADMIN";
}
