package com.example.auraterra_oficial.repository;

import com.example.auraterra_oficial.model.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // Método para buscar por nombre o especie sin importar mayúsculas/minúsculas
    List<Animal> findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(String nombre, String especie);

    Page<Animal> findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(String nombre, String especie,
            Pageable pageable);
}