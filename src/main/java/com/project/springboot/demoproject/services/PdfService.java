package com.project.springboot.demoproject.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrden;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;

import lombok.RequiredArgsConstructor;

/**
 * Genera y guarda el PDF de una orden de compra (Apache PDFBox). Si la
 * orden esta en BORRADOR, dibuja una marca de agua diagonal semitransparente
 * con el texto "BORRADOR". Ver docs/sdd/02-especificacion.md.
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private final OrdenCompraRepository ordenCompraRepository;

    /** Genera el PDF, lo guarda en la orden (reemplazando el anterior si existe) y lo devuelve. */
    @Transactional
    public byte[] generarYGuardar(Long ordenId) {
        OrdenCompra orden = ordenCompraRepository.findById(ordenId)
                .orElseThrow(() -> ResourceNotFoundException.of("OrdenCompra", ordenId));

        byte[] pdf = generarPdf(orden);
        orden.setPdfDocumento(pdf);
        orden.setPdfGeneradoEn(LocalDateTime.now());
        ordenCompraRepository.save(orden);

        return pdf;
    }

    /** Devuelve el PDF ya guardado. Lanza ResourceNotFoundException (404) si no se ha generado. */
    @Transactional(readOnly = true)
    public byte[] obtenerPdfGuardado(Long ordenId) {
        OrdenCompra orden = ordenCompraRepository.findById(ordenId)
                .orElseThrow(() -> ResourceNotFoundException.of("OrdenCompra", ordenId));
        if (orden.getPdfDocumento() == null) {
            throw new ResourceNotFoundException(
                    "La orden " + ordenId + " todavia no tiene un PDF generado. Use POST /ordenes/" + ordenId + "/pdf primero.");
        }
        return orden.getPdfDocumento();
    }

    // ------------------------------------------------------------------

    private byte[] generarPdf(OrdenCompra orden) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                if (orden.getEstado() == EstadoOrden.BORRADOR) {
                    dibujarMarcaDeAgua(contenido, pagina);
                }
                dibujarDatosOrden(contenido, pagina, orden);
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el PDF de la orden " + orden.getId(), e);
        }
    }

    /** Marca de agua diagonal, semitransparente y legible, con el texto "BORRADOR". */
    private void dibujarMarcaDeAgua(PDPageContentStream contenido, PDPage pagina) throws IOException {
        PDExtendedGraphicsState transparencia = new PDExtendedGraphicsState();
        transparencia.setNonStrokingAlphaConstant(0.25f);
        contenido.setGraphicsStateParameters(transparencia);

        PDFont fuenteMarca = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float centroX = pagina.getMediaBox().getWidth() / 2f;
        float centroY = pagina.getMediaBox().getHeight() / 2f;

        contenido.beginText();
        contenido.setFont(fuenteMarca, 90);
        contenido.setNonStrokingColor(new Color(200, 40, 40));
        contenido.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), centroX - 260, centroY - 80));
        contenido.showText("BORRADOR");
        contenido.endText();

        PDExtendedGraphicsState opacidadNormal = new PDExtendedGraphicsState();
        opacidadNormal.setNonStrokingAlphaConstant(1f);
        contenido.setGraphicsStateParameters(opacidadNormal);
    }

    /** Numero de orden, fecha, proveedor, producto, cantidad, precio unitario, total, bodega destino y estado. */
    private void dibujarDatosOrden(PDPageContentStream contenido, PDPage pagina, OrdenCompra orden) throws IOException {
        PDFont fuenteTitulo = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fuenteTexto = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float margen = 60;
        float y = pagina.getMediaBox().getHeight() - 80;

        contenido.setNonStrokingColor(Color.BLACK);
        contenido.beginText();
        contenido.setFont(fuenteTitulo, 18);
        contenido.newLineAtOffset(margen, y);
        contenido.showText("Orden de compra No. " + orden.getId());
        contenido.endText();

        y -= 40;
        List<String> lineas = List.of(
                "Fecha de creacion: " + orden.getFechaCreacion(),
                "Proveedor: " + orden.getProveedor().getNombre(),
                "Producto: " + orden.getProducto().getNombre(),
                "Cantidad: " + orden.getCantidad(),
                "Precio unitario: " + orden.getPrecioUnitario(),
                "Total: " + orden.getTotal(),
                "Bodega destino: " + orden.getBodegaDestino().getNombre(),
                "Estado: " + orden.getEstado());

        contenido.setFont(fuenteTexto, 12);
        for (String linea : lineas) {
            contenido.beginText();
            contenido.newLineAtOffset(margen, y);
            contenido.showText(linea);
            contenido.endText();
            y -= 20;
        }
    }
}
