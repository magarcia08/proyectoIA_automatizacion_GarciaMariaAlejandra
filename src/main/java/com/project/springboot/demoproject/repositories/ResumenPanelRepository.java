package com.project.springboot.demoproject.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.springboot.demoproject.entities.ResumenPanel;

public interface ResumenPanelRepository extends JpaRepository<ResumenPanel, Long> {

    Optional<ResumenPanel> findByFecha(LocalDate fecha);

    /** El "ultimo resumen valido" = el de fecha mas reciente (solo hay uno por fecha). */
    Optional<ResumenPanel> findFirstByOrderByFechaDesc();
}
