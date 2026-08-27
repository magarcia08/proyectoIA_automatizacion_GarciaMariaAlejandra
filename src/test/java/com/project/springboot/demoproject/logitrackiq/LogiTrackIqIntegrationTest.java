package com.project.springboot.demoproject.logitrackiq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.logitrackiq.support.AbstractLogiTrackIqTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de integracion (HTTP real, con JWT real, a traves de
 * SecurityFilterChain) requerida por el enunciado para
 * PATCH /ordenes/{id}/estado y POST /panel/resumen. Cubre ademas las
 * reglas obligatorias 3, 6 y la mitad HTTP de la 7 (ver
 * docs/enunciado-logitrack-iq.md). Escrita ANTES de implementar los
 * controladores/servicios correspondientes.
 */
@AutoConfigureMockMvc
class LogiTrackIqIntegrationTest extends AbstractLogiTrackIqTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtenerToken(String username) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("username", username, "password", "clave123"));
        String respuesta = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("token").asText();
    }

    @Test
    void agenteIntentaAprobarOrden_devuelve403() throws Exception {
        Usuario admin = crearUsuario(Rol.ADMIN);
        Usuario agente = crearUsuario(Rol.AGENTE);
        Proveedor proveedor = crearProveedor(7);
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);

        String tokenAdmin = obtenerToken(admin.getUsername());
        String cuerpoOrden = objectMapper.writeValueAsString(java.util.Map.of(
                "productoId", producto.getId(),
                "proveedorId", proveedor.getId(),
                "bodegaDestinoId", bodega.getId(),
                "cantidad", 10,
                "precioUnitario", 1000));

        String respuestaOrden = mockMvc.perform(post("/ordenes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoOrden))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long ordenId = objectMapper.readTree(respuestaOrden).get("id").asLong();

        String tokenAgente = obtenerToken(agente.getUsername());
        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .header("Authorization", "Bearer " + tokenAgente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"APROBADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearOrden_conCantidadInvalida_devuelve400() throws Exception {
        Usuario agente = crearUsuario(Rol.AGENTE);
        Proveedor proveedor = crearProveedor(7);
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);
        String token = obtenerToken(agente.getUsername());

        String cuerpo = objectMapper.writeValueAsString(java.util.Map.of(
                "productoId", producto.getId(),
                "proveedorId", proveedor.getId(),
                "bodegaDestinoId", bodega.getId(),
                "cantidad", 0,
                "precioUnitario", 1000));

        mockMvc.perform(post("/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicarResumen_conSeveridadInvalida_devuelve400() throws Exception {
        Usuario agente = crearUsuario(Rol.AGENTE);
        String token = obtenerToken(agente.getUsername());

        String cuerpo = """
                {
                  "fecha": "%s",
                  "narrativa": "Narrativa de prueba con longitud valida de mas de veinte caracteres.",
                  "alertas": [
                    {"severidad": "URGENTE", "titulo": "x", "detalle": "y", "productoId": 1}
                  ],
                  "accionesSugeridas": []
                }
                """.formatted(java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota")));

        mockMvc.perform(post("/panel/resumen")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void kpis_requiereAutenticacion() throws Exception {
        // Sin JWT, Spring Security trata la peticion como anonima y la
        // rechaza con 403 (mismo comportamiento ya establecido por el resto
        // de endpoints protegidos de la aplicacion; no hay AuthenticationEntryPoint
        // personalizado que distinga "sin token" de "token invalido").
        mockMvc.perform(get("/kpis")).andExpect(status().isForbidden());
    }
}
