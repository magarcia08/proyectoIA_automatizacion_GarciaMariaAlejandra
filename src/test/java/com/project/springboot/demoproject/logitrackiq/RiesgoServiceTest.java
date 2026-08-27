package com.project.springboot.demoproject.logitrackiq;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.springboot.demoproject.dto.logitrackiq.CoberturaCalculo;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.EstadoCobertura;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.logitrackiq.support.AbstractLogiTrackIqTest;
import com.project.springboot.demoproject.services.RiesgoService;

/**
 * Reglas obligatorias 1 y 2 de docs/enunciado-logitrack-iq.md ("Pruebas
 * obligatorias"). Escritas ANTES de implementar RiesgoService.calcularCobertura
 * (ver docs/sdd/04-tareas.md, fase "test: define reorder and order-state rules").
 */
class RiesgoServiceTest extends AbstractLogiTrackIqTest {

    @Autowired
    private RiesgoService riesgoService;

    @Test
    void consumoCero_coberturaEsNullYEstadoEsSinConsumo() {
        Proveedor proveedor = crearProveedor(10);
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);
        asignarStock(bodega, producto, 50);
        // Sin ningun movimiento SALIDA => consumo diario promedio = 0

        CoberturaCalculo resultado = riesgoService.calcularCobertura(producto.getId());

        assertThat(resultado.consumoDiarioPromedio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.diasCobertura()).isNull();
        assertThat(resultado.estadoCobertura()).isEqualTo(EstadoCobertura.SIN_CONSUMO);
        assertThat(resultado.enRiesgo()).isFalse();
    }

    @Test
    void stockIgualAlPuntoDeReorden_noEstaEnRiesgo() {
        Usuario usuario = crearUsuario(Rol.EMPLEADO);
        Proveedor proveedor = crearProveedor(10); // diasEntrega = 10
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);

        // 300 unidades de SALIDA en los ultimos 30 dias => consumo = 10/dia
        // puntoReorden = 10 * 10 * 1.5 = 150
        registrarSalidaHace(usuario, bodega, producto, 300, 5);
        asignarStock(bodega, producto, 150); // stock == puntoReorden exacto

        CoberturaCalculo resultado = riesgoService.calcularCobertura(producto.getId());

        assertThat(resultado.puntoReorden()).isEqualByComparingTo(new BigDecimal("150"));
        assertThat(resultado.stockTotal()).isEqualTo(150);
        assertThat(resultado.enRiesgo())
                .as("stock igual al punto de reorden NO debe contar como en riesgo (debe ser estrictamente menor)")
                .isFalse();
        assertThat(resultado.diasCobertura()).isNotNull();
    }
}
