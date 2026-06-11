package com.optistock.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsuarioLoginIgnoreCase(String usuarioLogin);

    boolean existsByUsuarioLoginIgnoreCase(String usuarioLogin);
}
