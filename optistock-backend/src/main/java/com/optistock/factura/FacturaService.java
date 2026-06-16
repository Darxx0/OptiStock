package com.optistock.factura;

import com.optistock.cliente.Cliente;
import com.optistock.cliente.ClienteRepository; // Usamos el repositorio directamente
import com.optistock.producto.Producto;
import com.optistock.producto.ProductoRepository;
import com.optistock.security.UsuarioActualService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final ClienteRepository clienteRepository; // Repositorio inyectado
    private final ProductoRepository productoRepository;
    private final UsuarioActualService usuarioActualService;

    public FacturaService(FacturaRepository facturaRepository,
            DetalleFacturaRepository detalleFacturaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            UsuarioActualService usuarioActualService) {
        this.facturaRepository = facturaRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.usuarioActualService = usuarioActualService;
    }

    @Transactional(readOnly = true)
    public List<FacturaDTO> listarFacturas() {
        return facturaRepository.findAll().stream()
                .map(this::mapToDTOPublic)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacturaDTO obtenerFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));
        return mapToDTOPublic(factura);
    }

    @Transactional(rollbackFor = Exception.class)
    public FacturaDTO crearFactura(FacturaDTO dto) {
        // 1. Lógica de cliente integrada (sin depender de un Service externo)
        Cliente cliente = clienteRepository.findByNumeroDocumento(dto.getDocumento())
                .orElseGet(() -> {
                    Cliente nuevo = new Cliente();
                    nuevo.setNumeroDocumento(dto.getDocumento());
                    nuevo.setTipoDocumento("CC");
                    String nombreCompleto = dto.getCliente() != null ? dto.getCliente().trim() : "Consumidor Final";
                    int espacioIndex = nombreCompleto.indexOf(" ");
                    if (espacioIndex > 0) {
                        nuevo.setNombre(nombreCompleto.substring(0, espacioIndex));
                        nuevo.setApellido(nombreCompleto.substring(espacioIndex + 1));
                    } else {
                        nuevo.setNombre(nombreCompleto);
                        nuevo.setApellido(".");
                    }
                    return clienteRepository.save(nuevo);
                });

        // 2. Crear cabecera con el usuario autenticado
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setIdUsuario(Long.valueOf(usuarioActualService.getIdUsuarioActual()));
        factura.setFecha(LocalDateTime.now());
        Factura facturaGuardada = facturaRepository.save(factura);

        // 3. Procesar items
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        for (FacturaItemDTO itemDTO : dto.getItems()) {
            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

            if (itemDTO.getCantidad() <= 0 || producto.getCantidad() < itemDTO.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para: " + producto.getNombre());
            }

            producto.setCantidad(producto.getCantidad() - itemDTO.getCantidad());
            productoRepository.save(producto);

            DetalleFactura detalle = new DetalleFactura();
            detalle.setFactura(facturaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            BigDecimal subtotalItem = producto.getPrecioUnitario().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotalItem);

            detalleFacturaRepository.save(detalle);
            subtotalAcumulado = subtotalAcumulado.add(subtotalItem);
        }

        return mapToDTOPublic(facturaGuardada);
    }

    public FacturaDTO mapToDTOPublic(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getIdFactura());
        Cliente c = factura.getCliente();
        dto.setCliente(c.getNombre() + " " + c.getApellido());
        dto.setDocumento(c.getNumeroDocumento());
        dto.setFecha(factura.getFecha());

        BigDecimal subtotalGeneral = BigDecimal.ZERO;
        List<FacturaItemDTO> items = factura.getDetalles().stream().map(detalle -> {
            FacturaItemDTO itemDTO = new FacturaItemDTO();
            itemDTO.setProductoId(detalle.getProducto().getIdProducto());
            itemDTO.setNombre(detalle.getProducto().getNombre());
            itemDTO.setCantidad(detalle.getCantidad());
            itemDTO.setPrecio(
                    detalle.getSubtotal().divide(new BigDecimal(detalle.getCantidad()), 2, RoundingMode.HALF_UP));
            return itemDTO;
        }).collect(Collectors.toList());

        for (FacturaItemDTO item : items) {
            subtotalGeneral = subtotalGeneral.add(item.getPrecio().multiply(new BigDecimal(item.getCantidad())));
        }

        dto.setItems(items);
        dto.setSubtotal(subtotalGeneral);
        dto.setIva(subtotalGeneral.multiply(new BigDecimal("0.19")));
        dto.setTotal(dto.getSubtotal().add(dto.getIva()));

        return dto;
    }
}