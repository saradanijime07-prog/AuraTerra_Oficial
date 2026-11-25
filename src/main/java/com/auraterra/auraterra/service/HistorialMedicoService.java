package com.auraterra.auraterra.service;

import com.auraterra.auraterra.model.HistorialMedico;
import com.auraterra.auraterra.repository.HistorialMedicoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HistorialMedicoService {

    private final HistorialMedicoRepository historialRepository;

    public HistorialMedicoService(HistorialMedicoRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public List<HistorialMedico> obtenerHistorialPorAnimal(Long animalId) {
        return historialRepository.findByAnimalId(animalId);
    }

    public HistorialMedico guardarHistorial(HistorialMedico historial) {
        return historialRepository.save(historial);
    }

    public void eliminarHistorial(Long id) {
        historialRepository.deleteById(id);
    }
}