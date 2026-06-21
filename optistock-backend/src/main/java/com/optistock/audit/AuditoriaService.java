package com.optistock.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public void registrar(Integer idUsuario, String accion, String entidad, Integer registroId, String descripcion, HttpServletRequest request) {
        Auditoria auditoria = new Auditoria();
        auditoria.setIdUsuario(idUsuario);
        auditoria.setAccion(accion);
        auditoria.setEntidadAfectada(entidad);
        auditoria.setRegistroId(registroId);
        auditoria.setDescripcion(descripcion);
        auditoria.setFechaHora(LocalDateTime.now());
        
        if (request != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            auditoria.setIpOrigen(ip);
        }

        auditoriaRepository.save(auditoria);
        
        logger.info("Auditoría -> Usuario: {}, Acción: {}, Entidad: {}, RegistroID: {}, Descripción: {}", 
                idUsuario, accion, entidad, registroId, descripcion);
    }
}
