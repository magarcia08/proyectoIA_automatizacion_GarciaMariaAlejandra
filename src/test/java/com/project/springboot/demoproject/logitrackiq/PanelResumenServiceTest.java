package com.project.springboot.demoproject.logitrackiq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.springboot.demoproject.dto.logitrackiq.AlertaDto;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelRequest;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.enums.Severidad;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.logitrackiq.support.AbstractLogiTrackIqTest;
import com.project.springboot.demoproject.services.PanelResumenService;

/**
 * Regla obligatoria 7 de docs/enunciado-logitrack-iq.md (parte de
 * "id inexistente"; la parte de "severidad invalida" se prueba a nivel
 * HTTP en LogiTrackIqIntegrationTest, porque un enum Java no puede
 * representar un valor invalido). Escrita ANTES de implementar
 * PanelResumenService.
 */
class PanelResumenServiceTest extends AbstractLogiTrackIqTest {

    @Autowired
    private PanelResumenService panelResumenService;

    private ResumenPanelRequest requestConAlertaDeBodegaInexistente(Long bodegaIdInexistente) {
        ResumenPanelRequest req = new ResumenPanelRequest();
        req.setFecha(LocalDate.now(ZoneId.of("America/Bogota")));
        req.setNarrativa("Narrativa de prueba con al menos veinte caracteres validos.");

        AlertaDto alerta = new AlertaDto();
        alerta.setSeveridad(Severidad.ALTA);
        alerta.setTitulo("Bodega inexistente");
        alerta.setDetalle("Esta alerta referencia una bodega que no existe.");
        alerta.setBodegaId(bodegaIdInexistente);
        req.setAlertas(List.of(alerta));
        req.setAccionesSugeridas(List.of());
        return req;
    }

    @Test
    void idInexistenteEnAlerta_seRechazaYConservaElResumenAnterior() {
        Usuario agente = crearUsuario(Rol.AGENTE);
        Bodega bodegaReal = crearBodega(1000);
        autenticarComo(agente);

        // 1) Se publica un resumen VALIDO primero.
        ResumenPanelRequest primero = requestConAlertaDeBodegaInexistente(bodegaReal.getId());
        ResumenPanelResponse publicado = panelResumenService.publicar(primero);
        assertThat(publicado.getFecha()).isEqualTo(primero.getFecha());

        // 2) Se intenta publicar un segundo resumen con un bodegaId que NO existe.
        ResumenPanelRequest invalido = requestConAlertaDeBodegaInexistente(999999L);

        assertThatThrownBy(() -> panelResumenService.publicar(invalido))
                .as("Un id de bodega inexistente en una alerta debe rechazarse")
                .isInstanceOf(BusinessException.class);

        // 3) El resumen anterior (valido) sigue siendo el que devuelve GET /panel/resumen.
        ResumenPanelResponse ultimo = panelResumenService.obtenerUltimo();
        assertThat(ultimo.getFecha()).isEqualTo(primero.getFecha());
        assertThat(ultimo.getAlertas().get(0).getBodegaId()).isEqualTo(bodegaReal.getId());
    }
}
