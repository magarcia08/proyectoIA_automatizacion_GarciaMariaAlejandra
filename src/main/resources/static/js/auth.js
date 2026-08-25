document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");
    const toggle = document.getElementById("togglePassword");
    const demoButton = document.getElementById("demoButton");

    toggle.addEventListener("click", function () {
        const password = document.getElementById("password");
        const mostrar = password.type === "password";
        password.type = mostrar ? "text" : "password";
        toggle.textContent = mostrar ? "Ocultar" : "Ver";
    });

    if (demoButton) {
        demoButton.classList.add("hidden");
    }
    form.addEventListener("submit", iniciarSesion);
});

async function iniciarSesion(evento) {
    evento.preventDefault();
    limpiarErroresLogin();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    let valido = true;

    if (!username) {
        document.getElementById("usernameError").textContent = "Ingresa tu usuario.";
        valido = false;
    }
    if (password.length < 6) {
        document.getElementById("passwordError").textContent = "La contraseña debe tener mínimo 6 caracteres.";
        valido = false;
    }
    if (!valido) {
        return;
    }

    const button = document.getElementById("loginButton");
    button.disabled = true;
    button.textContent = "Ingresando...";

    try {
        const respuesta = await peticionApi(API.RUTAS.LOGIN, {
            method: "POST",
            body: JSON.stringify({ username, password })
        });
        guardarToken(respuesta.token);
        guardarUsuario({ username: respuesta.username, rol: respuesta.rol });
        window.location.href = "dashboard.html";
    } catch (error) {
        mostrarAlertaLogin(error.message, "error");
    } finally {
        button.disabled = false;
        button.textContent = "Iniciar sesión";
    }
}

function limpiarErroresLogin() {
    document.getElementById("usernameError").textContent = "";
    document.getElementById("passwordError").textContent = "";
    document.getElementById("loginAlert").className = "alert hidden";
}

function mostrarAlertaLogin(mensaje, tipo) {
    const alerta = document.getElementById("loginAlert");
    alerta.textContent = mensaje;
    alerta.className = "alert " + (tipo === "error" ? "alert-error" : "alert-success");
}
