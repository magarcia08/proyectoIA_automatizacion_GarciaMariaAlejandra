package com.project.springboot.demoproject.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.repositories.OrdenCompraRepository;

import lombok.RequiredArgsConstructor;

/**
 * Genera y guarda el PDF de una orden de compra (Apache PDFBox). Si la
 * orden esta en BORRADOR, dibuja una marca de agua diagonal semitransparente
 * con el texto "BORRADOR". Ver docs/sdd/02-especificacion.md.
 *
 * FASE 2: solo la firma de los metodos, sin logica (ver RiesgoService).
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private final OrdenCompraRepository ordenCompraRepository;

    /** Genera el PDF, lo guarda en la orden (reemplazando el anterior si existe) y lo devuelve. */
    @Transactional
    public byte[] generarYGuardar(Long ordenId) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    /** Devuelve el PDF ya guardado. Lanza ResourceNotFoundException (404) si no se ha generado. */
    @Transactional(readOnly = true)
    public byte[] obtenerPdfGuardado(Long ordenId) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }
}
