package com.auraterra.auraterra.service;

import com.auraterra.auraterra.model.Usuario;
import com.auraterra.auraterra.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "CIUDADANO";
        }
        return switch (rol.trim().toUpperCase(Locale.ROOT)) {
            case "ADMIN", "ADMINISTRADOR" -> "ADMIN";
            case "VETERINARIO", "CLINICO", "CLÍNICO" -> "VETERINARIO";
            default -> "CIUDADANO";
        };
    }

    public String determinarRutaPorRol(String rol, String correo) {
        String rolNormalizado = normalizarRol(rol);
        return switch (rolNormalizado) {
            case "ADMIN" -> "redirect:/admin/dashboard";
            case "VETERINARIO" -> "redirect:/animales/vista";
            default -> (correo != null && !correo.isBlank())
                    ? "redirect:/ciudadano/inicio?usuarioEmail=" + correo.trim() + "#mis-reportes"
                    : "redirect:/ciudadano/inicio#mis-reportes";
        };
    }

    // Registro público seguro: siempre fuerza rol CIUDADANO en el backend
    public Usuario registrarCiudadano(String nombre, String telefono, String correo, String password) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }
        if (correo == null || correo.isBlank() || !correo.contains("@")) {
            throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
        }
        if (password == null || password.trim().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        String correoLimpio = correo.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
        }

        Usuario nuevo = new Usuario(
                nombre.trim(),
                telefono != null ? telefono.trim() : "",
                correoLimpio,
                password,
                "CIUDADANO");
        return usuarioRepository.save(nuevo);
    }

    public Optional<Usuario> autenticar(String correo, String password) {
        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .filter(u -> password.equals(u.getPassword()));
    }

    @Bean
    public CommandLineRunner cargarUsuariosIniciales() {
        return args -> {
            crearSiNoExiste("Administradora AuraTerra", "3000000001", "admin@auraterra.com", "admin123", "ADMIN");
            crearSiNoExiste("Veterinario Clínico", "3000000002", "vet@auraterra.com", "vet123", "VETERINARIO");
            crearSiNoExiste("Ciudadano Voluntario", "3000000003", "ciudadano@auraterra.com", "user123", "CIUDADANO");
        };
    }

    private void crearSiNoExiste(String nombre, String telefono, String correo, String password, String rol) {
        if (!usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            usuarioRepository.save(new Usuario(nombre, telefono, correo, password, rol));
        }
    }
}