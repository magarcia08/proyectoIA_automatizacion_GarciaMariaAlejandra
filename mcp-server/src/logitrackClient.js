// Cliente HTTP minimo hacia la API real de LogiTrack. No accede a la base
// de datos ni implementa reglas de negocio: solo llama endpoints ya
// protegidos por Spring Security, autenticado como un usuario con rol
// AGENTE (ver docs/enunciado-logitrack-iq.md, seccion 7).

const BASE_URL = process.env.LOGITRACK_API_URL || "http://localhost:8085";
const USERNAME = process.env.LOGITRACK_AGENTE_USERNAME || "agente";
const PASSWORD = process.env.LOGITRACK_AGENTE_PASSWORD || "agente123";

let tokenCache = null;

async function login() {
    const respuesta = await fetch(`${BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: USERNAME, password: PASSWORD }),
    });
    if (!respuesta.ok) {
        throw new Error(`No se pudo autenticar como '${USERNAME}' (rol AGENTE) contra ${BASE_URL}: HTTP ${respuesta.status}`);
    }
    const datos = await respuesta.json();
    tokenCache = datos.token;
    return tokenCache;
}

async function peticion(path, options = {}, permitirReintento = true) {
    if (!tokenCache) {
        await login();
    }
    const respuesta = await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${tokenCache}`,
            ...(options.headers || {}),
        },
    });

    if (respuesta.status === 401 && permitirReintento) {
        // El token pudo expirar: se descarta y se reintenta una vez con uno nuevo.
        tokenCache = null;
        return peticion(path, options, false);
    }

    const texto = await respuesta.text();
    let cuerpo = null;
    if (texto) {
        try {
            cuerpo = JSON.parse(texto);
        } catch {
            cuerpo = texto;
        }
    }

    if (!respuesta.ok) {
        const mensaje = cuerpo && typeof cuerpo === "object" && cuerpo.message ? cuerpo.message : `HTTP ${respuesta.status}`;
        const error = new Error(mensaje);
        error.status = respuesta.status;
        error.body = cuerpo;
        throw error;
    }
    return cuerpo;
}

export function get(path) {
    return peticion(path, { method: "GET" });
}

export function post(path, body) {
    return peticion(path, { method: "POST", body: JSON.stringify(body) });
}
