package com.optistock.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByIdUsuario(Integer idUsuario);

    List<Auditoria> findByEntidadAfectada(String entidadAfectada);

    List<Auditoria> findByAccion(String accion);
}
