package com.example.auraterra_oficial.controller;

import com.example.auraterra_oficial.model.Animal;
import com.example.auraterra_oficial.model.HistorialMedico;
import com.example.auraterra_oficial.service.AnimalService;
import com.example.auraterra_oficial.service.HistorialMedicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/animales")
public class AnimalController {

    private final AnimalService animalService;
    private final HistorialMedicoService historialService;

    public AnimalController(AnimalService animalService, HistorialMedicoService historialService) {
        this.animalService = animalService;
        this.historialService = historialService;
    }

    // 1. Listar, filtrar, paginar y calcular estadísticas para el Dashboard
    @GetMapping("/vista")
    public String listarAnimalesVista(Model model,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        int pageSize = 5; // Cantidad de animales por página
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Animal> paginaAnimales = animalService.obtenerPagina(busqueda, pageable);

        model.addAttribute("animales", paginaAnimales.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaAnimales.getTotalPages());
        model.addAttribute("totalAnimales", paginaAnimales.getTotalElements()); // Total real en base de datos
        model.addAttribute("nuevoAnimal", new Animal());
        model.addAttribute("busqueda", busqueda);

        return "animales";
    }

    // 2. Guardar o actualizar un animal
    @PostMapping("/guardar")
    public String guardarAnimalVista(@ModelAttribute Animal animal) {
        animalService.guardarAnimal(animal);
        return "redirect:/animales/vista";
    }

    // 3. Cargar los datos del animal en el formulario para editarlo
    @GetMapping("/editar/{id}")
    public String editarAnimal(@PathVariable Long id, Model model) {
        Animal animal = animalService.obtenerPorId(id).orElse(new Animal());

        // Mantenemos la paginación activa al editar
        Pageable pageable = PageRequest.of(0, 5, Sort.by(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Animal> paginaAnimales = animalService.obtenerPagina(null, pageable);

        model.addAttribute("nuevoAnimal", animal);
        model.addAttribute("animales", paginaAnimales.getContent());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", paginaAnimales.getTotalPages());
        model.addAttribute("totalAnimales", paginaAnimales.getTotalElements());

        return "animales";
    }

    // 4. Eliminar un animal por su ID
    @GetMapping("/eliminar/{id}")
    public String eliminarAnimal(@PathVariable Long id) {
        animalService.eliminarAnimal(id);
        return "redirect:/animales/vista";
    }

    // 5. Ver el historial médico de un animal en específico
    @GetMapping("/historial/{id}")
    public String verHistorial(@PathVariable Long id, Model model) {
        Animal animal = animalService.obtenerPorId(id).orElse(null);
        if (animal == null) {
            return "redirect:/animales/vista";
        }

        List<HistorialMedico> historialList = historialService.obtenerHistorialPorAnimal(id);
        HistorialMedico nuevoHistorial = new HistorialMedico();
        nuevoHistorial.setAnimal(animal);
        nuevoHistorial.setFecha(LocalDate.now());

        model.addAttribute("animal", animal);
        model.addAttribute("historiales", historialList);
        model.addAttribute("nuevoHistorial", nuevoHistorial);

        return "historial_detalle";
    }

    // 6. Guardar una nueva nota médica para el animal
    @PostMapping("/historial/guardar")
    public String guardarHistorial(@ModelAttribute HistorialMedico historialMedico) {
        historialService.guardarHistorial(historialMedico);
        return "redirect:/animales/historial/" + historialMedico.getAnimal().getId();
    }
}