package com.auraterra.auraterra.controller;

import com.auraterra.auraterra.model.Animal;
import com.auraterra.auraterra.model.HistorialMedico;
import com.auraterra.auraterra.model.Usuario;
import com.auraterra.auraterra.repository.UsuarioRepository;
import com.auraterra.auraterra.service.AnimalService;
import com.auraterra.auraterra.service.HistorialMedicoService;
import com.auraterra.auraterra.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final AnimalService animalService;
    private final HistorialMedicoService historialMedicoService;

    public AdminController(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            AnimalService animalService,
            HistorialMedicoService historialMedicoService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.animalService = animalService;
        this.historialMedicoService = historialMedicoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estadoSalud", required = false) String estadoSalud,
            @RequestParam(value = "zonaRescate", required = false) String zonaRescate,
            HttpSession session,
            Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        String rol = usuarioService.normalizarRol(usuario.getRol());
        if (!"ADMIN".equals(rol)) {
            return usuarioService.determinarRutaPorRol(rol, usuario.getCorreo());
        }

        List<Animal> todosAnimales = animalService.obtenerTodos();
        List<Animal> animalesFiltrados = animalService.filtrarMulticriterio(busqueda, estadoSalud, zonaRescate);

        List<HistorialMedico> historiales = new ArrayList<>();
        for (Animal animal : todosAnimales) {
            if (animal.getId() != null) {
                historiales.addAll(historialMedicoService.obtenerHistorialPorAnimal(animal.getId()));
            }
        }
        List<Usuario> usuarios = usuarioRepository.findAll();

        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("rolActual", rol);
        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("estadoSalud", estadoSalud != null ? estadoSalud : "");
        model.addAttribute("zonaRescate", zonaRescate != null ? zonaRescate : "");
        model.addAttribute("animales", animalesFiltrados);
        model.addAttribute("historiales", historiales);
        model.addAttribute("usuariosRegistrados", usuarios);
        model.addAttribute("totalAnimales", todosAnimales.size());
        model.addAttribute("totalFiltrados", animalesFiltrados.size());
        model.addAttribute("totalHistoriales", historiales.size());
        model.addAttribute("totalUsuarios", usuarios.size());

        return "admin/dashboard";
    }
}