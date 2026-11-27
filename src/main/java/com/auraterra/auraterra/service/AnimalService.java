package com.auraterra.auraterra.service;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.repository.AnimalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<Animal> obtenerTodos() {
        return animalRepository.findAll();
    }

    public Optional<Animal> obtenerPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return animalRepository.findById(id);
    }

    public Animal guardarAnimal(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Los datos del animal son obligatorios.");
        }
        if (animal.getNombre() == null || animal.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del animal es obligatorio.");
        }
        if (animal.getEspecie() == null || animal.getEspecie().isBlank()) {
            throw new IllegalArgumentException("La especie es obligatoria.");
        }
        if (animal.getEdad() != null && animal.getEdad() < 0) {
            throw new IllegalArgumentException("La edad estimada no puede ser negativa.");
        }

        animal.setNombre(animal.getNombre().trim());
        animal.setEspecie(animal.getEspecie().trim());

        if (animal.getEstadoSalud() == null || animal.getEstadoSalud().isBlank()) {
            animal.setEstadoSalud("En observación");
        } else {
            animal.setEstadoSalud(animal.getEstadoSalud().trim());
        }

        return animalRepository.save(animal);
    }

    public void eliminarAnimal(Long id) {
        if (id == null || !animalRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el registro de fauna seleccionado.");
        }
        animalRepository.deleteById(id);
    }

    public List<Animal> buscarPorNombreOEspecie(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return animalRepository.findAll();
        }
        String termino = busqueda.trim();
        return animalRepository.findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(termino, termino);
    }

    // Filtro multicriterio para consultas y reportes en clínica/administración
    public List<Animal> filtrarMulticriterio(String busqueda, String estadoSalud, String zonaRescate) {
        String termino = (busqueda != null) ? busqueda.trim().toLowerCase(Locale.ROOT) : "";
        String estado = (estadoSalud != null) ? estadoSalud.trim().toLowerCase(Locale.ROOT) : "";
        String zona = (zonaRescate != null) ? zonaRescate.trim().toLowerCase(Locale.ROOT) : "";

        return animalRepository.findAll().stream()
                .filter(a -> termino.isEmpty()
                        || (a.getNombre() != null && a.getNombre().toLowerCase(Locale.ROOT).contains(termino))
                        || (a.getEspecie() != null && a.getEspecie().toLowerCase(Locale.ROOT).contains(termino)))
                .filter(a -> estado.isEmpty() || "todos".equals(estado)
                        || (a.getEstadoSalud() != null && a.getEstadoSalud().toLowerCase(Locale.ROOT).contains(estado)))
                .filter(a -> zona.isEmpty() || "todas".equals(zona)
                        || (a.getZonaRescate() != null && a.getZonaRescate().toLowerCase(Locale.ROOT).contains(zona)))
                .toList();
    }

    public Page<Animal> obtenerPagina(String busqueda, Pageable pageable) {
        if (busqueda != null && !busqueda.isBlank()) {
            String termino = busqueda.trim();
            return animalRepository.findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(
                    termino, termino, pageable);
        }
        return animalRepository.findAll(pageable);
    }
}