package com.optistock.cliente;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> obtenerTodos() {
        return clienteRepository.findAll().stream()
                .map(ClienteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        return ClienteDTO.fromEntity(cliente);
    }

    public ClienteDTO crear(ClienteDTO dto) {
        if (clienteRepository.findByNumeroDocumento(dto.getNumeroDocumento()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese número de documento");
        }
        
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setNumeroDocumento(dto.getNumeroDocumento());
        cliente.setTipoDocumento(dto.getTipoDocumento());
        
        return ClienteDTO.fromEntity(clienteRepository.save(cliente));
    }

    public ClienteDTO actualizar(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
                
        if (!cliente.getNumeroDocumento().equals(dto.getNumeroDocumento()) &&
            clienteRepository.findByNumeroDocumento(dto.getNumeroDocumento()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro cliente con ese número de documento");
        }
        
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setNumeroDocumento(dto.getNumeroDocumento());
        cliente.setTipoDocumento(dto.getTipoDocumento());
        
        return ClienteDTO.fromEntity(clienteRepository.save(cliente));
    }

    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }
}
