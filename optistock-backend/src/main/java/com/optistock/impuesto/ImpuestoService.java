package com.optistock.impuesto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImpuestoService {

    private final ImpuestoRepository impuestoRepository;

    // Inyección limpia por constructor
    public ImpuestoService(ImpuestoRepository impuestoRepository) {
        this.impuestoRepository = impuestoRepository;
    }

    @Transactional(readOnly = true)
    public List<ImpuestoDTO> findAll() {
        return impuestoRepository.findAll().stream()
                .map(ImpuestoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ImpuestoDTO findById(Integer id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impuesto no encontrado con el ID: " + id));
        return ImpuestoDTO.fromEntity(impuesto);
    }

    @Transactional
    public ImpuestoDTO create(ImpuestoDTO dto) {
        Impuesto impuesto = new Impuesto();
        impuesto.setCodigo(dto.getCodigo().toUpperCase().trim());
        impuesto.setNombre(dto.getNombre().trim());
        impuesto.setPorcentaje(dto.getPorcentaje());
        impuesto.setActivo(true);

        Impuesto guardado = impuestoRepository.save(impuesto);
        return ImpuestoDTO.fromEntity(guardado);
    }

    @Transactional
    public ImpuestoDTO update(Integer id, ImpuestoDTO dto) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impuesto no encontrado con el ID: " + id));

        impuesto.setCodigo(dto.getCodigo().toUpperCase().trim());
        impuesto.setNombre(dto.getNombre().trim());
        impuesto.setPorcentaje(dto.getPorcentaje());
        if (dto.getActivo() != null) {
            impuesto.setActivo(dto.getActivo());
        }

        Impuesto actualizado = impuestoRepository.save(impuesto);
        return ImpuestoDTO.fromEntity(actualizado);
    }

    @Transactional
    public void delete(Integer id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impuesto no encontrado con el ID: " + id));
        
        // Si manejas borrado lógico en tu proyecto puedes usar: impuesto.setActivo(false); impuestoRepository.save(impuesto);
        impuestoRepository.delete(impuesto);
    }
}