package com.ebbenezer.taller.config;

import com.ebbenezer.taller.model.Usuario;
import com.ebbenezer.taller.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearOActualizarSiDebil("dueno@ebbenezer.com", "Dueño", "dueno.2026", "dueno123", Usuario.Rol.DUENO);
        crearOActualizarSiDebil("empleado@ebbenezer.com", "Empleado", "empleado2026", "empleado123", Usuario.Rol.EMPLEADO);
    }

    private void crearOActualizarSiDebil(String email, String nombre, String password, String passwordAnterior, Usuario.Rol rol) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setEmail(email);
            nuevo.setNombre(nombre);
            nuevo.setRol(rol);
            return nuevo;
        });

        boolean nuevaContrasena = usuario.getPasswordHash() == null
                || passwordEncoder.matches(passwordAnterior, usuario.getPasswordHash());

        if (nuevaContrasena) {
            usuario.setPasswordHash(passwordEncoder.encode(password));
            usuarioRepository.save(usuario);
            System.out.println(">>> Contraseña actualizada para: " + email + " / " + password + " (" + rol + ")");
        }
    }
}
