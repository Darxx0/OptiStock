package com.optistock.proveedor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    List<Proveedor> findByRazonSocialContainingIgnoreCase(String razonSocial);
}
