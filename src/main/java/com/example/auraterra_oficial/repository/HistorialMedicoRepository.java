package com.example.auraterra_oficial.repository;

import com.example.auraterra_oficial.model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {

    // Para buscar todos los historiales asociados a un animal específico
    List<HistorialMedico> findByAnimalId(Long animalId);

}