package com.auraterra.auraterra.controller;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.model.HistorialMedico;
import com.auraterra.auraterra.model.Usuario;
import com.auraterra.auraterra.service.AnimalService;
import com.auraterra.auraterra.service.HistorialMedicoService;
import com.auraterra.auraterra.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/animales")
public class AnimalController {

    private final AnimalService animalService;
    private final HistorialMedicoService historialMedicoService;
    private final UsuarioService usuarioService;

    public AnimalController(
            AnimalService animalService,
            HistorialMedicoService historialMedicoService,
            UsuarioService usuarioService) {
        this.animalService = animalService;
        this.historialMedicoService = historialMedicoService;
        this.usuarioService = usuarioService;
    }

    private String validarAccesoClinico(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        String rol = usuarioService.normalizarRol(usuario.getRol());
        if (!"VETERINARIO".equals(rol) && !"ADMIN".equals(rol)) {
            return usuarioService.determinarRutaPorRol(rol, usuario.getCorreo());
        }
        return null;
    }

    private Map<Long, String> construirResumenControles(List<Animal> animales) {
        Map<Long, String> resumen = new HashMap<>();
        for (Animal animal : animales) {
            if (animal.getId() != null) {
                List<HistorialMedico> lista = historialMedicoService.obtenerHistorialPorAnimal(animal.getId());
                if (lista == null || lista.isEmpty()) {
                    resumen.put(animal.getId(), "Sin controles aún");
                } else {
                    resumen.put(animal.getId(),
                            lista.size() + (lista.size() == 1 ? " control registrado" : " controles registrados"));
                }
            }
        }
        return resumen;
    }

    private void cargarDatosVista(
            Model model,
            Usuario usuario,
            String rol,
            String busqueda,
            String estadoSalud,
            String zonaRescate,
            Animal formulario) {
        List<Animal> todos = animalService.obtenerTodos();
        List<Animal> filtrados = animalService.filtrarMulticriterio(busqueda, estadoSalud, zonaRescate);

        long totalCriticos = todos.stream()
                .filter(a -> a.getEstadoSalud() != null
                        && (a.getEstadoSalud().toUpperCase(Locale.ROOT).contains("CRITIC")
                                || a.getEstadoSalud().toUpperCase(Locale.ROOT).contains("CRÍTIC")))
                .count();

        long totalEnRehabilitacion = todos.stream()
                .filter(a -> a.getEstadoSalud() != null
                        && (a.getEstadoSalud().toUpperCase(Locale.ROOT).contains("REHAB")
                                || a.getEstadoSalud().toUpperCase(Locale.ROOT).contains("OBSERV")))
                .count();

        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("rolActual", rol);
        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("estadoSalud", estadoSalud != null ? estadoSalud : "");
        model.addAttribute("zonaRescate", zonaRescate != null ? zonaRescate : "");
        model.addAttribute("animales", filtrados);
        model.addAttribute("totalAnimales", todos.size());
        model.addAttribute("totalFiltrados", filtrados.size());
        model.addAttribute("totalCriticos", totalCriticos);
        model.addAttribute("totalEnRehabilitacion", totalEnRehabilitacion);
        model.addAttribute("ultimoControlPorAnimal", construirResumenControles(filtrados));
        if (!model.containsAttribute("animalForm")) {
            model.addAttribute("animalForm", formulario != null ? formulario : new Animal());
        }
    }

    @GetMapping("/vista")
    public String verAnimales(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estadoSalud", required = false) String estadoSalud,
            @RequestParam(value = "zonaRescate", required = false) String zonaRescate,
            HttpSession session,
            Model model) {
        String redireccion = validarAccesoClinico(session);
        if (redireccion != null) {
            return redireccion;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String rol = usuarioService.normalizarRol(usuario.getRol());
        cargarDatosVista(model, usuario, rol, busqueda, estadoSalud, zonaRescate, new Animal());

        return "animales";
    }

    @PostMapping("/guardar")
    public String guardarAnimal(
            @ModelAttribute("animalForm") Animal animal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String redireccion = validarAccesoClinico(session);
        if (redireccion != null) {
            return redireccion;
        }

        try {
            animalService.guardarAnimal(animal);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro de fauna guardado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            redirectAttributes.addFlashAttribute("animalForm", animal);
        }
        return "redirect:/animales/vista";
    }

    @GetMapping("/editar/{id}")
    public String editarAnimal(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String redireccion = validarAccesoClinico(session);
        if (redireccion != null) {
            return redireccion;
        }

        Optional<Animal> existenteOpt = animalService.obtenerPorId(id);
        if (existenteOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el paciente solicitado.");
            return "redirect:/animales/vista";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String rol = usuarioService.normalizarRol(usuario.getRol());
        cargarDatosVista(model, usuario, rol, "", "", "", existenteOpt.get());

        return "animales";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarAnimal(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String redireccion = validarAccesoClinico(session);
        if (redireccion != null) {
            return redireccion;
        }

        try {
            animalService.eliminarAnimal(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro eliminado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/animales/vista";
    }

    @GetMapping("/historial/{id}")
    public String verHistorialAnimal(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String redireccion = validarAccesoClinico(session);
        if (redireccion != null) {
            return redireccion;
        }

        Optional<Animal> animalOpt = animalService.obtenerPorId(id);
        if (animalOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el paciente solicitado.");
            return "redirect:/animales/vista";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String rol = usuarioService.normalizarRol(usuario.getRol());

        Animal animal = animalOpt.get();
        HistorialMedico nuevoHistorial = new HistorialMedico();
        nuevoHistorial.setAnimal(animal);
        nuevoHistorial.setFecha(LocalDate.now());

        List<HistorialMedico> historiales = historialMedicoService.obtenerHistorialPorAnimal(id);

        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("rolActual", rol);
        model.addAttribute("animal", animal);
        model.addAttribute("nuevoHistorial", nuevoHistorial);
        model.addAttribute("historiales", historiales);

        return "historialdetalle";
    }

    @PostMapping("/historial/guardar")
    public String guardarHistorialAnimal(
            @ModelAttribute("nuevoHistorial") HistorialMedico historial,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String redireccion = validarAccesoClinico(session);
        if (redireccion != null) {
            return redireccion;
        }

        Long animalId = (historial.getAnimal() != null) ? historial.getAnimal().getId() : null;
        try {
            historialMedicoService.guardarHistorial(historial);
            redirectAttributes.addFlashAttribute("mensajeExito", "Control clínico registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        if (animalId != null) {
            return "redirect:/animales/historial/" + animalId;
        }
        return "redirect:/animales/vista";
    }
}