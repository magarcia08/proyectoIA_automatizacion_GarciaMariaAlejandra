package com.project.springboot.demoproject.logitrackiq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.EstadoOrden;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.logitrackiq.support.AbstractLogiTrackIqTest;
import com.project.springboot.demoproject.services.OrdenCompraService;

/**
 * Reglas obligatorias 4 y 5 de docs/enunciado-logitrack-iq.md. Escritas
 * ANTES de implementar OrdenCompraService.cambiarEstado.
 */
class OrdenCompraServiceTest extends AbstractLogiTrackIqTest {

    @Autowired
    private OrdenCompraService ordenCompraService;

    private OrdenCompraRequest requestValido(Producto producto, Proveedor proveedor, Bodega bodega) {
        OrdenCompraRequest req = new OrdenCompraRequest();
        req.setProductoId(producto.getId());
        req.setProveedorId(proveedor.getId());
        req.setBodegaDestinoId(bodega.getId());
        req.setCantidad(20);
        req.setPrecioUnitario(new BigDecimal("5000.00"));
        return req;
    }

    @Test
    void ordenCancelada_noSePuedeAprobar() {
        Usuario admin = crearUsuario(Rol.ADMIN);
        Proveedor proveedor = crearProveedor(7);
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);
        autenticarComo(admin);

        OrdenCompraResponse creada = ordenCompraService.crear(requestValido(producto, proveedor, bodega));
        ordenCompraService.cambiarEstado(creada.getId(), "CANCELADA");

        assertThatThrownBy(() -> ordenCompraService.cambiarEstado(creada.getId(), "APROBADA"))
                .as("BORRADOR/CANCELADA -> APROBADA no esta permitido una vez cancelada")
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void ordenAprobada_alRecibirse_generaMovimientoEntrada() {
        Usuario admin = crearUsuario(Rol.ADMIN);
        Proveedor proveedor = crearProveedor(7);
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);
        autenticarComo(admin);

        OrdenCompraResponse creada = ordenCompraService.crear(requestValido(producto, proveedor, bodega));
        ordenCompraService.cambiarEstado(creada.getId(), "APROBADA");
        OrdenCompraResponse recibida = ordenCompraService.cambiarEstado(creada.getId(), "RECIBIDA");

        assertThat(recibida.getEstado()).isEqualTo(EstadoOrden.RECIBIDA);

        List<Movimiento> entradas = movimientoRepository.findByBodegaDestinoId(bodega.getId()).stream()
                .filter(m -> m.getTipo() == TipoMovimiento.ENTRADA)
                .filter(m -> m.getDetalles().stream().anyMatch(d -> d.getProducto().getId().equals(producto.getId())
                        && d.getCantidad().equals(20)))
                .toList();

        assertThat(entradas)
                .as("La recepcion debe crear automaticamente un movimiento ENTRADA por la cantidad y bodega destino de la orden")
                .isNotEmpty();
    }
}
