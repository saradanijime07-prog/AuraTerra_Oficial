package com.example.auraterra_oficial.service;

import com.example.auraterra_oficial.model.HistorialMedico;
import com.example.auraterra_oficial.repository.HistorialMedicoRepository;
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