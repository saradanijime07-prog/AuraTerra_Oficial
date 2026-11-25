package com.auraterra.auraterra.repository;

import com.auraterra.auraterra.model.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // Método para buscar por nombre o especie sin importar mayúsculas/minúsculas
    List<Animal> findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(String nombre, String especie);

    Page<Animal> findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(String nombre, String especie,
            Pageable pageable);

    @Query("""
            SELECT a FROM Animal a
            WHERE (:condicion IS NULL OR :condicion = '' OR :condicion = 'TODOS' OR a.estadoSalud = :condicion)
              AND (
                    :q IS NULL OR :q = ''
                    OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(a.especie) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(a.observaciones, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            ORDER BY a.id DESC
            """)
    Page<Animal> buscarCasosCiudadanos(
            @Param("condicion") String condicion,
            @Param("q") String q,
            Pageable pageable);

    @Query("""
            SELECT a FROM Animal a
            WHERE :usuario IS NOT NULL AND :usuario <> ''
              AND LOWER(COALESCE(a.observaciones, '')) LIKE LOWER(CONCAT('%', :usuario, '%'))
            ORDER BY a.id DESC
            """)
    List<Animal> buscarReportesPorUsuario(@Param("usuario") String usuario);
}