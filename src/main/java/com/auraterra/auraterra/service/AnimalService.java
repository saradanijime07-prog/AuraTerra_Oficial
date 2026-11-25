package com.auraterra.auraterra.service;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    // Inyección por constructor (limpia la advertencia de Spring)
    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<Animal> obtenerTodos() {
        return animalRepository.findAll();
    }

    public Optional<Animal> obtenerPorId(Long id) {
        return animalRepository.findById(id);
    }

    public Animal guardarAnimal(Animal animal) {
        return animalRepository.save(animal);
    }

    public void eliminarAnimal(Long id) {
        animalRepository.deleteById(id);
    }

    public List<Animal> buscarPorNombreOEspecie(String busqueda) {
        return animalRepository.findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(busqueda, busqueda);
    }

    public Page<Animal> obtenerPagina(String busqueda, Pageable pageable) {
        if (busqueda != null && !busqueda.isEmpty()) {
            return animalRepository.findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCase(busqueda, busqueda,
                    pageable);
        }
        return animalRepository.findAll(pageable);
    }
}