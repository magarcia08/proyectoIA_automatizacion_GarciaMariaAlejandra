document.addEventListener("DOMContentLoaded", iniciarDashboard);

// Nombre del módulo que está actualmente cargado en #mainContent.
// Los módulos (producto.js, movimiento.js, etc.) hacen peticiones async
// antes de tocar el DOM; si el usuario navega a otro módulo mientras esas
// peticiones siguen pendientes, el HTML de este módulo ya no existe cuando
// la promesa resuelve. Cada inicializador debe llamar a esModuloVigente(nombre)
// despues de cada await y abortar si ya no es el modulo activo.
let moduloCargadoActualmente = null;

function esModuloVigente(nombre) {
    return moduloCargadoActualmente === nombre;
}

function iniciarDashboard() {
    prepararSesion();
    mostrarUsuario();
    aplicarPermisosNavegacion();
    configurarNavegacion();
    configurarMenuMovil();
    document.getElementById("logoutButton").addEventListener("click", cerrarSesion);
    mostrarInicio();
}

function aplicarPermisosNavegacion() {
    const usuario = obtenerUsuario() || { rol: "EMPLEADO" };
    const esAdmin = usuario.rol === "ADMIN" || usuario.rol === "SUPERADMIN";
    document.getElementById("usuarioNavItem").classList.toggle("hidden", !esAdmin);
    document.getElementById("adminSectionLabel").classList.toggle("hidden", !esAdmin);
    document.getElementById("auditoriaNavItem").classList.toggle("hidden", !esAdmin);
}

function prepararSesion() {
    if (!obtenerToken()) {
        window.location.href = "login.html";
    }
}

function mostrarUsuario() {
    const usuario = obtenerUsuario() || { username: "Usuario", rol: "EMPLEADO" };
    const nombre = usuario.username || "Usuario";
    document.getElementById("userName").textContent = nombre;
    document.getElementById("userRole").textContent = usuario.rol || "EMPLEADO";
    document.getElementById("userInitial").textContent = nombre.charAt(0).toUpperCase();

    const estado = document.getElementById("apiState");
    estado.innerHTML = '<span class="status-dot"></span><span>Backend conectado</span>';
}

function configurarNavegacion() {
    document.querySelectorAll(".nav-item").forEach(button => {
        button.addEventListener("click", function () {
            abrirModulo(button.dataset.module);
        });
    });
}

function configurarMenuMovil() {
    const sidebar = document.getElementById("sidebar");
    const overlay = document.getElementById("sidebarOverlay");
    const cerrar = () => sidebar.classList.remove("open");

    document.getElementById("menuButton").addEventListener("click", () => {
        sidebar.classList.add("open");
    });
    document.getElementById("sidebarClose").addEventListener("click", cerrar);
    overlay.addEventListener("click", cerrar);
}

function marcarModuloActivo(nombre) {
    document.querySelectorAll(".nav-item").forEach(button => {
        button.classList.toggle("active", button.dataset.module === nombre);
    });
    document.getElementById("sidebar").classList.remove("open");
}

async function abrirModulo(nombre) {
    if (nombre === "inicio") {
        mostrarInicio();
        return;
    }

    const titulos = {
        producto: "Gestión de productos",
        bodega: "Gestión de bodegas",
        inventario: "Inventario por bodega",
        movimiento: "Movimientos",
        auditoria: "Auditoría",
        reporte: "Reportes",
        usuario: "Gestión de usuarios"
    };

    moduloCargadoActualmente = nombre;
    marcarModuloActivo(nombre);
    document.getElementById("pageTitle").textContent = titulos[nombre];
    const contenido = document.getElementById("mainContent");
    contenido.innerHTML = '<div class="loading-state"><div><div class="spinner"></div>Cargando módulo...</div></div>';

    try {
        const respuesta = await fetch("pages/" + nombre + ".html");
        if (!respuesta.ok) {
            throw new Error("No se encontró el módulo.");
        }
        const html = await respuesta.text();
        // Si el usuario ya navego a otro modulo mientras esperabamos el
        // fetch, no pisamos su pantalla actual con este HTML desactualizado.
        if (!esModuloVigente(nombre)) {
            return;
        }
        contenido.innerHTML = html;
        iniciarModulo(nombre);
    } catch (error) {
        if (!esModuloVigente(nombre)) {
            return;
        }
        contenido.innerHTML = `
            <div class="panel empty-state">
                <strong>No fue posible cargar el módulo</strong>
                <span>Abre el frontend con Live Server o un servidor local.</span>
            </div>`;
    }
}

