/*
 * Configuración central del frontend. Como este frontend ahora se sirve
 * directamente desde el propio backend de Spring Boot (src/main/resources/static),
 * las peticiones van al mismo origen (BASE_URL vacío = mismo host/puerto).
 * Si sirves el frontend por separado (Live Server, etc.), pon aquí la URL
 * completa del backend, por ejemplo "http://localhost:8080".
 */
const API = {
    BASE_URL: "",
    MODO_DEMO: false,
    RUTAS: {
        LOGIN: "/auth/login",
        REGISTRO: "/auth/register",
        USUARIOS: "/usuarios",
        BODEGAS: "/bodegas",
        PRODUCTOS: "/productos",
        INVENTARIO: "/inventario",
        MOVIMIENTOS: "/movimientos",
        AUDITORIAS: "/auditorias",
        REPORTES: "/reportes",
        // LogiTrack IQ
        KPIS: "/kpis",
        PRODUCTOS_RIESGO: "/productos/riesgo",
        BODEGAS_CRITICAS: "/bodegas/criticas",
        PROVEEDORES: "/proveedores",
        ORDENES: "/ordenes",
        PANEL_RESUMEN: "/panel/resumen"
    }
};

const TOKEN_KEY = "logitrack_token";
const USER_KEY = "logitrack_usuario";

// LogiTrack IQ exige que el JWT se guarde SOLO en sessionStorage (nunca
// localStorage): se pierde al cerrar la pestaña/navegador, reduciendo la
// ventana de exposición del token frente al reto anterior.
function guardarToken(token) {
    sessionStorage.setItem(TOKEN_KEY, token);
}

function obtenerToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}

function eliminarToken() {
    sessionStorage.removeItem(TOKEN_KEY);
}

function guardarUsuario(usuario) {
    sessionStorage.setItem(USER_KEY, JSON.stringify(usuario));
}

function obtenerUsuario() {
    const usuario = sessionStorage.getItem(USER_KEY);
    return usuario ? JSON.parse(usuario) : null;
}

function eliminarUsuario() {
    sessionStorage.removeItem(USER_KEY);
}

/**
 * Llama al backend real de LogiTrack. Agrega automáticamente el header
 * Authorization con el token JWT guardado tras el login (excepto en la
 * propia petición de login, para no arrastrar un token viejo/inválido).
 */
async function peticionApi(ruta, opciones = {}) {
    const headers = { "Content-Type": "application/json", ...(opciones.headers || {}) };
    const esLogin = ruta === API.RUTAS.LOGIN;
    const token = obtenerToken();
    if (token && !esLogin) {
        headers.Authorization = "Bearer " + token;
    }

    const respuesta = await fetch(API.BASE_URL + ruta, { ...opciones, headers });
    if (!respuesta.ok) {
        let mensaje = "No fue posible completar la solicitud.";
        try {
            const error = await respuesta.json();
            mensaje = error.message || mensaje;
            if (Array.isArray(error.detalles) && error.detalles.length > 0) {
                mensaje += ": " + error.detalles.join("; ");
            }
        } catch (e) {
            // El backend todavía puede devolver errores sin cuerpo JSON.
        }
        throw new Error(mensaje);
    }
    if (respuesta.status === 204) {
        return null;
    }
    return respuesta.json();
}

/**
 * Igual que peticionApi, pero para respuestas binarias (el PDF de una
 * orden): no se puede usar un <a href="..."> directo porque el endpoint
 * exige el header Authorization. Devuelve un Blob listo para mostrarse
 * en una pestaña nueva (URL.createObjectURL) o descargarse.
 */
async function peticionApiBinaria(ruta, opciones = {}) {
    const headers = { ...(opciones.headers || {}) };
    const token = obtenerToken();
    if (token) {
        headers.Authorization = "Bearer " + token;
    }
    const respuesta = await fetch(API.BASE_URL + ruta, { ...opciones, headers });
    if (!respuesta.ok) {
        let mensaje = "No fue posible completar la solicitud.";
        try {
            const error = await respuesta.json();
            mensaje = error.message || mensaje;
        } catch (e) {
            // sin cuerpo JSON
        }
        throw new Error(mensaje);
    }
    return respuesta.blob();
}