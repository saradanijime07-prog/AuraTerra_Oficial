package com.auraterra.auraterra.repository;

import com.auraterra.auraterra.model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {
    // Para buscar todos los historiales asociados a un animal específico
    List<HistorialMedico> findByAnimalId(Long animalId);
}