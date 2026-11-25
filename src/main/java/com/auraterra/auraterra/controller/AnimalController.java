package com.auraterra.auraterra.controller;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.model.HistorialMedico;
import com.auraterra.auraterra.service.AnimalService;
import com.auraterra.auraterra.service.HistorialMedicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/animales")
public class AnimalController {

    private final AnimalService animalService;
    private final HistorialMedicoService historialService;

    public AnimalController(AnimalService animalService, HistorialMedicoService historialService) {
        this.animalService = animalService;
        this.historialService = historialService;
    }

    // 1. Listar, filtrar, paginar y calcular estadísticas para el Dashboard Clínico
    @GetMapping("/vista")
    public String listarAnimalesVista(Model model,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        cargarModeloDashboard(model, new Animal(), busqueda, page);
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
    public String editarAnimal(@PathVariable Long id,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Animal animal = animalService.obtenerPorId(id).orElse(new Animal());
        cargarModeloDashboard(model, animal, busqueda, page);
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

    // Método auxiliar para alimentar las tablas, paginación y tarjetas KPI del
    // Dashboard
    private void cargarModeloDashboard(Model model, Animal animalFormulario, String busqueda, int page) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Animal> paginaAnimales = animalService.obtenerPagina(busqueda, pageable);

        List<Animal> todosLosAnimales = animalService.obtenerTodos();

        long totalCriticos = todosLosAnimales.stream()
                .filter(a -> a.getEstadoSalud() != null
                        && (a.getEstadoSalud().equalsIgnoreCase("CRITICO")
                                || a.getEstadoSalud().equalsIgnoreCase("CRÍTICO")))
                .count();

        long totalEnRehabilitacion = todosLosAnimales.stream()
                .filter(a -> a.getEstadoSalud() == null
                        || (!a.getEstadoSalud().equalsIgnoreCase("FALLECIDO")
                                && !a.getEstadoSalud().equalsIgnoreCase("LIBERADO")
                                && !a.getEstadoSalud().equalsIgnoreCase("CUIDADO PERMANENTE")))
                .count();

        model.addAttribute("animales", paginaAnimales.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaAnimales.getTotalPages());
        model.addAttribute("totalAnimales", paginaAnimales.getTotalElements());
        model.addAttribute("totalCriticos", totalCriticos);
        model.addAttribute("totalEnRehabilitacion", totalEnRehabilitacion);
        model.addAttribute("nuevoAnimal", animalFormulario);
        model.addAttribute("busqueda", busqueda);

        Map<Long, Integer> conteoHistoriales = new HashMap<>();
        Map<Long, String> ultimoControlPorAnimal = new HashMap<>();

        for (Animal a : paginaAnimales.getContent()) {
            List<HistorialMedico> notas = historialService.obtenerHistorialPorAnimal(a.getId());
            int cantidad = (notas != null) ? notas.size() : 0;
            conteoHistoriales.put(a.getId(), cantidad);

            if (notas != null && !notas.isEmpty()) {
                // Si la lista viene ordenada o no, tomamos el último elemento disponible de
                // forma segura
                HistorialMedico ultimaNota = notas.get(notas.size() - 1);
                String fechaTexto = (ultimaNota.getFecha() != null) ? ultimaNota.getFecha().toString() : "Sin fecha";
                ultimoControlPorAnimal.put(a.getId(), cantidad + " nota(s) · Últ: " + fechaTexto);
            } else {
                ultimoControlPorAnimal.put(a.getId(), "Sin controles aún");
            }
        }

        model.addAttribute("conteoHistoriales", conteoHistoriales);
        model.addAttribute("ultimoControlPorAnimal", ultimoControlPorAnimal);

    }
}