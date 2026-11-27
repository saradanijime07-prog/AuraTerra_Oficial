package com.auraterra.auraterra.service;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.model.HistorialMedico;
import com.auraterra.auraterra.repository.AnimalRepository;
import com.auraterra.auraterra.repository.HistorialMedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Service
public class HistorialMedicoService {

    private final HistorialMedicoRepository historialRepository;
    private final AnimalRepository animalRepository;

    public HistorialMedicoService(
            HistorialMedicoRepository historialRepository,
            AnimalRepository animalRepository) {
        this.historialRepository = historialRepository;
        this.animalRepository = animalRepository;
    }

    public List<HistorialMedico> obtenerHistorialPorAnimal(Long animalId) {
        if (animalId == null) {
            return List.of();
        }
        return historialRepository.findByAnimalId(animalId);
    }

    @Transactional
    public HistorialMedico guardarHistorial(HistorialMedico historial) {
        if (historial == null) {
            throw new IllegalArgumentException("El registro del control médico es obligatorio.");
        }
        if (historial.getDiagnostico() == null || historial.getDiagnostico().isBlank()) {
            throw new IllegalArgumentException("El diagnóstico es obligatorio.");
        }
        if (historial.getDescripcionTratamiento() == null || historial.getDescripcionTratamiento().isBlank()) {
            throw new IllegalArgumentException("La descripción del tratamiento es obligatoria.");
        }
        if (historial.getAnimal() == null || historial.getAnimal().getId() == null) {
            throw new IllegalArgumentException("Debes asociar el control médico a un animal registrado.");
        }

        Animal paciente = animalRepository.findById(historial.getAnimal().getId())
                .orElseThrow(() -> new IllegalArgumentException("El animal asociado no existe en la base de datos."));

        if (historial.getPesoKg() != null && historial.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso en kilogramos debe ser mayor a cero.");
        }

        if (historial.getFecha() == null) {
            historial.setFecha(LocalDate.now());
        }

        historial.setDiagnostico(historial.getDiagnostico().trim());
        historial.setDescripcionTratamiento(historial.getDescripcionTratamiento().trim());
        if (historial.getObservaciones() != null) {
            historial.setObservaciones(historial.getObservaciones().trim());
        }
        if (historial.getEstadoResultante() != null && !historial.getEstadoResultante().isBlank()) {
            String estadoLimpio = historial.getEstadoResultante().trim();
            historial.setEstadoResultante(estadoLimpio);
            paciente.setEstadoSalud(estadoLimpio);
            animalRepository.save(paciente);
        }

        historial.setAnimal(paciente);
        return historialRepository.save(historial);
    }

    @Transactional
    public void eliminarHistorial(Long id) {
        if (id == null || !historialRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el control médico especificado.");
        }
        historialRepository.deleteById(id);
    }
}