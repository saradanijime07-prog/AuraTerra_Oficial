package com.auraterra.auraterra.controller;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.repository.AnimalRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ciudadano")
public class CiudadanoController {

    private final AnimalRepository animalRepository;

    public CiudadanoController(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @GetMapping("/inicio")
    public String mostrarPortalCiudadano(
            @RequestParam(name = "condicion", required = false, defaultValue = "TODOS") String condicion,
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "usuarioEmail", required = false, defaultValue = "") String usuarioEmail,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {

        int tamanoPagina = 6;
        Page<Animal> paginaCasos = animalRepository.buscarCasosCiudadanos(
                condicion,
                q.trim(),
                PageRequest.of(Math.max(page, 0), tamanoPagina));

        String filtroUsuario = usuarioEmail.trim();
        List<Animal> misReportes = filtroUsuario.isEmpty()
                ? Collections.emptyList()
                : animalRepository.buscarReportesPorUsuario(filtroUsuario);

        model.addAttribute("paginaCasos", paginaCasos);
        model.addAttribute("animales", paginaCasos.getContent());
        model.addAttribute("condicionActual", condicion);
        model.addAttribute("busquedaActual", q);
        model.addAttribute("usuarioEmailActual", filtroUsuario);
        model.addAttribute("misReportes", misReportes);

        return "inicio";
    }

    @PostMapping("/reportar")
    public String recibirReporteCiudadano(
            @RequestParam String especie,
            @RequestParam String estadoSalud,
            @RequestParam String ubicacion,
            @RequestParam(required = false) String contacto,
            @RequestParam(required = false) String usuarioEmail,
            @RequestParam String observaciones,
            RedirectAttributes redirectAttributes) {

        Animal nuevoAnimal = new Animal();
        nuevoAnimal.setNombre(especie.trim());
        nuevoAnimal.setEspecie(especie.trim());
        nuevoAnimal.setEstadoSalud(estadoSalud);

        String correoOContacto = (contacto != null && !contacto.isBlank())
                ? contacto.trim()
                : (usuarioEmail != null ? usuarioEmail.trim() : "");

        // Si agregas el campo contactoReportante en tu entidad Animal, guárdalo aquí:
        // nuevoAnimal.setContactoReportante(correoOContacto);

        String detalle = "Zona: " + ubicacion.trim() + " — " + observaciones.trim();
        if (!correoOContacto.isEmpty()) {
            detalle += " (Contacto: " + correoOContacto + ")";
        }
        nuevoAnimal.setObservaciones(detalle);

        Animal guardado = animalRepository.save(nuevoAnimal);

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "¡Gracias por tu reporte! Caso #" + guardado.getId()
                        + " registrado: " + especie.trim()
                        + " (" + estadoSalud + ") en " + ubicacion.trim() + ".");

        if (!correoOContacto.isEmpty()) {
            redirectAttributes.addAttribute("usuarioEmail", correoOContacto);
        }

        return "redirect:/ciudadano/inicio#mis-reportes";
    }
}