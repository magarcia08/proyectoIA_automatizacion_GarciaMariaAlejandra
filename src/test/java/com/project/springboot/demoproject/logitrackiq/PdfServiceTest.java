package com.project.springboot.demoproject.logitrackiq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.logitrackiq.support.AbstractLogiTrackIqTest;
import com.project.springboot.demoproject.services.OrdenCompraService;
import com.project.springboot.demoproject.services.PdfService;

/**
 * Regla obligatoria 8 de docs/enunciado-logitrack-iq.md. Escrita ANTES de
 * implementar PdfService.
 */
class PdfServiceTest extends AbstractLogiTrackIqTest {

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Autowired
    private PdfService pdfService;

    private Long crearOrdenBorrador() {
        Usuario admin = crearUsuario(Rol.ADMIN);
        Proveedor proveedor = crearProveedor(7);
        Producto producto = crearProducto(proveedor);
        Bodega bodega = crearBodega(1000);
        autenticarComo(admin);

        OrdenCompraRequest req = new OrdenCompraRequest();
        req.setProductoId(producto.getId());
        req.setProveedorId(proveedor.getId());
        req.setBodegaDestinoId(bodega.getId());
        req.setCantidad(15);
        req.setPrecioUnitario(new BigDecimal("3000.00"));

        OrdenCompraResponse creada = ordenCompraService.crear(req);
        return creada.getId();
    }

    @Test
    void pdfDeOrdenBorrador_seGuardaYContieneMarcaDeAguaBorrador() throws Exception {
        Long ordenId = crearOrdenBorrador();

        byte[] pdf = pdfService.generarYGuardar(ordenId);

        assertThat(pdf).isNotEmpty();
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(doc);
            assertThat(texto).contains("BORRADOR");
        }

        byte[] guardado = pdfService.obtenerPdfGuardado(ordenId);
        assertThat(guardado).isEqualTo(pdf);
    }

    @Test
    void alCambiarEstado_elPdfGuardadoYaNoQuedaDisponible() {
        Long ordenId = crearOrdenBorrador();
        pdfService.generarYGuardar(ordenId);

        ordenCompraService.cambiarEstado(ordenId, "APROBADA");

        assertThatThrownBy(() -> pdfService.obtenerPdfGuardado(ordenId))
                .as("Cambiar el estado debe borrar el PDF guardado hasta que se genere de nuevo")
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
