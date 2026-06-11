package com.optistock.factura;

import com.optistock.cliente.Cliente;
import com.optistock.cliente.ClienteRepository;
import com.optistock.producto.Producto;
import com.optistock.producto.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public FacturaService(FacturaRepository facturaRepository,
            DetalleFacturaRepository detalleFacturaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {
        this.facturaRepository = facturaRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
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
        // 1. Manejar Cliente
        Cliente cliente = clienteRepository.findByNumeroDocumento(dto.getDocumento())
                .orElseGet(() -> {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setNumeroDocumento(dto.getDocumento());
                    nuevoCliente.setTipoDocumento("CC");

                    // Separar nombre y apellido
                    String nombreCompleto = dto.getCliente() != null ? dto.getCliente().trim() : "Consumidor Final";
                    int espacioIndex = nombreCompleto.indexOf(" ");
                    if (espacioIndex > 0) {
                        nuevoCliente.setNombre(nombreCompleto.substring(0, espacioIndex));
                        nuevoCliente.setApellido(nombreCompleto.substring(espacioIndex + 1));
                    } else {
                        nuevoCliente.setNombre(nombreCompleto);
                        nuevoCliente.setApellido(".");
                    }

                    return clienteRepository.save(nuevoCliente);
                });

        // 2. Crear cabecera de Factura
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setIdUsuario(1L);
        factura.setFecha(LocalDateTime.now());
        Factura facturaGuardada = facturaRepository.save(factura);

        // 3. Crear Detalles
        for (FacturaItemDTO itemDTO : dto.getItems()) {
            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado: " + itemDTO.getProductoId()));

            DetalleFactura detalle = new DetalleFactura();
            detalle.setFactura(facturaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());

            BigDecimal subtotal = producto.getPrecioUnitario().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotal);

            int stockActual = producto.getCantidad() != null ? producto.getCantidad() : 0;
            if (itemDTO.getCantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La cantidad debe ser mayor a 0");
            }
            if (stockActual <= 0 || itemDTO.getCantidad() > stockActual) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para: " + producto.getNombre() +
                                " (disponible: " + stockActual + ")");
            }
            producto.setCantidad(stockActual - itemDTO.getCantidad());
            productoRepository.save(producto);

            detalleFacturaRepository.save(detalle);
            facturaGuardada.getDetalles().add(detalle);
        }

        return mapToDTOPublic(facturaGuardada);
    }

    public FacturaDTO mapToDTOPublic(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getIdFactura());

        Cliente c = factura.getCliente();
        String nombreCompleto = c.getNombre() + (c.getApellido().equals(".") ? "" : " " + c.getApellido());
        dto.setCliente(nombreCompleto);
        dto.setDocumento(c.getNumeroDocumento());

        dto.setFecha(factura.getFecha());

        // No almacenamos email ni teléfono en la BD, se devuelven vacíos
        dto.setEmail("");
        dto.setTelefono("");

        BigDecimal subtotalGeneral = BigDecimal.ZERO;

        List<FacturaItemDTO> items = factura.getDetalles().stream().map(detalle -> {
            FacturaItemDTO itemDTO = new FacturaItemDTO();
            itemDTO.setProductoId(detalle.getProducto().getIdProducto());
            itemDTO.setNombre(detalle.getProducto().getNombre());
            itemDTO.setCantidad(detalle.getCantidad());

            // Usar el precio unitario guardado en el subtotal en lugar de recalcularlo
            BigDecimal precioCalc = detalle.getSubtotal().divide(new BigDecimal(detalle.getCantidad()), 2,
                    java.math.RoundingMode.HALF_UP);
            itemDTO.setPrecio(precioCalc);

            return itemDTO;
        }).collect(Collectors.toList());

        for (FacturaItemDTO item : items) {
            subtotalGeneral = subtotalGeneral.add(item.getPrecio().multiply(new BigDecimal(item.getCantidad())));
        }

        dto.setItems(items);
        dto.setSubtotal(subtotalGeneral);
        // IVA es 19%
        BigDecimal iva = subtotalGeneral.multiply(new BigDecimal("0.19"));
        dto.setIva(iva);
        dto.setTotal(subtotalGeneral.add(iva));

        return dto;
    }
}