function iniciarModulo(nombre) {
    const inicializadores = {
        producto: iniciarProductos,
        bodega: iniciarBodegas,
        inventario: iniciarInventario,
        movimiento: iniciarMovimientos,
        auditoria: iniciarAuditorias,
        reporte: iniciarReportes,
        usuario: iniciarUsuarios
    };
    if (inicializadores[nombre]) {
        inicializadores[nombre]();
    }
}

async function mostrarInicio() {
    moduloCargadoActualmente = "inicio";
    marcarModuloActivo("inicio");
    document.getElementById("pageTitle").textContent = "Resumen general";
    const contenedor = document.getElementById("mainContent");
    contenedor.innerHTML = '<div class="loading-state"><div><div class="spinner"></div>Cargando resumen...</div></div>';

    try {
        const [resumen, productos, movimientos] = await Promise.all([
            peticionApi(API.RUTAS.REPORTES + "/resumen"),
            peticionApi(API.RUTAS.PRODUCTOS),
            peticionApi(API.RUTAS.MOVIMIENTOS)
        ]);

        const stockTotal = resumen.stockPorBodega.reduce((suma, item) => suma + Number(item.stockTotal), 0);
        const productosBajos = productos.filter(item => Number(item.stockTotal) < 10).length;

        contenedor.innerHTML = `
            <div class="page-head fade-in">
                <div>
                    <h2>Así está la operación</h2>
                    <p>Indicadores principales de inventario y movimientos.</p>
                </div>
                <button class="btn btn-secondary" onclick="abrirModulo('reporte')">Ver reportes</button>
            </div>
            <div class="kpi-grid fade-in">
                ${tarjetaKpi("□", "Productos", resumen.totalProductos, productosBajos + " requieren atención", "#dbeafe", "#1d4ed8")}
                ${tarjetaKpi("▥", "Bodegas", resumen.totalBodegas, "bodegas registradas", "#ede9fe", "#7c3aed")}
                ${tarjetaKpi("▦", "Inventario total", formatoNumero(stockTotal), "unidades disponibles", "#dcfce7", "#16a34a")}
                ${tarjetaKpi("⇄", "Movimientos", resumen.totalMovimientos, "registrados en total", "#fef3c7", "#d97706")}
            </div>
            <div class="dashboard-grid fade-in">
                <article class="panel">
                    <div class="panel-head">
                        <h3>Inventario por bodega</h3>
                        <span>Unidades registradas</span>
                    </div>
                    <div class="warehouse-bars">${barrasBodegas(resumen)}</div>
                </article>
                <article class="panel">
                    <div class="panel-head">
                        <h3>Actividad reciente</h3>
                        <span>Últimos movimientos</span>
                    </div>
                    <div class="activity-list">${actividadReciente(movimientos)}</div>
                </article>
            </div>`;
    } catch (error) {
        contenedor.innerHTML = `<div class="panel empty-state"><strong>No fue posible cargar el resumen</strong><span>${escaparHtml(error.message)}</span></div>`;
    }
}

function tarjetaKpi(icono, etiqueta, valor, detalle, acento, color) {
    return `
        <article class="kpi-card" style="--card-accent:${acento};--card-color:${color}">
            <div class="kpi-label"><span class="kpi-icon">${icono}</span>${etiqueta}</div>
            <div class="kpi-value">${valor}</div>
            <div class="kpi-detail">${detalle}</div>
        </article>`;
}

function barrasBodegas(resumen) {
    const totales = resumen.stockPorBodega.map(item => ({ nombre: item.bodegaNombre, total: Number(item.stockTotal) }));
    const maximo = Math.max(...totales.map(item => item.total), 1);
    return totales.map(item => `
        <div class="warehouse-row">
            <strong>${escaparHtml(item.nombre)}</strong>
            <div class="bar-track"><span class="bar-fill" style="width:${(item.total / maximo) * 100}%"></span></div>
            <span>${formatoNumero(item.total)}</span>
        </div>`).join("");
}

function actividadReciente(movimientos) {
    if (!movimientos || movimientos.length === 0) {
        return '<div class="empty-state">Aún no hay movimientos registrados.</div>';
    }
    return movimientos.slice(-3).reverse().map(movimiento => {
        const clase = movimiento.tipo === "ENTRADA" ? "badge-green"
            : movimiento.tipo === "SALIDA" ? "badge-red" : "badge-blue";
        return `
            <div class="activity-item">
                <span class="activity-icon ${clase}">⇄</span>
                <div class="activity-copy">
                    <strong>${movimiento.tipo}</strong>
                    <p>${escaparHtml(movimiento.usuarioUsername || "")}</p>
                    <small>${formatoFecha(movimiento.fecha)}</small>
                </div>
            </div>`;
    }).join("");
}

function cerrarSesion() {
    eliminarToken();
    eliminarUsuario();
    window.location.href = "login.html";
}
