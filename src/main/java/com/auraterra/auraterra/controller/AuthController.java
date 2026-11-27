package com.auraterra.auraterra.controller;

import com.auraterra.auraterra.model.Usuario;
import com.auraterra.auraterra.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        Usuario logueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (logueado != null) {
            return usuarioService.determinarRutaPorRol(logueado.getRol(), logueado.getCorreo());
        }
        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @RequestParam("correo") String correo,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {
        Optional<Usuario> optUsuario = usuarioService.autenticar(correo, password);

        if (optUsuario.isEmpty()) {
            model.addAttribute("errorLogin", "Correo o contraseña incorrectos.");
            model.addAttribute("activeTab", "loginForm");
            return "login";
        }

        Usuario usuario = optUsuario.get();
        String rol = usuarioService.normalizarRol(usuario.getRol());

        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rolUsuario", rol);

        return usuarioService.determinarRutaPorRol(rol, usuario.getCorreo());
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam("correo") String correo,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            HttpSession session,
            Model model) {
        if (password == null || !password.equals(passwordConfirm)) {
            model.addAttribute("errorRegistro", "Las contraseñas no coinciden.");
            model.addAttribute("activeTab", "registerForm");
            return "login";
        }

        try {
            Usuario guardado = usuarioService.registrarCiudadano(nombre, telefono, correo, password);
            session.setAttribute("usuarioLogueado", guardado);
            session.setAttribute("rolUsuario", "CIUDADANO");

            return usuarioService.determinarRutaPorRol("CIUDADANO", guardado.getCorreo());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorRegistro", ex.getMessage());
            model.addAttribute("activeTab", "registerForm");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}