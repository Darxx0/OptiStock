package com.optistock.pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Set;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /** IDs de facturas que YA tienen al menos un pago registrado */
    @Query("SELECT DISTINCT p.factura.idFactura FROM Pago p")
    Set<Long> findFacturasConPago();

    List<Pago> findByFacturaIdFactura(Long idFactura);
}
