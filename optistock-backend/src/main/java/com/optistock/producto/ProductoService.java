package com.optistock.producto;

import com.optistock.categoria.Categoria;
import com.optistock.categoria.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> findAll() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoDTO findById(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
        return ProductoDTO.fromEntity(p);
    }

    public ProductoDTO create(ProductoDTO dto) {
        Categoria categoria = resolveCategoria(dto);
        Producto p = new Producto();
        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecioUnitario(dto.getPrecio());
        p.setCategoria(categoria);
        return ProductoDTO.fromEntity(productoRepository.save(p));
    }

    public ProductoDTO update(Integer id, ProductoDTO dto) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
        if (dto.getNombre() != null)
            p.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null)
            p.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null)
            p.setPrecioUnitario(dto.getPrecio());
        if (dto.getCategoria() != null || dto.getIdCategoria() != null) {
            p.setCategoria(resolveCategoria(dto));
        }
        return ProductoDTO.fromEntity(productoRepository.save(p));
    }

    public void delete(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));

        p.setActivo(false);
        productoRepository.save(p);
    }

    // Resuelve categoria por idCategoria o por nombre (crea si no existe)
    private Categoria resolveCategoria(ProductoDTO dto) {
        if (dto.getIdCategoria() != null) {
            return categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Categoría no encontrada: " + dto.getIdCategoria()));
        }
        if (dto.getCategoria() != null && !dto.getCategoria().isBlank()) {
            return categoriaRepository.findByNombreIgnoreCase(dto.getCategoria())
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(dto.getCategoria());
                        return categoriaRepository.save(nueva);
                    });
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere categoria o idCategoria");
    }
}
