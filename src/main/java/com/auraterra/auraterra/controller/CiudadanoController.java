package com.auraterra.auraterra.controller;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.model.Usuario;
import com.auraterra.auraterra.service.AnimalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/ciudadano")
public class CiudadanoController {

    private final AnimalService animalService;

    public CiudadanoController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping("/inicio")
    public String inicioCiudadano(
            @RequestParam(value = "usuarioEmail", required = false) String usuarioEmail,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estadoSalud", required = false) String estadoSalud,
            HttpSession session,
            Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String correoFiltro = usuario != null ? usuario.getCorreo() : usuarioEmail;

        List<Animal> animalesFiltrados = animalService.filtrarMulticriterio(busqueda, estadoSalud, null);
        List<Animal> todosLosAnimales = animalService.obtenerTodos();

        List<Animal> misReportes = (correoFiltro != null && !correoFiltro.isBlank())
                ? todosLosAnimales.stream()
                        .filter(a -> a.getObservaciones() != null
                                && a.getObservaciones().toLowerCase(Locale.ROOT)
                                        .contains(correoFiltro.trim().toLowerCase(Locale.ROOT)))
                        .toList()
                : List.of();

        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("correoFiltro", correoFiltro != null ? correoFiltro : "");
        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("estadoSalud", estadoSalud != null ? estadoSalud : "");
        model.addAttribute("reportes", animalesFiltrados);
        model.addAttribute("misReportes", misReportes);
        model.addAttribute("totalMisReportes", misReportes.size());
        if (!model.containsAttribute("nuevoReporte")) {
            model.addAttribute("nuevoReporte", new Animal());
        }

        return "inicio";
    }

    @PostMapping("/reportar")
    public String crearReporte(
            @ModelAttribute("nuevoReporte") Animal reporte,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String correoReportante = usuario != null ? usuario.getCorreo() : null;

        try {
            if (correoReportante != null && !correoReportante.isBlank()) {
                String obsActual = reporte.getObservaciones() != null ? reporte.getObservaciones().trim() : "";
                String marcaCorreo = "[Reportante: " + correoReportante.trim().toLowerCase(Locale.ROOT) + "]";
                if (!obsActual.toLowerCase(Locale.ROOT).contains(correoReportante.trim().toLowerCase(Locale.ROOT))) {
                    reporte.setObservaciones(obsActual.isBlank() ? marcaCorreo : obsActual + " " + marcaCorreo);
                }
            }

            animalService.guardarAnimal(reporte);
            redirectAttributes.addFlashAttribute("mensajeReporte", "Tu reporte fue registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorReporte", ex.getMessage());
            redirectAttributes.addFlashAttribute("nuevoReporte", reporte);
        }

        if (correoReportante != null && !correoReportante.isBlank()) {
            return "redirect:/ciudadano/inicio?usuarioEmail=" + correoReportante + "#mis-reportes";
        }
        return "redirect:/ciudadano/inicio#mis-reportes";
    }
}